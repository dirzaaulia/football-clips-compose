import json
from datetime import datetime
from pathlib import Path

from loguru import logger
from worker.clients.supabase_client import supabase_client
from worker.config.settings import settings


class TelemetryService:

    def record_cycle(
        self,
        mode: str,
        next_sleep_str: str,
        cycle_index: int,
        steps_log: list[dict]
    ):
        now_wib = datetime.now(settings.wib_tz)
        ts_str = now_wib.strftime("%d %b %Y, %H:%M:%S WIB")

        # 1. Supabase database logging
        try:
            cycle_res = supabase_client.table("worker_cycles").insert({
                "timestamp": ts_str,
                "mode": mode,
                "next_sleep": next_sleep_str,
                "cycle_index": cycle_index
            }).execute()

            if cycle_res.data:
                cycle_id = cycle_res.data[0]["id"]
                events_to_insert = []
                order = 1
                for step in steps_log:
                    title = step.get("title", "")
                    status = step.get("status", "OK")
                    for line in step.get("events", []):
                        events_to_insert.append({
                            "cycle_id": cycle_id,
                            "step_title": title,
                            "step_status": status,
                            "event_text": line,
                            "event_order": order
                        })
                        order += 1

                if events_to_insert:
                    supabase_client.table("worker_events").insert(events_to_insert).execute()

        except Exception as e:
            logger.warning(f"Telemetry Supabase error: {e}")

        # 2. Local web server (logs.json) compatibility
        try:
            log_paths = [
                Path("/home/dirzaaulia11/web/logs.json"),
                Path(__file__).resolve().parent.parent.parent / "web" / "logs.json",
            ]
            for target_path in log_paths:
                if target_path.parent.exists():
                    existing_logs = []
                    if target_path.exists():
                        try:
                            with open(target_path, encoding="utf-8") as f:
                                existing_logs = json.load(f)
                        except Exception:
                            existing_logs = []

                    cycle_record = {
                        "timestamp": now_wib.strftime("%Y-%m-%d %H:%M:%S WIB"),
                        "mode": mode,
                        "next_sleep": next_sleep_str,
                        "steps": steps_log
                    }
                    existing_logs.insert(0, cycle_record)
                    # Keep maximum 50 cycles
                    existing_logs = existing_logs[:50]
                    with open(target_path, "w", encoding="utf-8") as f:
                        json.dump(existing_logs, f, indent=2, ensure_ascii=False)
                    break
        except Exception as e:
            logger.warning(f"Telemetry web/logs.json error: {e}")

telemetry_service = TelemetryService()
