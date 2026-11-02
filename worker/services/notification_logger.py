import json
import uuid
from datetime import UTC, datetime
from typing import Any
from loguru import logger
from worker.clients.database_client import db_client as supabase_client


class NotificationLogger:
    def __init__(self):
        self._recent_notifications: list[dict[str, Any]] = []

    def _load_remote_history(self) -> list[dict[str, Any]]:
        if not supabase_client:
            return self._recent_notifications
        try:
            res = supabase_client.table("telemetry_state").select("payload").eq("key", "notifications").execute()
            if res.data and len(res.data) > 0:
                payload = res.data[0].get("payload", {})
                raw_list = payload.get("notifications", [])
                clean_list = [
                    n for n in raw_list
                    if (
                        n.get("audience", {}).get("total_devices", 0) > 0
                        or n.get("audience", {}).get("target_tokens", 0) > 0
                    )
                    and "Home vs Away" not in n.get("title", "")
                    and "Home Team" not in n.get("title", "")
                    and "dummy" not in n.get("title", "").lower()
                ]
                return clean_list
        except Exception as e:
            logger.debug(f"[NotificationLogger] Failed to load remote history from PostgREST: {e}")
        return self._recent_notifications

    def log_notification(
        self,
        event_type: str,
        match: dict[str, Any],
        title: str,
        body: str,
        club_ids: list[Any],
        audience: dict[str, Any],
        channel_id: str = "channel_live_scores"
    ) -> dict[str, Any] | None:
        total_devs = audience.get("total_devices", 0) if isinstance(audience, dict) else 0
        target_tokens = audience.get("target_tokens", 0) if isinstance(audience, dict) else 0
        if total_devs <= 0 and target_tokens <= 0:
            logger.debug(f"[NotificationLogger] Skipped logging '{title}' (0 devices).")
            return None

        if "Home vs Away" in title or "Home Team" in title or "dummy" in title.lower():
            return None

        home = (
            match.get("home_team_name")
            or (match.get("home_team") if isinstance(match.get("home_team"), str) else None)
            or (match.get("homeTeam", {}).get("name") if isinstance(match.get("homeTeam"), dict) else None)
            or "Home"
        )
        away = (
            match.get("away_team_name")
            or (match.get("away_team") if isinstance(match.get("away_team"), str) else None)
            or (match.get("awayTeam", {}).get("name") if isinstance(match.get("awayTeam"), dict) else None)
            or "Away"
        )

        entry = {
            "id": f"notif_{uuid.uuid4().hex[:10]}",
            "timestamp": datetime.now(UTC).isoformat(),
            "event_type": event_type,
            "title": title,
            "body": body,
            "match_id": str(match.get("id", "")),
            "home_team": home,
            "away_team": away,
            "club_ids": [int(c) for c in club_ids if str(c).isdigit()],
            "channel_id": channel_id,
            "audience": audience
        }

        # Merge with remote history
        current_list = self._load_remote_history()
        updated_list = [entry] + [n for n in current_list if n.get("id") != entry["id"]]
        self._recent_notifications = updated_list[:200]

        self._upload_history(self._recent_notifications)
        return entry

    def _upload_history(self, notifications: list[dict[str, Any]]):
        if not supabase_client:
            return
        try:
            payload = {
                "updated_at": datetime.now(UTC).isoformat(),
                "count": len(notifications),
                "notifications": notifications,
            }
            supabase_client.table("telemetry_state").upsert(
                {
                    "key": "notifications",
                    "payload": payload,
                    "updated_at": datetime.now(UTC).isoformat(),
                },
                on_conflict="key",
            ).execute()
            logger.info(f"[NotificationLogger] Published {len(notifications)} notifications to Oracle VM PostgREST.")
        except Exception as e:
            logger.debug(f"[NotificationLogger] Failed to upload notifications to PostgREST: {e}")

    def purge_invalid_entries(self):
        if not supabase_client:
            return
        cleaned = self._load_remote_history()
        self._upload_history(cleaned)


notification_logger = NotificationLogger()
try:
    notification_logger.purge_invalid_entries()
except Exception:
    pass
