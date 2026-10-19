import json
import uuid
from datetime import UTC, datetime
from typing import Any
from loguru import logger
from worker.clients.supabase_client import supabase_client


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
                return payload.get("notifications", [])
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
    ) -> dict[str, Any]:
        entry = {
            "id": f"notif_{uuid.uuid4().hex[:10]}",
            "timestamp": datetime.now(UTC).isoformat(),
            "event_type": event_type,
            "title": title,
            "body": body,
            "match_id": str(match.get("id", "")),
            "home_team": match.get("home_team", "Home"),
            "away_team": match.get("away_team", "Away"),
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


notification_logger = NotificationLogger()
