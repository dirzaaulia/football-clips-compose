import asyncio
import signal
import socket
import sys
from datetime import UTC, datetime, timedelta
from pathlib import Path
from typing import Any

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
from worker.clients.youtube_client import youtube_client
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
        self.fixture_cycle_index = self._get_initial_fixture_cycle_index()
        self.highlight_sweep_index = self._get_initial_highlight_sweep_index()
        self.last_youtube_sync_timestamp: float = 0.0
        self.last_websub_renewal_timestamp: float = 0.0
        self.fixture_task: asyncio.Task | None = None
        self.highlight_task: asyncio.Task | None = None
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

    @staticmethod
    def _get_initial_fixture_cycle_index() -> int:
        try:
            telemetry_data = telemetry_service._load_history()
            pipelines = telemetry_data.get("pipelines", {})
            if "fixture_engine" in pipelines:
                return int(pipelines["fixture_engine"].get("cycle_index", 0))
            cycles = [c for c in telemetry_data.get("cycles", []) if c.get("pipeline") == "FIXTURE_SYNC"]
            if cycles:
                return int(cycles[0].get("pipeline_cycle_index") or cycles[0].get("cycle_index", 0))
            return WorkerOrchestrator._get_initial_cycle_index()
        except Exception:
            return 0

    @staticmethod
    def _get_initial_highlight_sweep_index() -> int:
        try:
            telemetry_data = telemetry_service._load_history()
            pipelines = telemetry_data.get("pipelines", {})
            if "highlight_engine" in pipelines:
                return int(pipelines["highlight_engine"].get("sweep_index", 0))
            cycles = [c for c in telemetry_data.get("cycles", []) if c.get("pipeline") == "YOUTUBE_SCRAPER"]
            if cycles:
                return int(cycles[0].get("pipeline_cycle_index") or cycles[0].get("cycle_index", 0))
            return 340
        except Exception:
            return 340

    def _setup_signals(self):
        def handle_signal(sig, frame):
            logger.info("Shutdown signal received. Cancelling worker tasks and stopping cleanly...")
            self.is_running = False
            if self.fixture_task and not self.fixture_task.done():
                self.fixture_task.cancel()
            if self.highlight_task and not self.highlight_task.done():
                self.highlight_task.cancel()
        signal.signal(signal.SIGINT, handle_signal)
        signal.signal(signal.SIGTERM, handle_signal)

    async def calculate_highlight_dynamic_sleep(self) -> tuple[int, str]:
        # 1. Check daily quota status
        quota_status = youtube_client.get_quota_status()
        if quota_status.get("is_exhausted", False) or quota_status.get("hits_today", 0) >= quota_status.get("limit", 10000):
            hours_left = max(0.5, float(quota_status.get("hours_until_reset", 4.0)))
            sleep_sec = int(hours_left * 3600)
            return sleep_sec, f"{hours_left:.1f}h (Daily Quota Budget Exhausted - Sleeping until PT Midnight Reset)"

        # 2. Check unlinked finished matches in the last 30 days
        now_utc = datetime.now(UTC)
        thirty_days_ago = (now_utc - timedelta(days=30)).isoformat()
        res = (
            supabase_client.table("matches")
            .select("id, utc_date, last_youtube_check")
            .eq("status", "FINISHED")
            .is_("highlight_video_id", "null")
            .gte("utc_date", thirty_days_ago)
            .order("utc_date", desc=True)
            .limit(100)
            .execute()
        )
        unlinked = res.data or []
        if not unlinked:
            return 7200, "2 Hours (All Active Fixtures Linked - Standby Idle)"

        # 3. Check earliest cooldown expiration across all unlinked fixtures
        has_fresh_matches = False
        earliest_wait_min = 1440.0
        for m in unlinked:
            raw_date = m.get("utc_date")
            if not raw_date:
                continue
            m_time = match_service.safe_parse_iso(raw_date)
            age_hours = (now_utc - m_time).total_seconds() / 3600.0
            last_check_str = m.get("last_youtube_check")
            if not last_check_str:
                earliest_wait_min = 0.0
                break
            last_check = match_service.safe_parse_iso(last_check_str)
            mins_since = (now_utc - last_check).total_seconds() / 60.0

            if age_hours < 6:
                has_fresh_matches = True
                req_cd = 30.0
            elif age_hours < 24:
                req_cd = 120.0
            elif age_hours < 72:
                req_cd = 360.0
            else:
                req_cd = 1440.0

            wait_remaining = max(0.0, req_cd - mins_since)
            if wait_remaining < earliest_wait_min:
                earliest_wait_min = wait_remaining

        if earliest_wait_min <= 1.0:
            return 60, "1 Minute (Eligible Fixtures Ready for YouTube Sweep)"

        if has_fresh_matches:
            sleep_min = min(30, int(earliest_wait_min) + 1)
            return sleep_min * 60, f"{sleep_min}m (Tier 1 Fresh Match Embargo Cooldown)"

        sleep_min = min(120, max(15, int(earliest_wait_min) + 1))
        return sleep_min * 60, f"{sleep_min}m (Tier Cooldown Guardrail Pacing)"

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
            await asyncio.to_thread(maintenance_service.cleanup_old_data, log_events=step2_events)
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

        # Decoupled highlight scanning: runs independently of live games
        # Throttle only if it just ran very recently (< 2 minutes) unless backfill or newly finished
        should_scrape_youtube = (
            is_backfill_mode
            or newly_finished > 0
            or (self.last_youtube_sync_timestamp == 0.0)
            or (minutes_since_youtube >= 2.0)
        )

        if should_scrape_youtube:
            try:
                self.last_youtube_sync_timestamp = now_ts
                await asyncio.to_thread(highlight_service.sync_unlinked_highlights, max_batch=batch_limit, log_events=step3_events)
                step3_status = "OK"
            except Exception as e:
                append_event(step3_events, f"Error Step 3: {e}", "ERROR", "YOUTUBE", "STEP_ERROR")
                step3_status = "ERROR"
        else:
            append_event(
                step3_events,
                f"YouTube Scraper rate-throttle active (last completed {minutes_since_youtube:.1f}m ago). Next sweep in {max(0, int(2.0 - minutes_since_youtube))}m.",
                "INFO",
                "YOUTUBE",
                "RATE_THROTTLE"
            )
            step3_status = "THROTTLED"
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

    async def run_fixture_loop(self):
        logger.info("[FixtureEngine] Starting independent Fixture & Live Score loop...")
        while self.is_running:
            self.fixture_cycle_index += 1
            is_full = (self.fixture_cycle_index == 1) or (self.fixture_cycle_index % 12 == 0)
            logger.info(f"=== [FixtureEngine] Starting Fixture Cycle #{self.fixture_cycle_index} (is_full={is_full}) ===")
            steps_log = []

            # Step 1: Match & Live Score Sync (60s tick during live play)
            step1_events = []
            try:
                await match_service.sync_matches(is_full_sync=is_full, log_events=step1_events)
                step1_status = "OK"
            except Exception as e:
                append_event(step1_events, f"Error Step 1: {e}", "ERROR", "MATCH_SYNC", "STEP_ERROR")
                step1_status = "ERROR"
            steps_log.append({"title": "Step 1: Match & Live Score Sync", "status": step1_status, "events": step1_events})

            # Step 2: Cache Purge & Database Maintenance
            step2_events = []
            try:
                await asyncio.to_thread(maintenance_service.cleanup_old_data, log_events=step2_events)
                step2_status = "OK"
            except Exception as e:
                append_event(step2_events, f"Error Step 2: {e}", "ERROR", "MAINTENANCE", "STEP_ERROR")
                step2_status = "ERROR"
            steps_log.append({"title": "Step 2: Cache Purge & Database Maintenance", "status": step2_status, "events": step2_events})

            # Step 4: Standings Sync
            step4_events = []
            hours_since_last_standings = (datetime.now(UTC).timestamp() - standings_service.last_sync_timestamp) / 3600.0
            if is_full or (standings_service.last_sync_timestamp == 0.0) or (hours_since_last_standings >= 6.0):
                try:
                    await standings_service.sync_standings(force=is_full, log_events=step4_events)
                    step4_status = "OK"
                except Exception as e:
                    append_event(step4_events, f"Error Step 4: {e}", "ERROR", "STANDINGS", "STEP_ERROR")
                    step4_status = "ERROR"
            else:
                append_event(step4_events, f"Standings up-to-date (last synced {hours_since_last_standings:.1f}h ago). Skipping.", "INFO", "STANDINGS", "SKIPPED")
                step4_status = "SKIPPED"
            steps_log.append({"title": "Step 4: League Standings Sync", "status": step4_status, "events": step4_events})

            sleep_sec, sleep_desc = await self.calculate_dynamic_sleep()
            telemetry_service.record_cycle(
                mode="FULL FIXTURE SYNC" if is_full else "FIXTURE SYNC",
                next_sleep_str=sleep_desc,
                cycle_index=self.fixture_cycle_index,
                steps_log=steps_log,
                pipeline_type="FIXTURE_SYNC",
                pipeline_cycle_index=self.fixture_cycle_index,
            )

            logger.info(f"[FixtureEngine] Cycle #{self.fixture_cycle_index} complete. Sleeping {sleep_desc} ({sleep_sec}s)...")
            try:
                await asyncio.sleep(sleep_sec)
            except asyncio.CancelledError:
                break

    async def run_highlight_loop(self):
        logger.info("[HighlightEngine] Starting independent YouTube Highlight Scraper loop...")
        while self.is_running:
            self.highlight_sweep_index += 1
            logger.info(f"=== [HighlightEngine] Starting Highlight Sweep #{self.highlight_sweep_index} ===")
            steps_log = []

            # Renew WebSub subscriptions every 72h
            hours_since_last_websub = (datetime.now(UTC).timestamp() - self.last_websub_renewal_timestamp) / 3600.0
            if self.last_websub_renewal_timestamp == 0.0 or hours_since_last_websub >= 72.0:
                self.last_websub_renewal_timestamp = datetime.now(UTC).timestamp()
                asyncio.create_task(websub_subscriber.subscribe_all())
                logger.info("[HighlightEngine] Triggered background YouTube WebSub renewal task.")

            # Step 3: YouTube Official Highlight Scraping & Quota Maximizer
            step3_events = []
            try:
                self.last_youtube_sync_timestamp = datetime.now(UTC).timestamp()
                await asyncio.to_thread(highlight_service.sync_unlinked_highlights, max_batch=25, log_events=step3_events)
                step3_status = "OK"
            except Exception as e:
                append_event(step3_events, f"Error Step 3: {e}", "ERROR", "YOUTUBE", "STEP_ERROR")
                step3_status = "ERROR"
            steps_log.append({"title": "Step 3: YouTube Highlight Scraper", "status": step3_status, "events": step3_events})

            sleep_sec, sleep_desc = await self.calculate_highlight_dynamic_sleep()
            telemetry_service.record_cycle(
                mode="YOUTUBE SWEEP",
                next_sleep_str=sleep_desc,
                cycle_index=self.highlight_sweep_index,
                steps_log=steps_log,
                pipeline_type="YOUTUBE_SCRAPER",
                pipeline_cycle_index=self.highlight_sweep_index,
            )

            logger.info(f"[HighlightEngine] Sweep #{self.highlight_sweep_index} complete. Next sweep in {sleep_desc} ({sleep_sec}s)...")
            try:
                await asyncio.sleep(sleep_sec)
            except asyncio.CancelledError:
                break

    async def start(self):
        is_once = "--once" in sys.argv
        is_force = "--force" in sys.argv

        if is_once:
            logger.info("Running single combined cycle in --once mode...")
            await self.run_single_cycle(is_full_sync=is_force)
            return

        logger.info("Football Clips Worker Engine: Launching Decoupled Dual-Engine Pipelines...")
        self.fixture_task = asyncio.create_task(self.run_fixture_loop())
        self.highlight_task = asyncio.create_task(self.run_highlight_loop())
        try:
            await asyncio.gather(self.fixture_task, self.highlight_task)
        except asyncio.CancelledError:
            logger.info("Worker orchestrator stopped cleanly.")

    async def run_match_sync_only(self, is_full_sync: bool = False) -> dict[str, Any]:
        """Runs Step 1 (Live & Upcoming match sync) independently."""
        events = []
        status = "OK"
        try:
            await match_service.sync_matches(is_full_sync=is_full_sync, log_events=events)
        except Exception as e:
            append_event(events, f"Error: {e}", "ERROR", "MATCH_SYNC", "ERROR")
            status = "ERROR"
        return {"status": status, "events": events}

    async def run_highlight_sync_only(self, batch_limit: int = 25) -> dict[str, Any]:
        """Runs Step 3 (YouTube Highlight scraper & Quota Maximizer) independently."""
        events = []
        status = "OK"
        try:
            self.last_youtube_sync_timestamp = datetime.now(UTC).timestamp()
            await asyncio.to_thread(highlight_service.sync_unlinked_highlights, max_batch=batch_limit, log_events=events)
        except Exception as e:
            append_event(events, f"Error: {e}", "ERROR", "YOUTUBE", "ERROR")
            status = "ERROR"
        return {"status": status, "events": events}

if __name__ == "__main__":
    orchestrator = WorkerOrchestrator()
    asyncio.run(orchestrator.start())
