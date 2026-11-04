"""Telemetry aggregation, cycle summary computation, and retention pruning."""

from datetime import datetime
from typing import Any

from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings


class TelemetryAggregator:
    """Helper for transforming and pruning cycles and event logs."""

    @staticmethod
    def normalize_steps(steps_log: list[dict[str, Any]] | None, mode: str) -> list[dict[str, Any]]:
        """Normalize steps log into a uniform list of step objects."""
        if not steps_log:
            return []
        first_item = steps_log[0]
        if isinstance(first_item, dict) and "events" in first_item:
            return steps_log
        return [{"title": mode, "status": "OK", "events": steps_log}]

    @staticmethod
    def build_events(
        normalized_steps: list[dict[str, Any]],
        existing_events: list[dict[str, Any]],
        unique_cycle_id: int,
        clean_subsystem: str,
        clean_pipeline: str,
        now_utc: datetime,
    ) -> list[dict[str, Any]]:
        """Build structured event records from steps."""
        new_events = []
        curr_id = (existing_events[0]["id"] + 1) if existing_events and "id" in existing_events[0] else 1
        order = 1

        for step in normalized_steps:
            title = step.get("title", "") if isinstance(step, dict) else str(step)
            status = step.get("status", "OK") if isinstance(step, dict) else "OK"
            events = step.get("events", []) if isinstance(step, dict) else []

            for ev in events:
                if isinstance(ev, dict):
                    line = ev.get("line") or str(ev)
                    level = ev.get("level", "INFO")
                    attrs = ev.get("attributes")
                    code = ev.get("code")
                    m_id = ev.get("match_id")
                else:
                    line = getattr(ev, "line", None) or str(ev)
                    level = getattr(ev, "level", "INFO")
                    attrs = getattr(ev, "attributes", None)
                    code = getattr(ev, "code", None)
                    m_id = getattr(ev, "match_id", None)

                new_events.append({
                    "id": curr_id,
                    "cycle_id": unique_cycle_id,
                    "subsystem": clean_subsystem,
                    "pipeline": clean_pipeline,
                    "step_title": title,
                    "step_status": status,
                    "event_text": line,
                    "level": level,
                    "created_at": now_utc.isoformat(),
                    "order": order,
                    "attributes": attrs,
                    "code": code,
                    "match_id": m_id,
                })
                curr_id += 1
                order += 1
        return new_events

    @staticmethod
    def compute_summary(new_events: list[dict[str, Any]], pipeline_type: str) -> dict[str, Any]:
        """Calculates quota, search count, and link metrics from events."""
        quota_status = youtube_client.get_quota_status()
        matches_checked_ids = {
            str(ev.get("match_id"))
            for ev in new_events
            if ev.get("match_id") and str(ev.get("match_id")).isdigit()
        }
        matches_linked_count = sum(
            1 for ev in new_events
            if (ev.get("attributes") or {}).get("status") == "LINKED"
            or "RESULT_LINKED" in (ev.get("code") or "")
        )
        cooldown_count = 0
        quota_used = 0
        for ev in new_events:
            attrs = ev.get("attributes") or {}
            if attrs.get("quota_used"):
                quota_used += int(attrs["quota_used"])
            if attrs.get("cooldown_count"):
                cooldown_count = int(attrs["cooldown_count"])
            elif "[COOLDOWN PROTECTED]" in (ev.get("event_text") or ""):
                try:
                    parts = ev.get("event_text", "").split("fixture(s)")
                    if len(parts) > 1:
                        cooldown_count = int(parts[0].split()[-1])
                except Exception:
                    pass

        target_quota = getattr(settings, "youtube_target_daily_quota", 8500)
        return {
            "matches_searched": len(matches_checked_ids),
            "quota_used": quota_used,
            "quota_hits_today": quota_status.get("hits_today", 0),
            "quota_target": target_quota,
            "quota_remaining": quota_status.get("remaining", 0),
            "matches_linked": matches_linked_count,
            "cooldown_count": cooldown_count,
            "pipeline": pipeline_type,
        }

    @staticmethod
    def prune_cycles_and_events(
        new_cycle: dict[str, Any],
        existing_cycles: list[dict[str, Any]],
        new_events: list[dict[str, Any]],
        existing_events: list[dict[str, Any]],
        unique_cycle_id: int,
    ) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
        """Prunes cycles and events to prevent unbounded growth."""
        def is_yt(c: dict[str, Any], key: str = "pipeline") -> bool:
            val = str(c.get(key, "")).lower()
            m = str(c.get("mode", "")).lower()
            cid = c.get("id") or c.get("cycle_id")
            return "youtube" in val or "highlight" in val or "youtube" in m or (isinstance(cid, int) and 100000 <= cid < 200000)

        def is_ws(c: dict[str, Any], key: str = "pipeline") -> bool:
            val = str(c.get(key, "")).lower()
            m = str(c.get("mode", "")).lower()
            cid = c.get("id") or c.get("cycle_id")
            return "websub" in val or "websub" in m or (isinstance(cid, int) and cid >= 200000)

        from datetime import UTC, timedelta
        cutoff_30d = (datetime.now(UTC) - timedelta(days=30)).isoformat()

        combined_cycles = [new_cycle] + [
            c for c in existing_cycles
            if c.get("id") != unique_cycle_id and (c.get("created_at") or "") >= cutoff_30d
        ]
        yt_cycles = [c for c in combined_cycles if is_yt(c)][:150]
        ws_cycles = [c for c in combined_cycles if is_ws(c)][:100]
        fx_cycles = [c for c in combined_cycles if not is_yt(c) and not is_ws(c)][:150]
        pruned_cycles = yt_cycles + fx_cycles + ws_cycles
        pruned_cycles.sort(key=lambda c: c.get("created_at") or c.get("timestamp") or "", reverse=True)
        retained_ids = {c.get("id") for c in pruned_cycles if c.get("id") is not None}

        combined_events = [
            e for e in (new_events + existing_events)
            if (e.get("created_at") or "") >= cutoff_30d and e.get("cycle_id") in retained_ids
        ]
        yt_events = [e for e in combined_events if is_yt(e)][:3000]
        ws_events = [e for e in combined_events if is_ws(e)][:1000]
        fx_events = [e for e in combined_events if not is_yt(e) and not is_ws(e)][:3000]
        pruned_events = yt_events + fx_events + ws_events
        return pruned_cycles, pruned_events
