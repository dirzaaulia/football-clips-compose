import time
from typing import Any
import httpx
from loguru import logger

AUDIENCE_STATE_URL = "https://storage.googleapis.com/highlightdata-telemetry/audience/audience_state.json"


class AudienceService:
    def __init__(self, state_url: str = AUDIENCE_STATE_URL, cache_ttl_seconds: int = 60):
        self.state_url = state_url
        self.cache_ttl = cache_ttl_seconds
        self._cached_data: dict[str, Any] | None = None
        self._last_fetch: float = 0.0

    def fetch_audience_state(self, force_refresh: bool = False) -> dict[str, Any]:
        now = time.time()
        if not force_refresh and self._cached_data and (now - self._last_fetch < self.cache_ttl):
            return self._cached_data

        try:
            with httpx.Client(timeout=5.0) as client:
                res = client.get(self.state_url)
                if res.status_code == 200:
                    self._cached_data = res.json()
                    self._last_fetch = now
                    return self._cached_data
        except Exception as e:
            logger.debug(f"[AudienceService] Failed to fetch audience state: {e}")

        return self._cached_data or {}

    def get_audience_for_clubs(self, club_ids: list[Any]) -> dict[str, Any]:
        state = self.fetch_audience_state()
        clubs_dict = state.get("clubs", {})

        target_clubs = [clubs_dict[str(cid)] for cid in club_ids if str(cid) in clubs_dict]
        if not target_clubs:
            return {
                "distinct_users": 0,
                "total_devices": 0,
                "device_to_user_ratio": 1.0,
                "platform_breakdown": {"android": 0, "web": 0},
                "distribution": {
                    "single_device_users": 0,
                    "multi_device_users": 0,
                    "max_devices_for_single_user": 1,
                },
            }

        total_devices = sum(c.get("total_devices", 0) for c in target_clubs)
        distinct_users = sum(c.get("distinct_users", 0) for c in target_clubs)
        android_count = sum(c.get("platform_breakdown", {}).get("android", 0) for c in target_clubs)
        web_count = sum(c.get("platform_breakdown", {}).get("web", 0) for c in target_clubs)
        single_users = sum(c.get("distribution", {}).get("single_device_users", 0) for c in target_clubs)
        multi_users = sum(c.get("distribution", {}).get("multi_device_users", 0) for c in target_clubs)
        max_dev = max(
            (c.get("distribution", {}).get("max_devices_for_single_user", 1) for c in target_clubs),
            default=1,
        )
        ratio = round(total_devices / max(1, distinct_users), 2)

        return {
            "distinct_users": distinct_users,
            "total_devices": total_devices,
            "device_to_user_ratio": ratio,
            "platform_breakdown": {
                "android": android_count,
                "web": web_count,
            },
            "distribution": {
                "single_device_users": single_users,
                "multi_device_users": multi_users,
                "max_devices_for_single_user": max_dev,
            },
        }


audience_service = AudienceService()
