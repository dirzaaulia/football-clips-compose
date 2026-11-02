"""
Google Cloud Tasks client for event-driven, rate-limited highlight processing.
Dispatches isolated per-match tasks to 'fc-highlight-queue' in us-central1.
"""

from datetime import UTC, datetime, timedelta
import json
import os
import time
from typing import Any
from loguru import logger
from google.cloud import tasks_v2
from google.protobuf import timestamp_pb2
from worker.config.settings import settings


class CloudTasksClient:
    """
    Client for managing Google Cloud Tasks dispatching with zero monthly cost
    under the 1,000,000 operations/month Google Cloud free tier.
    """

    def __init__(self) -> None:
        self.project_id = os.getenv("GCP_PROJECT", os.getenv("GOOGLE_CLOUD_PROJECT", "highlightdata"))
        self.location = os.getenv("GCP_LOCATION", "us-central1")
        self.queue_id = "fc-highlight-queue"
        self._client: tasks_v2.CloudTasksClient | None = None
        self._service_url = os.getenv(
            "WORKER_SERVICE_URL",
            "https://fc-worker-519310318914.us-central1.run.app"
        )
        self._auth_token = os.getenv(
            "FC_INTERNAL_TOKEN",
            "NGnR7DZlXCQyDBHecirt7SER2Flryb2HTuY5-ETTkUnsag28mWDtLhqgMJbr27dm"
        )

    @property
    def client(self) -> tasks_v2.CloudTasksClient | None:
        if self._client is None:
            try:
                self._client = tasks_v2.CloudTasksClient()
            except Exception as e:
                logger.warning(f"[CloudTasksClient] Could not initialize CloudTasksClient: {e}")
                self._client = None
        return self._client

    def dispatch_highlight_task(
        self,
        match_id: int | str,
        home: str = "",
        away: str = "",
        competition: str = "",
        schedule_delay_seconds: int = 0
    ) -> bool:
        """
        Dispatches an isolated highlight search task for a single finished match.
        Isolates execution, prevents cross-match delays, and adheres to 2 tasks/sec rate limiting.
        """
        c = self.client
        if not c:
            logger.info(f"[CloudTasksClient] Cloud Tasks offline. Task for match {match_id} will be handled by regular sweep.")
            return False

        try:
            parent = c.queue_path(self.project_id, self.location, self.queue_id)
            url = f"{self._service_url}/tasks/process-highlight"
            payload = {
                "match_id": str(match_id),
                "home": home,
                "away": away,
                "competition": competition,
                "dispatched_at": time.time(),
            }
            body = json.dumps(payload).encode("utf-8")

            task: dict[str, Any] = {
                "http_request": {
                    "http_method": tasks_v2.HttpMethod.POST,
                    "url": url,
                    "headers": {
                        "Content-Type": "application/json",
                        "X-FC-Internal": self._auth_token,
                        "User-Agent": "FC-Cloud-Tasks/1.0",
                    },
                    "body": body,
                }
            }

            if schedule_delay_seconds > 0:
                d = datetime.now(UTC) + timedelta(seconds=schedule_delay_seconds)
                timestamp = timestamp_pb2.Timestamp()
                timestamp.FromDatetime(d)
                task["schedule_time"] = timestamp

            # Task ID naming ensures idempotency (same match won't duplicate tasks within window)
            delay_suffix = f"-delay{schedule_delay_seconds}s" if schedule_delay_seconds > 0 else ""
            task_id = f"match-{match_id}-{int(time.time() // 60)}{delay_suffix}"
            task["name"] = c.task_path(self.project_id, self.location, self.queue_id, task_id)

            response = c.create_task(request={"parent": parent, "task": task})
            logger.info(f"[CloudTasksClient] Dispatched task {response.name} for match {match_id} ({home} vs {away})")
            return True

        except Exception as e:
            if "AlreadyExists" in str(e):
                logger.debug(f"[CloudTasksClient] Task for match {match_id} already queued.")
                return True
            logger.warning(f"[CloudTasksClient] Failed to dispatch task for match {match_id}: {e}")
            return False


cloud_tasks_client = CloudTasksClient()
