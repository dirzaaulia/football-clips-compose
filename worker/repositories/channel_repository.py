"""Centralized database-driven repository for YouTube channels and aliases.

Loads from PostgreSQL `competitions` and `supported_clubs` via PostgREST
with 1-hour in-memory cache TTL. 100% database-driven.
"""

import re
import time
from typing import Any
from loguru import logger

from worker.clients.database_client import db_client as supabase_client
from worker.config.broadcaster_registry import GLOBAL_BROADCASTER_REGISTRY

GENERIC_CLUB_STOP_WORDS = {
    "club", "atletico", "real", "deportivo", "sporting", "racing",
    "city", "united", "inter", "union", "fc", "cf", "ac", "sc",
    "dynamo", "dinamo", "wanderers", "athletic", "rovers", "town",
    "county", "al", "el", "la", "de", "san", "santa",
}


class ChannelRepository:
    """100% Database-Driven in-memory cached repository for YouTube channels and aliases."""

    CACHE_TTL_SECONDS = 3600  # 1 hour

    def __init__(self):
        self._competitions: dict[str, dict[str, Any]] = {}
        self._clubs_by_id: dict[str, dict[str, Any]] = {}
        self._clubs_by_name: dict[str, dict[str, Any]] = {}
        self._channel_ids_by_handle: dict[str, str] = {}
        self._handles_by_channel_id: dict[str, str] = {}
        self._last_loaded_time: float = 0.0
        self._initialized = False

    def ensure_loaded(self, force: bool = False):
        """Synchronously ensures data is loaded from PostgreSQL."""
        now = time.time()
        if not force and self._initialized and (now - self._last_loaded_time) < self.CACHE_TTL_SECONDS:
            return

        try:
            # 1. Fetch competitions
            comp_res = supabase_client.table("competitions").select(
                "id,name,short_name,youtube_handle,youtube_channel_id,backup_channel_handles,backup_channel_ids"
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

            # 2. Fetch supported_clubs
            club_res = supabase_client.table("supported_clubs").select(
                "id,team_id,name,short_name,competition_code,youtube_handle,youtube_channel_id,aliases"
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
                    # index name without common suffixes
                    clean_name = name.replace(" fc", "").replace(" cf", "").replace(" ac", "").strip()
                    if clean_name and clean_name not in new_clubs_by_name:
                        new_clubs_by_name[clean_name] = row
                if short_name:
                    new_clubs_by_name[short_name] = row

                for alias in (row.get("aliases") or []):
                    a_low = alias.lower().strip()
                    if a_low and a_low not in new_clubs_by_name:
                        new_clubs_by_name[a_low] = row

            # 3. Index Global Broadcaster Registry
            for b in GLOBAL_BROADCASTER_REGISTRY:
                h = b.get("channel_handle")
                cid = b.get("channel_id")
                if h and cid:
                    new_channel_ids[h.lower()] = cid

            if new_comps:
                self._competitions = new_comps
            if new_clubs_by_name:
                self._clubs_by_id = new_clubs_by_id
                self._clubs_by_name = new_clubs_by_name
            if new_channel_ids:
                self._channel_ids_by_handle = new_channel_ids
                self._handles_by_channel_id = {cid: h for h, cid in new_channel_ids.items() if not cid.startswith("UC_")}

            self._last_loaded_time = now
            self._initialized = True
            logger.debug(
                f"[ChannelRepository] Refreshed: {len(self._competitions)} comps, "
                f"{len(self._clubs_by_id)} clubs, {len(self._channel_ids_by_handle)} channel IDs."
            )
        except Exception as e:
            logger.warning(f"[ChannelRepository] Fetch failed: {e}")
            self._initialized = True

    def get_competition_channel(self, comp_code: str) -> tuple[str, str]:
        """Returns (handle, channel_id) for competition."""
        self.ensure_loaded()
        code = comp_code.upper().strip()
        row = self._competitions.get(code)
        if row:
            return (row.get("youtube_handle") or "", row.get("youtube_channel_id") or "")
        return ("", "")

    def get_competition_backups(self, comp_code: str) -> list[str]:
        """Returns list of backup channel handles for competition."""
        self.ensure_loaded()
        code = comp_code.upper().strip()
        row = self._competitions.get(code)
        if row and row.get("backup_channel_handles"):
            return row["backup_channel_handles"]
        return []

    def get_club_channel(self, club_name: str, short_name: str = "") -> tuple[str, str]:
        """Returns (handle, channel_id) for a club with strict word-boundary matching."""
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
            # Word-boundary match across clubs to avoid intra-word collisions (e.g. 'gent' in 'argentinos')
            for k, row in self._clubs_by_name.items():
                if len(k) >= 4 and k not in GENERIC_CLUB_STOP_WORDS:
                    k_pat = rf"\b{re.escape(k)}\b"
                    norm_pat = rf"\b{re.escape(norm_clean)}\b"
                    if re.search(k_pat, norm_clean) or re.search(norm_pat, k):
                        match = row
                        break

        if match:
            return (match.get("youtube_handle") or "", match.get("youtube_channel_id") or "")
        return ("", "")

    def get_club_handle(self, club_name: str, short_name: str = "") -> str | None:
        """Returns YouTube handle (@handle) for a club."""
        handle, _ = self.get_club_channel(club_name, short_name)
        return handle or None

    def get_club_aliases(self, club_name: str, short_name: str = "") -> list[str]:
        """Returns search/matching aliases for a club from database."""
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

        if match and match.get("aliases"):
            aliases = list(match["aliases"])
            if match.get("short_name") and match["short_name"].lower() not in aliases:
                aliases.append(match["short_name"].lower())
            if norm_clean not in aliases:
                aliases.append(norm_clean)
            return aliases

        # Default fallback to normalized club name without parentheticals
        return [norm_clean] if norm_clean else [norm_name]

    def get_channel_id_for_handle(self, handle: str) -> str | None:
        """Returns YouTube channel ID for a given handle if known in database."""
        self.ensure_loaded()
        if not handle:
            return None
        return self._channel_ids_by_handle.get(handle.lower().strip())

    def get_handle_for_channel_id(self, channel_id: str) -> str | None:
        """Returns YouTube @handle for a given channel ID if known in database."""
        self.ensure_loaded()
        if not channel_id:
            return None
        return self._handles_by_channel_id.get(channel_id.strip())

    def get_all_websub_channels(self) -> dict[str, str]:
        """Returns all verified channel_id -> label mapping across leagues, clubs, and broadcasters."""
        self.ensure_loaded()
        channel_map: dict[str, str] = {}

        for comp_id, row in self._competitions.items():
            cid = row.get("youtube_channel_id")
            handle = row.get("youtube_handle", "")
            if cid and not cid.startswith("UC_"):
                channel_map[cid] = f"League: {comp_id} ({handle})"
            b_handles = row.get("backup_channel_handles") or []
            b_ids = row.get("backup_channel_ids") or []
            for i, bh in enumerate(b_handles):
                bcid = b_ids[i] if i < len(b_ids) else self._channel_ids_by_handle.get(bh.lower())
                if bcid and not bcid.startswith("UC_") and bcid not in channel_map:
                    channel_map[bcid] = f"Backup Broadcaster: {comp_id} ({bh})"

        for club_id, row in self._clubs_by_id.items():
            cid = row.get("youtube_channel_id")
            handle = row.get("youtube_handle", "")
            name = row.get("name", "")
            if cid and not cid.startswith("UC_") and cid not in channel_map:
                channel_map[cid] = f"Club: {name} ({handle})"

        for b in GLOBAL_BROADCASTER_REGISTRY:
            cid = b.get("channel_id") or self._channel_ids_by_handle.get((b.get("channel_handle") or "").lower())
            if cid and not cid.startswith("UC_") and cid not in channel_map:
                name = b.get("name", "Broadcaster")
                handle = b.get("channel_handle", "")
                channel_map[cid] = f"Registry Broadcaster: {name} ({handle})"

        return channel_map


# Global singleton instance
channel_repository = ChannelRepository()
