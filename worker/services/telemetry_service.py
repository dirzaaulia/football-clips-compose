import json
import os
from datetime import UTC, datetime, timedelta
from pathlib import Path

from loguru import logger
from worker.clients.supabase_client import supabase_client
from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings

LOCAL_CACHE_PATH = "telemetry_cache.json"


class TelemetryService:

    def __init__(self):
        pass

    def _load_history(self) -> dict:
        """Loads existing telemetry history from Oracle VM PostgREST or local disk cache."""
        if supabase_client:
            try:
                res = supabase_client.table("telemetry_state").select("payload").eq("key", "telemetry").execute()
                if res.data and len(res.data) > 0:
                    payload = res.data[0].get("payload", {})
                    if isinstance(payload, dict) and "cycles" in payload:
                        return payload
            except Exception as e:
                logger.debug(f"[TelemetryService] Failed to load remote telemetry from PostgREST: {e}")

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
        steps_log: list[dict],
        pipeline_type: str = "FIXTURE_SYNC",
        pipeline_cycle_index: int | None = None,
    ):
        now_utc = datetime.now(UTC)
        now_wib = datetime.now(settings.wib_tz)
        ts_str = now_wib.strftime("%d %b %Y, %H:%M:%S WIB")

        # 1. Load current history
        history = self._load_history()
        existing_cycles = history.get("cycles", [])
        existing_events = history.get("events", [])
        existing_pipelines = history.get("pipelines", {})

        # Compute unique ID to avoid collision between pipelines
        unique_cycle_id = cycle_index if pipeline_type == "FIXTURE_SYNC" else (cycle_index + 100000)

        # 2. Build new cycle object
        new_cycle = {
            "id": unique_cycle_id,
            "cycle_index": cycle_index,
            "timestamp": ts_str,
            "mode": mode,
            "next_sleep": next_sleep_str,
            "created_at": now_utc.isoformat(),
            "pipeline": pipeline_type,
            "pipeline_cycle_index": pipeline_cycle_index or cycle_index,
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
                is_hl = any(k in upper for k in ["HIGHLIGHT", "LINKED", "MATCHED", "VERIFIED"])
                is_goal = any(k in upper for k in ["GOAL", "SCORE", "LIVE SCORE"])
                is_err = any(k in upper for k in ["ERROR", "CRITICAL", "EXCEPTION", "FAILED"])

                new_events.append({
                    "id": curr_id,
                    "cycle_id": unique_cycle_id,
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

        # 4. Merge and retain recent execution cycles (max 50 latest cycles)
        combined_cycles = [new_cycle] + [c for c in existing_cycles if c.get("id") != unique_cycle_id]
        combined_cycles.sort(
            key=lambda c: c.get("created_at") or c.get("timestamp") or "",
            reverse=True
        )
        pruned_cycles = combined_cycles[:50]
        retained_cycle_ids = {c.get("id") for c in pruned_cycles if c.get("id") is not None}

        # Prune events to only those belonging to retained cycles
        combined_events = new_events + existing_events
        pruned_events = [e for e in combined_events if e.get("cycle_id") in retained_cycle_ids]

        # 5. Live Quota Metrics
        quota_status = youtube_client.get_quota_status()

        # Update structured pipeline status
        if pipeline_type == "FIXTURE_SYNC":
            existing_pipelines["fixture_engine"] = {
                "cycle_index": pipeline_cycle_index or cycle_index,
                "cadence": next_sleep_str,
                "status": "LIVE_ACTIVE" if "60s" in next_sleep_str else "IDLE",
                "last_sync": now_utc.isoformat(),
                "mode": mode,
            }
        elif pipeline_type == "YOUTUBE_SCRAPER":
            existing_pipelines["highlight_engine"] = {
                "sweep_index": pipeline_cycle_index or cycle_index,
                "cadence": next_sleep_str,
                "status": "AUTONOMOUS_SWEEP",
                "quota_hits": quota_status.get("hits_today", 0),
                "quota_target": 8500,
                "last_sweep": now_utc.isoformat(),
                "mode": mode,
            }

        payload = {
            "updated_at": now_utc.isoformat(),
            "cycle_count": len(pruned_cycles),
            "quota": quota_status,
            "pipelines": existing_pipelines,
            "cycles": pruned_cycles,
            "events": pruned_events,
        }

        # 6. Save local disk cache
        try:
            with open(LOCAL_CACHE_PATH, "w", encoding="utf-8") as f:
                json.dump(payload, f, ensure_ascii=False)
        except Exception as e:
            logger.debug(f"[TelemetryService] Failed to save local cache: {e}")

        # 7. Persist to Oracle VM PostgreSQL via PostgREST
        if supabase_client:
            try:
                supabase_client.table("telemetry_state").upsert(
                    {
                        "key": "telemetry",
                        "payload": payload,
                        "updated_at": now_utc.isoformat(),
                    },
                    on_conflict="key",
                ).execute()
                logger.info(f"[TelemetryService] Published {len(pruned_cycles)} cycles & {len(pruned_events)} events to Oracle VM PostgREST.")
            except Exception as e:
                logger.warning(f"[TelemetryService] PostgREST telemetry_state upsert failed: {e}")


telemetry_service = TelemetryService()
