"""Structured cycle telemetry and pipeline observability service.

Persists execution cycles, structured step logs, and live health metrics
to Oracle VM PostgreSQL via PostgREST telemetry_state table.
"""

import json
import os
import tempfile
import traceback
from datetime import UTC, datetime
from typing import Any

from loguru import logger
from worker.clients.database_client import db_client as supabase_client
from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings
from worker.services.telemetry_aggregator import TelemetryAggregator

LOCAL_CACHE_PATH = os.path.join(tempfile.gettempdir(), "telemetry_cache.json")


class TelemetryService:
    def __init__(self):
        pass

    def _load_history(self) -> dict[str, Any]:
        """Loads existing telemetry history from Oracle VM PostgREST or local disk cache."""
        if supabase_client:
            try:
                res = (
                    supabase_client.table("telemetry_state")
                    .select("payload")
                    .eq("key", "telemetry")
                    .execute()
                )
                if res.data and len(res.data) > 0:
                    payload = res.data[0].get("payload", {})
                    if isinstance(payload, dict) and "cycles" in payload:
                        return payload
            except Exception as e:
                logger.debug(f"[TelemetryService] Failed to load remote telemetry: {e}")

        if os.path.exists(LOCAL_CACHE_PATH):
            try:
                with open(LOCAL_CACHE_PATH, "r", encoding="utf-8") as f:
                    data = json.load(f)
                    if isinstance(data, dict) and "cycles" in data:
                        return data
            except Exception as e:
                logger.debug(f"[TelemetryService] Failed to load local cache: {e}")

        return {"cycles": [], "events": [], "pipelines": {}}

    def record_cycle(
        self,
        mode: str,
        next_sleep_str: str,
        cycle_index: int,
        steps_log: list[dict[str, Any]],
        pipeline_type: str = "FIXTURE_SYNC",
        pipeline_cycle_index: int | None = None,
    ):
        """Records an execution cycle and its associated events to PostgREST and cache."""
        now_utc = datetime.now(UTC)
        now_wib = datetime.now(settings.wib_tz)
        ts_str = now_wib.strftime("%Y-%m-%d %H:%M:%S WIB")

        history = self._load_history()
        existing_cycles = history.get("cycles", [])
        existing_events = history.get("events", [])
        existing_pipelines = history.get("pipelines", {})

        clean_cycle_index = cycle_index % 100000
        clean_pipeline_index = (pipeline_cycle_index % 100000) if pipeline_cycle_index is not None else clean_cycle_index

        if pipeline_type in ["FIXTURE_SYNC", "fixtures"]:
            clean_pipeline, clean_subsystem, unique_cycle_id = "fixtures", "FIXTURE_SYNC", clean_cycle_index
        elif pipeline_type in ["youtube_upload", "WEBSUB", "WEBSUB_PUSH", "WEBSUB_RENEWAL", "websub"]:
            clean_pipeline, clean_subsystem, unique_cycle_id = "websub", "WEBSUB_PUSH", clean_cycle_index + 200000
        else:
            clean_pipeline, clean_subsystem, unique_cycle_id = "youtube", "HIGHLIGHT_PIPELINE", clean_cycle_index + 100000

        new_cycle = {
            "id": unique_cycle_id,
            "cycle_index": clean_cycle_index,
            "timestamp": ts_str,
            "mode": mode,
            "next_sleep": next_sleep_str,
            "created_at": now_utc.isoformat(),
            "pipeline": clean_pipeline,
            "subsystem": clean_subsystem,
            "pipeline_cycle_index": clean_pipeline_index,
        }

        normalized_steps = TelemetryAggregator.normalize_steps(steps_log, mode)
        new_events = TelemetryAggregator.build_events(
            normalized_steps, existing_events, unique_cycle_id, clean_subsystem, clean_pipeline, now_utc
        )
        cycle_summary = TelemetryAggregator.compute_summary(new_events, pipeline_type)
        new_cycle["summary"] = cycle_summary

        pruned_cycles, pruned_events = TelemetryAggregator.prune_cycles_and_events(
            new_cycle, existing_cycles, new_events, existing_events, unique_cycle_id
        )

        quota_status = youtube_client.get_quota_status()
        self._update_pipelines_status(existing_pipelines, pipeline_type, pipeline_cycle_index or cycle_index, next_sleep_str, now_utc, mode, quota_status)

        payload = {
            "updated_at": now_utc.isoformat(),
            "cycle_count": len(pruned_cycles),
            "quota": quota_status,
            "pipelines": existing_pipelines,
            "cycles": pruned_cycles,
            "events": pruned_events,
        }

        self._persist_local_cache(payload)
        self._persist_remote_db(unique_cycle_id, now_utc, clean_pipeline, clean_subsystem, new_events, cycle_summary, payload, pruned_cycles, pruned_events)

    def _update_pipelines_status(self, pipelines: dict, p_type: str, idx: int, sleep_str: str, now_utc: datetime, mode: str, quota: dict):
        if p_type == "FIXTURE_SYNC":
            pipelines["fixture_engine"] = {
                "cycle_index": idx,
                "cadence": sleep_str,
                "status": "LIVE_ACTIVE" if ("60s" in sleep_str or "1 Minute" in sleep_str) else "IDLE",
                "last_sync": now_utc.isoformat(),
                "mode": mode,
            }
        elif p_type == "YOUTUBE_SCRAPER":
            pipelines["highlight_engine"] = {
                "sweep_index": idx,
                "cadence": sleep_str,
                "status": "AUTONOMOUS_SWEEP",
                "quota_hits": quota.get("hits_today", 0),
                "quota_target": getattr(settings, "youtube_target_daily_quota", 8500),
                "last_sweep": now_utc.isoformat(),
                "mode": mode,
            }

    def _persist_local_cache(self, payload: dict[str, Any]):
        try:
            with open(LOCAL_CACHE_PATH, "w", encoding="utf-8") as f:
                json.dump(payload, f, ensure_ascii=False)
        except Exception as e:
            logger.debug(f"[TelemetryService] Failed to save local cache: {e}")

    def _persist_remote_db(self, cycle_id: int, now_utc: datetime, clean_pipeline: str, clean_subsystem: str, new_events: list, summary: dict, payload: dict, cycles: list, events: list):
        if not supabase_client:
            return
        try:
            all_ok = not any(ev.get("level") in ["ERROR", "CRITICAL"] for ev in new_events)
            cycle_row = {
                "cycle_id": cycle_id,
                "started_at": now_utc.isoformat(),
                "duration_ms": 250,
                "status": "SUCCESS" if all_ok else "ERROR",
                "fixtures_synced": summary.get("matches_searched", 0) if clean_pipeline == "fixtures" else 0,
                "live_matches_detected": 0,
                "highlights_scraped": summary.get("matches_linked", 0),
                "pipeline": clean_pipeline,
                "metadata": summary,
            }
            supabase_client.table("worker_cycles").upsert(cycle_row, on_conflict="cycle_id").execute()

            if new_events:
                db_events = []
                for ev in new_events[:500]:
                    meta = dict(ev.get("attributes") or {})
                    if ev.get("match_id"):
                        meta["match_id"] = str(ev.get("match_id"))
                    db_events.append({
                        "cycle_id": cycle_id,
                        "timestamp": ev.get("created_at") or now_utc.isoformat(),
                        "level": ev.get("level", "INFO"),
                        "subsystem": clean_subsystem,
                        "action": ev.get("code") or ev.get("step_title") or "TICK",
                        "message": ev.get("event_text", "")[:1000],
                        "metadata": meta,
                    })
                if db_events:
                    supabase_client.table("worker_events").insert(db_events).execute()
        except Exception as e:
            logger.debug(f"[TelemetryService] worker_cycles/worker_events notice: {e}")

        try:
            supabase_client.table("telemetry_state").upsert(
                {"key": "telemetry", "payload": payload, "updated_at": now_utc.isoformat()},
                on_conflict="key",
            ).execute()
            logger.info(f"[TelemetryService] Published {len(cycles)} cycles & {len(events)} events to Oracle VM PostgREST.")
        except Exception as e:
            logger.warning(f"[TelemetryService] PostgREST telemetry_state upsert failed: {e}")

    def record_crash(self, source: str, exc: Exception, tb_str: str | None = None):
        """Captures uncaught exceptions, formatted stack traces, and publishes CRITICAL telemetry alert."""
        err_msg = str(exc) or type(exc).__name__
        full_tb = tb_str or traceback.format_exc()
        crash_step = {
            "title": f"CRITICAL CRASH: {source}",
            "status": "CRITICAL",
            "events": [
                {
                    "line": f"[CRASH] {source} unhandled exception: {err_msg}",
                    "level": "CRITICAL",
                    "code": "TASK_CRASH",
                    "attributes": {"traceback": full_tb[:2000], "source": source},
                }
            ],
        }
        logger.error(f"[TelemetryService] Uncaught crash in {source}: {err_msg}\n{full_tb}")
        try:
            self.record_cycle(
                mode="CRASH_ALERT",
                next_sleep_str="HALTED_OR_RECOVERING",
                cycle_index=999999,
                steps_log=[crash_step],
                pipeline_type="SYSTEM_CRASH",
            )
        except Exception as tel_err:
            logger.error(f"[TelemetryService] Failed to record crash event: {tel_err}")

    def reset_telemetry(self):
        """Resets all recorded telemetry cycles, events, and pipeline statuses."""
        now_utc = datetime.now(UTC)
        payload = {
            "updated_at": now_utc.isoformat(),
            "cycle_count": 0,
            "quota": youtube_client.get_quota_status(),
            "pipelines": {},
            "cycles": [],
            "events": [],
        }
        if os.path.exists(LOCAL_CACHE_PATH):
            try:
                os.remove(LOCAL_CACHE_PATH)
            except Exception:
                pass
        self._persist_local_cache(payload)

        if supabase_client:
            try:
                supabase_client.table("telemetry_state").upsert(
                    {"key": "telemetry", "payload": payload, "updated_at": now_utc.isoformat()},
                    on_conflict="key",
                ).execute()
                logger.info("[TelemetryService] Reset telemetry state successfully in PostgREST.")
            except Exception as e:
                logger.warning(f"[TelemetryService] Failed to reset telemetry in PostgREST: {e}")


telemetry_service = TelemetryService()
