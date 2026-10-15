import asyncio
import signal
import socket
import sys
from datetime import UTC, datetime
from pathlib import Path

# Ensure line-buffered stdout for real-time telemetry logging
if hasattr(sys.stdout, "reconfigure"):
    try:
        sys.stdout.reconfigure(line_buffering=True)
        sys.stderr.reconfigure(line_buffering=True)
    except Exception:
        pass

# Default 20-second socket timeout to prevent any HTTP client from blocking indefinitely
socket.setdefaulttimeout(20.0)

# Ensure root workspace is in sys.path when invoked directly
_pkg_root = str(Path(__file__).resolve().parent.parent)
if _pkg_root not in sys.path:
    sys.path.insert(0, _pkg_root)

from loguru import logger

from worker.clients.supabase_client import supabase_client
from worker.config.settings import settings
from worker.scripts.subscribe_youtube_websub import websub_subscriber
from worker.services.highlight_service import highlight_service
from worker.services.maintenance_service import maintenance_service
from worker.services.match_service import match_service
from worker.services.standings_service import standings_service
from worker.services.telemetry_service import telemetry_service
from worker.utils.telemetry_logger import append_event


class WorkerOrchestrator:

    def __init__(self):
        self.is_running = True
        self.cycle_index = self._get_initial_cycle_index()
        self.last_youtube_sync_timestamp: float = 0.0
        self.last_websub_renewal_timestamp: float = 0.0
        self._setup_signals()

    @staticmethod
    def _get_initial_cycle_index() -> int:
        try:
            telemetry_data = telemetry_service._load_history()
            cycles = telemetry_data.get("cycles", [])
            if cycles:
                idx = cycles[0].get("cycle_index") or cycles[0].get("id")
                if idx:
                    return int(idx)
        except Exception as e:
            logger.warning(f"Could not fetch initial cycle index from GCS: {e}")
        return 0

    def _setup_signals(self):
        def handle_signal(sig, frame):
            logger.info("Shutdown signal received. Completing cycle and stopping cleanly...")
            self.is_running = False
        signal.signal(signal.SIGINT, handle_signal)
        signal.signal(signal.SIGTERM, handle_signal)

    async def calculate_dynamic_sleep(self) -> tuple[int, str]:
        now_utc = datetime.now(UTC)
        now_iso = now_utc.isoformat()

        # Check if there are active LIVE matches
        live_res = (
            supabase_client.table("matches")
            .select("id")
            .in_("status", ["IN_PLAY", "PAUSED"])
            .limit(1)
            .execute()
        )
        if live_res.data:
            return settings.sleep_live_match, "60s (Near-Live Match In Progress)"

        # Check if matches kick off within the next 45 minutes
        future_res = (
            supabase_client.table("matches")
            .select("utc_date")
            .eq("status", "TIMED")
            .gte("utc_date", now_iso)
            .order("utc_date", desc=False)
            .limit(1)
            .execute()
        )
        if future_res.data:
            next_start = match_service.safe_parse_iso(future_res.data[0]["utc_date"])
            diff_min = (next_start - now_utc).total_seconds() / 60
            if 0 <= diff_min <= 45:
                return settings.sleep_near_match, f"15 Minutes (Upcoming kickoff in {int(diff_min)}m)"

        return settings.sleep_normal, "2 Hours (Standard Idle Interval)"

    async def run_single_cycle(self, is_full_sync: bool = False):
        self.cycle_index += 1
        logger.info(f"=== Starting Cycle #{self.cycle_index} ===")
        steps_log = []

        # STEP 1: Match & Live Score Sync (Every cycle - 60s during live play)
        step1_events = []
        try:
            await match_service.sync_matches(is_full_sync=is_full_sync, log_events=step1_events)
            step1_status = "OK"
        except Exception as e:
            append_event(step1_events, f"Error Step 1: {e}", "ERROR", "MATCH_SYNC", "STEP_ERROR")
            step1_status = "ERROR"
        steps_log.append({"title": "Step 1: Match & Live Score Sync", "status": step1_status, "events": step1_events})

        # STEP 2: Cache Purge & Database Maintenance (Runs before highlights to clean/repair statuses)
        step2_events = []
        try:
            maintenance_service.cleanup_old_data(log_events=step2_events)
            step2_status = "OK"
        except Exception as e:
            append_event(step2_events, f"Error Step 2: {e}", "ERROR", "MAINTENANCE", "STEP_ERROR")
            step2_status = "ERROR"
        steps_log.append({"title": "Step 2: Cache Purge & Database Maintenance", "status": step2_status, "events": step2_events})

        # STEP 3: YouTube Official Highlight Scraping (Decoupled & Tier-Optimized)
        step3_events = []
        is_backfill_mode = is_full_sync or ("--force" in sys.argv) or ("--backfill" in sys.argv)
        batch_limit = 100 if is_backfill_mode else 25

        newly_finished = match_service.consume_newly_finished()
        now_ts = datetime.now(UTC).timestamp()
        minutes_since_youtube = (now_ts - self.last_youtube_sync_timestamp) / 60.0

        # Check if any match is currently live
        has_live_matches = False
        try:
            live_check = supabase_client.table("matches").select("id").in_("status", ["IN_PLAY", "PAUSED"]).limit(1).execute()
            has_live_matches = bool(live_check.data)
        except Exception:
            pass

        should_scrape_youtube = (
            is_backfill_mode
            or newly_finished > 0
            or not has_live_matches
            or (self.last_youtube_sync_timestamp == 0.0)
            or (minutes_since_youtube >= (settings.youtube_live_standby_interval / 60.0))
        )

        if should_scrape_youtube:
            try:
                self.last_youtube_sync_timestamp = now_ts
                highlight_service.sync_unlinked_highlights(max_batch=batch_limit, log_events=step3_events)
                step3_status = "OK"
            except Exception as e:
                append_event(step3_events, f"Error Step 3: {e}", "ERROR", "YOUTUBE", "STEP_ERROR")
                step3_status = "ERROR"
        else:
            append_event(
                step3_events,
                f"YouTube Scraper on standby during live play (last checked {minutes_since_youtube:.1f}m ago). Next scan in {max(0, int(settings.youtube_live_standby_interval / 60.0 - minutes_since_youtube))}m or on full-time.",
                "INFO",
                "YOUTUBE",
                "STANDBY"
            )
            step3_status = "STANDBY"
        steps_log.append({"title": "Step 3: YouTube Highlight Scraper", "status": step3_status, "events": step3_events})

        # STEP 4: League Standings Sync
        step4_events = []
        hours_since_last_standings = (datetime.now(UTC).timestamp() - standings_service.last_sync_timestamp) / 3600.0
        should_sync_standings = is_full_sync or (standings_service.last_sync_timestamp == 0.0) or (hours_since_last_standings >= 6.0)
        if should_sync_standings:
            try:
                await standings_service.sync_standings(force=is_full_sync, log_events=step4_events)
                step4_status = "OK"
            except Exception as e:
                append_event(step4_events, f"Error Step 4: {e}", "ERROR", "STANDINGS", "STEP_ERROR")
                step4_status = "ERROR"
        else:
            append_event(step4_events, f"Standings up-to-date (last synced {hours_since_last_standings:.1f}h ago). Skipping.", "INFO", "STANDINGS", "SKIPPED")
            step4_status = "SKIPPED"
        steps_log.append({"title": "Step 4: League Standings Sync", "status": step4_status, "events": step4_events})

        # STEP 5: YouTube WebSub Subscription Renewal (Every 3 days in background)
        hours_since_last_websub = (datetime.now(UTC).timestamp() - self.last_websub_renewal_timestamp) / 3600.0
        if is_full_sync or self.last_websub_renewal_timestamp == 0.0 or hours_since_last_websub >= 72.0:
            self.last_websub_renewal_timestamp = datetime.now(UTC).timestamp()
            asyncio.create_task(websub_subscriber.subscribe_all())
            logger.info("[MainWorker] Triggered background YouTube WebSub renewal task.")

        # Calculate dynamic sleep
        sleep_sec, sleep_desc = await self.calculate_dynamic_sleep()
        telemetry_service.record_cycle(
            mode="FULL SYNC" if is_full_sync else "REGULAR SYNC",
            next_sleep_str=sleep_desc,
            cycle_index=self.cycle_index,
            steps_log=steps_log
        )
        return sleep_sec, sleep_desc

    async def start(self):
        is_once = "--once" in sys.argv
        is_force = "--force" in sys.argv

        logger.info("Football Clips Worker Engine (Async) initialized.")
        while self.is_running:
            is_full = is_force or (self.cycle_index == 0) or (self.cycle_index % 12 == 0)
            sleep_sec, sleep_desc = await self.run_single_cycle(is_full_sync=is_full)

            if is_once:
                logger.info("Mode --once completed. Exiting.")
                break

            logger.info(f"Cycle #{self.cycle_index} complete. Sleeping {sleep_desc} ({sleep_sec}s)...")
            try:
                await asyncio.sleep(sleep_sec)
            except asyncio.CancelledError:
                break

if __name__ == "__main__":
    orchestrator = WorkerOrchestrator()
    asyncio.run(orchestrator.start())
