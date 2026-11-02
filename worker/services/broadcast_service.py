"""
Global Official Live Stream Broadcast Rights Engine & Directory
100% Database-driven via PostgreSQL telemetry_state table with in-memory caching.
"""

import json
import time
from pathlib import Path
from typing import Any

from loguru import logger

from worker.clients.database_client import db_client


class GlobalBroadcastRightsService:
    CACHE_TTL_SECONDS = 86400  # 24 hours

    def __init__(self):
        self._cached_directory: dict[str, Any] | None = None
        self._last_loaded: float = 0.0

    def _load_directory(self) -> dict[str, Any]:
        now = time.monotonic()
        if self._cached_directory and (now - self._last_loaded < self.CACHE_TTL_SECONDS):
            return self._cached_directory

        # 1. Fetch from database telemetry_state
        if db_client:
            try:
                res = db_client.table("telemetry_state").select("payload").eq("key", "broadcast_directory").execute()
                if res.data and len(res.data) > 0 and res.data[0].get("payload"):
                    self._cached_directory = res.data[0]["payload"]
                    self._last_loaded = now
                    logger.info("[BroadcastService] Loaded broadcast directory from database.")
                    return self._cached_directory
            except Exception as e:
                logger.warning(f"[BroadcastService] Database load failed, checking fallback file: {e}")

        # 2. Fallback to local static file if DB unreachable
        fallback_path = Path("web/public/broadcast_directory.json")
        if fallback_path.exists():
            try:
                with open(fallback_path, "r", encoding="utf-8") as f:
                    self._cached_directory = json.load(f)
                    self._last_loaded = now
                    logger.info("[BroadcastService] Loaded broadcast directory from local file fallback.")
                    return self._cached_directory
            except Exception as e:
                logger.error(f"[BroadcastService] Failed to load local broadcast fallback: {e}")

        return {"countries": {}, "season": 2026}

    def resolve_broadcaster(self, competition_id: str, country_code: str) -> dict[str, Any]:
        directory = self._load_directory()
        c_code = country_code.upper()
        country_data = directory.get("countries", {}).get(c_code, {})
        rights = country_data.get("rights", {})

        if competition_id in rights:
            return rights[competition_id]

        return {
            "competition_id": competition_id,
            "country_code": c_code,
            "country_name": country_data.get("name", c_code),
            "broadcaster_name": "Official League Hub",
            "stream_url": "https://www.fifa.com/fifaplus",
            "official_youtube_channel": None,
            "is_global_fallback": True,
            "is_direct_live_stream": False,
            "tier": "GLOBAL_D2C_FALLBACK",
        }

    def generate_full_world_directory(self) -> dict[str, Any]:
        return self._load_directory()

    def sync_and_upgrade_highlight_targets(self) -> list[str]:
        directory = self._load_directory()
        upgraded_channels = []
        for country_info in directory.get("countries", {}).values():
            for right in country_info.get("rights", {}).values():
                yt = right.get("official_youtube_channel")
                if yt and yt not in upgraded_channels:
                    upgraded_channels.append(yt)
        return upgraded_channels


broadcast_rights_service = GlobalBroadcastRightsService()
