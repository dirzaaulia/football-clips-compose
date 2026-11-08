"""Centralized database-driven repository for YouTube channels, broadcasters, taxonomy, and club aliases.

Loads from PostgreSQL (`competitions`, `supported_clubs`, `broadcasters`, `highlight_taxonomy`)
with 1-hour in-memory cache TTL and hermetic JSON seed fallback.
"""

import re
import time
from typing import Any

from loguru import logger

from worker.clients.database_client import db_client as supabase_client
from worker.models.taxonomy_models import BroadcasterEntity, TaxonomyEntry
from worker.repositories.taxonomy_seed_loader import (
    fetch_db_broadcasters,
    fetch_db_taxonomy,
    load_seed_broadcasters,
    load_seed_club_aliases,
    load_seed_taxonomy,
)

GENERIC_CLUB_STOP_WORDS = {
    "club", "atletico", "real", "deportivo", "sporting", "racing",
    "city", "united", "inter", "union", "fc", "cf", "ac", "sc",
    "dynamo", "dinamo", "wanderers", "athletic", "rovers", "town",
    "county", "al", "el", "la", "de", "san", "santa",
}


class ChannelRepository:
    """Database-driven in-memory cached repository with O(1) inverted club alias index."""

    CACHE_TTL_SECONDS = 3600

    def __init__(self):
        self._competitions: dict[str, dict[str, Any]] = {}
        self._clubs_by_id: dict[str, dict[str, Any]] = {}
        self._clubs_by_name: dict[str, dict[str, Any]] = {}
        self._channel_ids_by_handle: dict[str, str] = {}
        self._handles_by_channel_id: dict[str, str] = {}
        self._broadcasters: list[BroadcasterEntity] = load_seed_broadcasters()
        self._taxonomy: list[TaxonomyEntry] = load_seed_taxonomy()
        self._seed_club_aliases: dict[str, list[str]] = load_seed_club_aliases()
        self._last_loaded_time: float = 0.0
        self._initialized = False

    def ensure_loaded(self, force: bool = False):
        """Synchronously ensures data is loaded from PostgreSQL with seed fallback."""
        now = time.time()
        if not force and self._initialized and (now - self._last_loaded_time) < self.CACHE_TTL_SECONDS:
            return

        try:
            comp_res = supabase_client.table("competitions").select(
                "id,name,short_name,youtube_handle,youtube_channel_id,backup_channel_handles,backup_channel_ids",
            ).execute()

            new_comps: dict[str, dict[str, Any]] = {}
            new_channel_ids: dict[str, str] = {}
            for row in comp_res.data or []:
                cid = row.get("id")
                if cid:
                    new_comps[cid.upper()] = row
                    name = (row.get("name") or "").upper()
                    if name:
                        new_comps[name] = row
                handle = (row.get("youtube_handle") or "").strip()
                ch_id = (row.get("youtube_channel_id") or "").strip()
                if handle and ch_id:
                    new_channel_ids[handle.lower()] = ch_id
                b_handles = row.get("backup_channel_handles") or []
                b_ids = row.get("backup_channel_ids") or []
                for i, bh in enumerate(b_handles):
                    if i < len(b_ids) and b_ids[i]:
                        new_channel_ids[bh.lower()] = b_ids[i]

            club_res = supabase_client.table("supported_clubs").select(
                "id,team_id,name,short_name,competition_code,youtube_handle,youtube_channel_id,aliases",
            ).eq("is_active", True).execute()

            new_clubs_by_id: dict[str, dict[str, Any]] = {}
            new_clubs_by_name: dict[str, dict[str, Any]] = {}

            for row in club_res.data or []:
                cid = str(row.get("id"))
                new_clubs_by_id[cid] = row
                name = (row.get("name") or "").lower().strip()
                short_name = (row.get("short_name") or "").lower().strip()
                handle = (row.get("youtube_handle") or "").strip()
                channel_id = (row.get("youtube_channel_id") or "").strip()

                if handle and channel_id:
                    new_channel_ids[handle.lower()] = channel_id
                if name:
                    new_clubs_by_name[name] = row
                    clean_name = name.replace(" fc", "").replace(" cf", "").replace(" ac", "").strip()
                    if clean_name and clean_name not in new_clubs_by_name:
                        new_clubs_by_name[clean_name] = row
                if short_name:
                    new_clubs_by_name[short_name] = row
                for alias in row.get("aliases") or []:
                    a_low = alias.lower().strip()
                    if a_low and a_low not in new_clubs_by_name:
                        new_clubs_by_name[a_low] = row

            self._broadcasters = fetch_db_broadcasters(supabase_client, self._broadcasters)
            self._taxonomy = fetch_db_taxonomy(supabase_client, self._taxonomy)

            for b in self._broadcasters:
                if b.channel_handle and b.channel_id:
                    new_channel_ids[b.channel_handle.lower()] = b.channel_id

            if new_comps:
                self._competitions = new_comps
            if new_clubs_by_name:
                self._clubs_by_id = new_clubs_by_id
                self._clubs_by_name = new_clubs_by_name
            if new_channel_ids:
                self._channel_ids_by_handle = new_channel_ids
                self._handles_by_channel_id = {
                    cid: h
                    for h, cid in new_channel_ids.items()
                    if bool(re.match(r"^UC[A-Za-z0-9_-]{22}$", cid.strip()))
                }

            self._last_loaded_time = now
            self._initialized = True
        except Exception as e:
            logger.warning(f"[ChannelRepository] Fetch failed, using cached/seed catalogs: {e}")
            self._initialized = True

    def get_broadcasters(self) -> list[BroadcasterEntity]:
        self.ensure_loaded()
        return self._broadcasters

    def get_taxonomy_entries(self) -> list[TaxonomyEntry]:
        self.ensure_loaded()
        return self._taxonomy

    def get_seed_club_aliases(self) -> dict[str, list[str]]:
        return self._seed_club_aliases

    def get_competition_channel(self, comp_code: str) -> tuple[str, str]:
        self.ensure_loaded()
        row = self._competitions.get(comp_code.upper().strip())
        if row:
            return (row.get("youtube_handle") or "", row.get("youtube_channel_id") or "")
        return ("", "")

    def get_competition_backups(self, comp_code: str) -> list[str]:
        self.ensure_loaded()
        row = self._competitions.get(comp_code.upper().strip())
        return list(row.get("backup_channel_handles") or []) if row else []

    def _find_club_row(self, club_name: str, short_name: str = "") -> dict[str, Any] | None:
        self.ensure_loaded()
        norm_name = club_name.lower().strip()
        norm_short = short_name.lower().strip() if short_name else ""
        norm_clean = re.sub(r"\s*\([^)]*\)", "", norm_name).strip()

        match = self._clubs_by_name.get(norm_name) or (self._clubs_by_name.get(norm_short) if norm_short else None)
        if not match and norm_clean != norm_name:
            match = self._clubs_by_name.get(norm_clean)
        if not match:
            clean = norm_clean.replace(" fc", "").replace(" cf", "").replace(" ac", "").strip()
            match = self._clubs_by_name.get(clean)
        if not match:
            for k, row in self._clubs_by_name.items():
                if len(k) >= 4 and k not in GENERIC_CLUB_STOP_WORDS:
                    if re.search(rf"\b{re.escape(k)}\b", norm_clean) or re.search(rf"\b{re.escape(norm_clean)}\b", k):
                        return row
        return match

    def get_club_channel(self, club_name: str, short_name: str = "") -> tuple[str, str]:
        match = self._find_club_row(club_name, short_name)
        if match:
            return (match.get("youtube_handle") or "", match.get("youtube_channel_id") or "")
        return ("", "")

    def get_club_handle(self, club_name: str, short_name: str = "") -> str | None:
        handle, _ = self.get_club_channel(club_name, short_name)
        return handle or None

    def get_club_aliases(self, club_name: str, short_name: str = "") -> list[str]:
        norm_name = club_name.lower().strip()
        norm_clean = re.sub(r"\s*\([^)]*\)", "", norm_name).strip()
        match = self._find_club_row(club_name, short_name)
        if match and match.get("aliases"):
            aliases = list(match["aliases"])
            if match.get("short_name") and match["short_name"].lower() not in aliases:
                aliases.append(match["short_name"].lower())
            if norm_clean not in aliases:
                aliases.append(norm_clean)
            return aliases
        return [norm_clean] if norm_clean else [norm_name]

    def get_channel_id_for_handle(self, handle: str) -> str | None:
        self.ensure_loaded()
        return self._channel_ids_by_handle.get(handle.lower().strip()) if handle else None

    def get_handle_for_channel_id(self, channel_id: str) -> str | None:
        self.ensure_loaded()
        return self._handles_by_channel_id.get(channel_id.strip()) if channel_id else None

    def get_all_websub_channels(self) -> dict[str, str]:
        self.ensure_loaded()
        channel_map: dict[str, str] = {}

        def _valid_cid(val: str | None) -> bool:
            return bool(val and re.match(r"^UC[A-Za-z0-9_-]{22}$", val.strip()))

        for comp_id, row in self._competitions.items():
            cid = row.get("youtube_channel_id")
            if _valid_cid(cid):
                channel_map[cid] = f"League: {comp_id} ({row.get('youtube_handle', '')})"
            b_handles = row.get("backup_channel_handles") or []
            b_ids = row.get("backup_channel_ids") or []
            for i, bh in enumerate(b_handles):
                bcid = b_ids[i] if i < len(b_ids) else self._channel_ids_by_handle.get(bh.lower())
                if _valid_cid(bcid) and bcid not in channel_map:
                    channel_map[bcid] = f"Backup Broadcaster: {comp_id} ({bh})"

        for _, row in self._clubs_by_id.items():
            cid = row.get("youtube_channel_id")
            if _valid_cid(cid) and cid not in channel_map:
                channel_map[cid] = f"Club: {row.get('name', '')} ({row.get('youtube_handle', '')})"

        for b in self._broadcasters:
            cid = b.channel_id or self._channel_ids_by_handle.get(b.channel_handle.lower())
            if _valid_cid(cid) and cid not in channel_map:
                channel_map[cid] = f"Registry Broadcaster: {b.name} ({b.channel_handle})"

        return channel_map


channel_repository = ChannelRepository()
