import json
import os
from datetime import UTC, datetime, timedelta
from pathlib import Path

from loguru import logger
from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings

try:
    from google.cloud import storage
except ImportError:
    storage = None

BUCKET_NAME = "highlightdata-telemetry"
BLOB_PATH = "telemetry.json"
LOCAL_CACHE_PATH = "telemetry_cache.json"


class TelemetryService:

    def __init__(self, bucket_name: str = BUCKET_NAME, blob_path: str = BLOB_PATH):
        self.bucket_name = bucket_name
        self.blob_path = blob_path
        self._storage_client = None
        self._init_storage()

    def _init_storage(self):
        if not storage:
            return
        try:
            self._storage_client = storage.Client(project="highlightdata")
        except Exception as e:
            logger.debug(f"[TelemetryService] Storage client init deferred: {e}")

    def _load_history(self) -> dict:
        """Loads existing telemetry history from GCS or local disk cache."""
        if self._storage_client:
            try:
                bucket = self._storage_client.bucket(self.bucket_name)
                blob = bucket.blob(self.blob_path)
                if blob.exists():
                    content = blob.download_as_text()
                    data = json.loads(content)
                    if isinstance(data, dict) and "cycles" in data:
                        return data
            except Exception as e:
                logger.debug(f"[TelemetryService] Failed to load remote telemetry: {e}")

        # Fallback to local cache file
        if os.path.exists(LOCAL_CACHE_PATH):
            try:
                with open(LOCAL_CACHE_PATH, "r", encoding="utf-8") as f:
                    return json.load(f)
            except Exception:
                pass

        return {"cycles": [], "events": []}

    def record_cycle(
        self,
        mode: str,
        next_sleep_str: str,
        cycle_index: int,
        steps_log: list[dict]
    ):
        now_utc = datetime.now(UTC)
        now_wib = datetime.now(settings.wib_tz)
        ts_str = now_wib.strftime("%d %b %Y, %H:%M:%S WIB")

        # 1. Load current history
        history = self._load_history()
        existing_cycles = history.get("cycles", [])
        existing_events = history.get("events", [])

        # 2. Build new cycle object
        new_cycle = {
            "id": cycle_index,
            "cycle_index": cycle_index,
            "timestamp": ts_str,
            "mode": mode,
            "next_sleep": next_sleep_str,
            "created_at": now_utc.isoformat(),
        }

        # 3. Build new events list
        new_events = []
        # Calculate start event ID based on highest existing ID
        max_event_id = max((e.get("id", 0) for e in existing_events), default=0)
        curr_id = max_event_id + 1
        order = 1

        for step in steps_log:
            title = step.get("title", "")
            status = step.get("status", "OK")
            for line in step.get("events", []):
                upper = line.upper()
                is_hl = any(k in upper for k in ["HIGHLIGHT", "LINKED", "BERHASIL", "MATCHED"])
                is_goal = any(k in upper for k in ["GOAL", "SKOR", "LIVE SCORE"])
                is_err = any(k in upper for k in ["ERROR", "GAGAL", "CRITICAL", "EXCEPTION"])

                new_events.append({
                    "id": curr_id,
                    "cycle_id": cycle_index,
                    "step_title": title,
                    "step_status": status,
                    "event_text": line,
                    "event_order": order,
                    "is_highlight": is_hl,
                    "is_goal": is_goal,
                    "is_error": is_err,
                })
                curr_id += 1
                order += 1

        # 4. Merge and retain 7 full days of detailed traces (168 hours)
        cutoff_dt = now_utc - timedelta(days=7)
        combined_cycles = [new_cycle] + [c for c in existing_cycles if c.get("id") != cycle_index]

        # Prune cycles older than 7 days
        pruned_cycles = []
        retained_cycle_ids = set()
        for c in combined_cycles:
            c_time_str = c.get("created_at")
            if c_time_str:
                try:
                    c_dt = datetime.fromisoformat(c_time_str.replace("Z", "+00:00"))
                    if c_dt >= cutoff_dt:
                        pruned_cycles.append(c)
                        retained_cycle_ids.add(c.get("id"))
                        continue
                except Exception:
                    pass
            # If no created_at or recent, keep at least the last 50 cycles
            if len(pruned_cycles) < 50:
                pruned_cycles.append(c)
                retained_cycle_ids.add(c.get("id"))

        # Prune events to only those belonging to retained cycles
        combined_events = new_events + existing_events
        pruned_events = [e for e in combined_events if e.get("cycle_id") in retained_cycle_ids]

        # 5. Live Quota Metrics
        quota_status = youtube_client.get_quota_status()

        payload = {
            "updated_at": now_utc.isoformat(),
            "cycle_count": len(pruned_cycles),
            "quota": quota_status,
            "cycles": pruned_cycles,
            "events": pruned_events,
        }

        # 6. Save local disk cache
        try:
            with open(LOCAL_CACHE_PATH, "w", encoding="utf-8") as f:
                json.dump(payload, f, ensure_ascii=False)
        except Exception as e:
            logger.debug(f"[TelemetryService] Failed to save local cache: {e}")

        # 7. Upload to Google Cloud Storage
        if self._storage_client:
            try:
                bucket = self._storage_client.bucket(self.bucket_name)
                blob = bucket.blob(self.blob_path)
                blob.cache_control = "public, max-age=10, s-maxage=10"
                blob.upload_from_string(
                    json.dumps(payload, ensure_ascii=False),
                    content_type="application/json"
                )
                logger.info(f"[TelemetryService] Published {len(pruned_cycles)} cycles & {len(pruned_events)} events to GCS.")
            except Exception as e:
                logger.warning(f"[TelemetryService] GCS upload failed: {e}")


telemetry_service = TelemetryService()
