import time
from datetime import UTC, datetime
from typing import Any
import httpx
from loguru import logger
from worker.clients.supabase_client import supabase_client

AUDIENCE_STATE_URL = "https://edge.fc.dirzaaulia.com/rest/v1/telemetry_state?key=eq.audience&select=payload"


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
                res = client.get(self.state_url, headers={"Accept": "application/json"})
                if res.status_code == 200:
                    raw = res.json()
                    if isinstance(raw, list) and len(raw) > 0 and "payload" in raw[0]:
                        self._cached_data = raw[0]["payload"]
                    elif isinstance(raw, dict) and "payload" in raw:
                        self._cached_data = raw["payload"]
                    elif isinstance(raw, dict):
                        self._cached_data = raw
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

    def save_audience_state(self, state: dict[str, Any]) -> bool:
        self._cached_data = state
        self._last_fetch = time.time()
        try:
            if supabase_client:
                supabase_client.table("telemetry_state").upsert(
                    {
                        "key": "audience",
                        "payload": state,
                        "updated_at": datetime.now(UTC).isoformat(),
                    },
                    on_conflict="key",
                ).execute()
                logger.info("[AudienceService] Successfully persisted audience_state to Oracle VM PostgREST.")
                return True
        except Exception as e:
            logger.warning(f"[AudienceService] Failed to persist audience state: {e}")
            return False
        return False

    def register_web_device(self, token: str, club_ids: list[Any], device_name: str = "Web Browser") -> dict[str, Any]:
        import hashlib
        from datetime import UTC, datetime

        token_hash = hashlib.sha256(token.encode()).hexdigest()[:16]
        device_id = f"web_{token_hash}"
        user_id = f"usr_web_{token_hash[:8]}"

        state = self.fetch_audience_state(force_refresh=True)
        devices = state.setdefault("devices", {})
        devices[device_id] = {
            "device_id": device_id,
            "user_id": user_id,
            "platform": "web",
            "device_name": device_name,
            "favorite_clubs": [int(c) for c in club_ids if str(c).isdigit()],
            "app_version": "web-1.0",
            "last_active": datetime.now(UTC).isoformat()
        }

        # Recalculate clubs
        new_clubs = {}
        for dev_id, d in devices.items():
            if dev_id.startswith("dev_"):
                continue
            favs = d.get("favorite_clubs", [])
            plat = d.get("platform", "android")
            uid = d.get("user_id", dev_id)
            for cid in favs:
                cid_str = str(cid)
                if cid_str not in new_clubs:
                    new_clubs[cid_str] = {
                        "club_id": cid,
                        "total_devices": 0,
                        "distinct_users": set(),
                        "platform_breakdown": {"android": 0, "web": 0},
                        "devices_per_user_ratio": 1.0,
                        "distribution": {"single_device_users": 0, "multi_device_users": 0}
                    }
                c_entry = new_clubs[cid_str]
                c_entry["total_devices"] += 1
                c_entry["distinct_users"].add(uid)
                c_entry["platform_breakdown"][plat] = c_entry["platform_breakdown"].get(plat, 0) + 1

        for cid_str, c_entry in new_clubs.items():
            u_count = len(c_entry["distinct_users"])
            c_entry["distinct_users"] = u_count
            c_entry["devices_per_user_ratio"] = round(c_entry["total_devices"] / max(1, u_count), 2)
            c_entry["distribution"]["single_device_users"] = u_count

        state["clubs"] = new_clubs
        state["total_registered_devices"] = len(devices)
        state["total_registered_users"] = len(set(d.get("user_id", k) for k, d in devices.items()))
        state["updated_at"] = datetime.now(UTC).isoformat()
        self.save_audience_state(state)
        return state


audience_service = AudienceService()
