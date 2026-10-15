import json
import uuid
from datetime import UTC, datetime
from typing import Any
from loguru import logger

try:
    from google.cloud import storage
except ImportError:
    storage = None

BUCKET_NAME = "highlightdata-telemetry"
BLOB_PATH = "notifications.json"


class NotificationLogger:
    def __init__(self, bucket_name: str = BUCKET_NAME, blob_path: str = BLOB_PATH):
        self.bucket_name = bucket_name
        self.blob_path = blob_path
        self._recent_notifications: list[dict[str, Any]] = []
        self._storage_client = None
        self._init_storage()

    def _init_storage(self):
        if not storage:
            return
        try:
            self._storage_client = storage.Client(project="highlightdata")
        except Exception as e:
            logger.debug(f"[NotificationLogger] Storage client init deferred: {e}")

    def _load_remote_history(self) -> list[dict[str, Any]]:
        if not self._storage_client:
            return self._recent_notifications
        try:
            bucket = self._storage_client.bucket(self.bucket_name)
            blob = bucket.blob(self.blob_path)
            if blob.exists():
                content = blob.download_as_text()
                data = json.loads(content)
                return data.get("notifications", [])
        except Exception as e:
            logger.debug(f"[NotificationLogger] Failed to load remote history: {e}")
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
        if not self._storage_client:
            return
        try:
            bucket = self._storage_client.bucket(self.bucket_name)
            blob = bucket.blob(self.blob_path)
            blob.cache_control = "public, max-age=10, s-maxage=10"
            payload = {
                "updated_at": datetime.now(UTC).isoformat(),
                "count": len(notifications),
                "notifications": notifications
            }
            blob.upload_from_string(
                json.dumps(payload, indent=2),
                content_type="application/json"
            )
            logger.info(f"[NotificationLogger] Published {len(notifications)} notifications to GCS.")
        except Exception as e:
            logger.debug(f"[NotificationLogger] Failed to upload notifications to GCS: {e}")


notification_logger = NotificationLogger()
