"""Decoupled dual-engine worker orchestrator.

Coordinates autonomous fixture sync and highlight scraping loops.
"""

import asyncio
import signal
import sys
from datetime import UTC, datetime
from typing import Any

from loguru import logger
from worker.scripts.subscribe_youtube_websub import websub_subscriber
from worker.services.highlight_service import highlight_service
from worker.services.maintenance_service import maintenance_service
from worker.services.match_service import match_service
from worker.services.standings_service import standings_service
from worker.services.telemetry_service import telemetry_service
from worker.utils.scheduler import cycle_scheduler
from worker.utils.telemetry_logger import append_event


class WorkerOrchestrator:
    """Orchestrates independent fixture and highlight pipeline lifecycles."""

    def __init__(self):
        self.is_running = True
        self.cycle_index = self._get_initial_index("cycles", "cycle_index")
        self.fixture_cycle_index = self._get_initial_pipeline_index("fixture_engine", "cycle_index", "FIXTURE_SYNC")
        self.highlight_sweep_index = self._get_initial_pipeline_index("highlight_engine", "sweep_index", "YOUTUBE_SCRAPER")
        self.last_youtube_sync_timestamp: float = 0.0
        self.last_websub_renewal_timestamp: float = 0.0
        self.last_fixture_sync_timestamp: float = 0.0
        self.fixture_task: asyncio.Task | None = None
        self.highlight_task: asyncio.Task | None = None
        self._setup_signals()

    @staticmethod
    def _get_initial_index(key: str, subkey: str) -> int:
        try:
            data = telemetry_service._load_history().get(key, [])
            if data:
                return int(data[0].get(subkey) or data[0].get("id", 0)) % 100000
        except Exception:
            pass
        return 0

    @staticmethod
    def _get_initial_pipeline_index(pipe_key: str, subkey: str, fallback_type: str) -> int:
        try:
            telemetry = telemetry_service._load_history()
            pipelines = telemetry.get("pipelines", {})
            if pipe_key in pipelines:
                return int(pipelines[pipe_key].get(subkey, 0)) % 100000
            cycles = [c for c in telemetry.get("cycles", []) if c.get("pipeline") == fallback_type]
            if cycles:
                return int(cycles[0].get("pipeline_cycle_index") or cycles[0].get("cycle_index", 0)) % 100000
        except Exception:
            pass
        return 0

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
        return await cycle_scheduler.calculate_highlight_dynamic_sleep()

    async def calculate_dynamic_sleep(self) -> tuple[int, str]:
        return await cycle_scheduler.calculate_dynamic_sleep()

    async def run_single_cycle(self, is_full_sync: bool = False):
        self.cycle_index += 1
        logger.info(f"=== Starting Cycle #{self.cycle_index} ===")
        steps_log = []

        now_ts = datetime.now(UTC).timestamp()
        hours_since_fixture = (now_ts - self.last_fixture_sync_timestamp) / 3600.0 if self.last_fixture_sync_timestamp > 0 else 999.0
        should_sync_fixtures = is_full_sync or (self.last_fixture_sync_timestamp == 0.0) or (hours_since_fixture >= 12.0)
        step1_title = "Step 1: Fixture Schedule & Club Sync (12h)" if should_sync_fixtures else "Step 1: Rapid Live Score Ticker (60s)"

        step1_events = []
        try:
            if should_sync_fixtures:
                await match_service.sync_matches(is_full_sync=is_full_sync, log_events=step1_events)
                self.last_fixture_sync_timestamp = now_ts
            else:
                await match_service.sync_live_scores(log_events=step1_events)
            step1_status = "OK"
        except Exception as e:
            append_event(step1_events, f"Error Step 1: {e}", "ERROR", "MATCH_SYNC", "STEP_ERROR")
            step1_status = "ERROR"
        steps_log.append({"title": step1_title, "status": step1_status, "events": step1_events})

        step2_events = []
        try:
            await asyncio.to_thread(maintenance_service.cleanup_old_data, log_events=step2_events)
            step2_status = "OK"
        except Exception as e:
            append_event(step2_events, f"Error Step 2: {e}", "ERROR", "MAINTENANCE", "STEP_ERROR")
            step2_status = "ERROR"
        steps_log.append({"title": "Step 2: Cache Purge & Database Maintenance", "status": step2_status, "events": step2_events})

        step3_events = []
        is_backfill = is_full_sync or ("--force" in sys.argv) or ("--backfill" in sys.argv)
        batch_limit = 100 if is_backfill else 25
        minutes_since_yt = (now_ts - self.last_youtube_sync_timestamp) / 60.0
        should_scrape = is_backfill or (match_service.consume_newly_finished() > 0) or (self.last_youtube_sync_timestamp == 0.0) or (minutes_since_yt >= 2.0)

        if should_scrape:
            try:
                self.last_youtube_sync_timestamp = now_ts
                await asyncio.to_thread(highlight_service.sync_unlinked_highlights, max_batch=batch_limit, log_events=step3_events)
                step3_status = "OK"
            except Exception as e:
                append_event(step3_events, f"Error Step 3: {e}", "ERROR", "YOUTUBE", "STEP_ERROR")
                step3_status = "ERROR"
        else:
            append_event(step3_events, f"YouTube Scraper throttled. Next in {max(0, int(2.0 - minutes_since_yt))}m.", "INFO", "YOUTUBE", "RATE_THROTTLE")
            step3_status = "THROTTLED"
        steps_log.append({"title": "Step 3: YouTube Highlight Scraper", "status": step3_status, "events": step3_events})

        step4_events = []
        hours_standings = (now_ts - standings_service.last_sync_timestamp) / 3600.0
        if is_full_sync or (standings_service.last_sync_timestamp == 0.0) or (hours_standings >= 6.0):
            try:
                await standings_service.sync_standings(force=is_full_sync, log_events=step4_events)
                step4_status = "OK"
            except Exception as e:
                append_event(step4_events, f"Error Step 4: {e}", "ERROR", "STANDINGS", "STEP_ERROR")
                step4_status = "ERROR"
        else:
            step4_status = "SKIPPED"
        steps_log.append({"title": "Step 4: League Standings Sync", "status": step4_status, "events": step4_events})

        if is_full_sync or self.last_websub_renewal_timestamp == 0.0 or (now_ts - self.last_websub_renewal_timestamp) / 3600.0 >= 72.0:
            self.last_websub_renewal_timestamp = now_ts
            asyncio.create_task(websub_subscriber.subscribe_all())

        sleep_sec, sleep_desc = await self.calculate_dynamic_sleep()
        telemetry_service.record_cycle(
            mode="FULL SYNC" if is_full_sync else "REGULAR SYNC",
            next_sleep_str=sleep_desc,
            cycle_index=self.cycle_index,
            steps_log=steps_log,
        )
        return sleep_sec, sleep_desc

    async def run_fixture_loop(self):
        logger.info("[FixtureEngine] Starting independent Fixture & Live Score loop...")
        while self.is_running:
            self.fixture_cycle_index += 1
            now_ts = datetime.now(UTC).timestamp()
            hours_since = (now_ts - self.last_fixture_sync_timestamp) / 3600.0 if self.last_fixture_sync_timestamp > 0 else 999.0
            should_sync = (self.last_fixture_sync_timestamp == 0.0) or (hours_since >= 12.0)
            is_full = should_sync and (self.fixture_cycle_index == 1)

            steps_log = []
            s1_events = []
            title = "Step 1: Fixture Schedule (12h)" if should_sync else "Step 1: Live Scores (60s)"
            try:
                if should_sync:
                    await match_service.sync_matches(is_full_sync=is_full, log_events=s1_events)
                    self.last_fixture_sync_timestamp = now_ts
                else:
                    await match_service.sync_live_scores(log_events=s1_events)
                s1_status = "OK"
            except Exception as e:
                append_event(s1_events, f"Error: {e}", "ERROR", "MATCH_SYNC", "STEP_ERROR")
                s1_status = "ERROR"
            steps_log.append({"title": title, "status": s1_status, "events": s1_events})

            s2_events = []
            try:
                await asyncio.to_thread(maintenance_service.cleanup_old_data, log_events=s2_events)
                s2_status = "OK"
            except Exception as e:
                append_event(s2_events, f"Error: {e}", "ERROR", "MAINTENANCE", "STEP_ERROR")
                s2_status = "ERROR"
            steps_log.append({"title": "Step 2: Maintenance", "status": s2_status, "events": s2_events})

            s4_events = []
            if is_full or (standings_service.last_sync_timestamp == 0.0) or ((now_ts - standings_service.last_sync_timestamp) / 3600.0 >= 6.0):
                try:
                    await standings_service.sync_standings(force=is_full, log_events=s4_events)
                    s4_status = "OK"
                except Exception as e:
                    append_event(s4_events, f"Error: {e}", "ERROR", "STANDINGS", "STEP_ERROR")
                    s4_status = "ERROR"
            else:
                s4_status = "SKIPPED"
            steps_log.append({"title": "Step 4: Standings", "status": s4_status, "events": s4_events})

            sleep_sec, sleep_desc = await self.calculate_dynamic_sleep()
            telemetry_service.record_cycle(
                mode="FULL FIXTURE SYNC" if is_full else "FIXTURE SYNC",
                next_sleep_str=sleep_desc,
                cycle_index=self.fixture_cycle_index,
                steps_log=steps_log,
                pipeline_type="FIXTURE_SYNC",
                pipeline_cycle_index=self.fixture_cycle_index,
            )
            try:
                await asyncio.sleep(sleep_sec)
            except asyncio.CancelledError:
                break

    async def run_highlight_loop(self):
        logger.info("[HighlightEngine] Starting independent YouTube Highlight Scraper loop...")
        while self.is_running:
            self.highlight_sweep_index += 1
            steps_log = []

            now_ts = datetime.now(UTC).timestamp()
            if self.last_websub_renewal_timestamp == 0.0 or (now_ts - self.last_websub_renewal_timestamp) / 3600.0 >= 72.0:
                self.last_websub_renewal_timestamp = now_ts
                asyncio.create_task(websub_subscriber.subscribe_all())

            s3_events = []
            try:
                self.last_youtube_sync_timestamp = now_ts
                await asyncio.to_thread(highlight_service.sync_unlinked_highlights, max_batch=25, log_events=s3_events)
                s3_status = "OK"
            except Exception as e:
                append_event(s3_events, f"Error: {e}", "ERROR", "YOUTUBE", "STEP_ERROR")
                s3_status = "ERROR"
            steps_log.append({"title": "Step 3: YouTube Highlight Scraper", "status": s3_status, "events": s3_events})

            sleep_sec, sleep_desc = await self.calculate_highlight_dynamic_sleep()
            telemetry_service.record_cycle(
                mode="YOUTUBE SWEEP",
                next_sleep_str=sleep_desc,
                cycle_index=self.highlight_sweep_index,
                steps_log=steps_log,
                pipeline_type="YOUTUBE_SCRAPER",
                pipeline_cycle_index=self.highlight_sweep_index,
            )
            try:
                await asyncio.sleep(sleep_sec)
            except asyncio.CancelledError:
                break

    async def start(self):
        is_once = "--once" in sys.argv
        is_force = "--force" in sys.argv
        pipe = "all"
        for arg in sys.argv:
            if arg.startswith("--pipeline="):
                pipe = arg.split("=", 1)[1].lower()
            elif arg in ["--fixture-only", "--fixtures-only"]:
                pipe = "fixture"
            elif arg in ["--highlight-only", "--highlights-only", "--youtube-only"]:
                pipe = "highlight"

        logger.info(f"Football Clips Worker Engine: Mode='{pipe}', Once={is_once}, Force={is_force}")
        try:
            if is_once:
                if pipe == "fixture":
                    await self.run_match_sync_only(is_full_sync=is_force)
                elif pipe == "highlight":
                    await self.run_highlight_sync_only(batch_limit=100 if is_force else 25)
                else:
                    await self.run_single_cycle(is_full_sync=is_force)
                return

            if pipe == "fixture":
                self.fixture_task = asyncio.create_task(self.run_fixture_loop())
                await self.fixture_task
            elif pipe == "highlight":
                self.highlight_task = asyncio.create_task(self.run_highlight_loop())
                await self.highlight_task
            else:
                self.fixture_task = asyncio.create_task(self.run_fixture_loop())
                self.highlight_task = asyncio.create_task(self.run_highlight_loop())
                await asyncio.gather(self.fixture_task, self.highlight_task)
        except asyncio.CancelledError:
            logger.info("Worker orchestrator stopped cleanly.")
        except Exception as e:
            logger.error(f"Fatal worker exception in pipeline {pipe}: {e}")
            telemetry_service.record_crash(f"pipeline_{pipe}", e)
            raise e

    async def run_match_sync_only(self, is_full_sync: bool = False) -> dict[str, Any]:
        self.fixture_cycle_index += 1
        events, status = [], "OK"
        try:
            await match_service.sync_matches(is_full_sync=is_full_sync, log_events=events)
        except Exception as e:
            append_event(events, f"Error: {e}", "ERROR", "MATCH_SYNC", "ERROR")
            status = "ERROR"
        sleep_sec, sleep_desc = await self.calculate_dynamic_sleep()
        telemetry_service.record_cycle(
            mode="FULL FIXTURE SYNC" if is_full_sync else "FIXTURE SYNC",
            next_sleep_str=sleep_desc,
            cycle_index=self.fixture_cycle_index,
            steps_log=[{"title": "Step 1: Match & Live Score Sync", "status": status, "events": events}],
            pipeline_type="FIXTURE_SYNC",
            pipeline_cycle_index=self.fixture_cycle_index,
        )
        return {"status": status, "events": events}

    async def run_highlight_sync_only(self, batch_limit: int = 25) -> dict[str, Any]:
        self.highlight_sweep_index += 1
        events, status = [], "OK"
        try:
            self.last_youtube_sync_timestamp = datetime.now(UTC).timestamp()
            await asyncio.to_thread(highlight_service.sync_unlinked_highlights, max_batch=batch_limit, log_events=events)
        except Exception as e:
            append_event(events, f"Error: {e}", "ERROR", "YOUTUBE", "ERROR")
            status = "ERROR"
        sleep_sec, sleep_desc = await self.calculate_highlight_dynamic_sleep()
        telemetry_service.record_cycle(
            mode="YOUTUBE SWEEP",
            next_sleep_str=sleep_desc,
            cycle_index=self.highlight_sweep_index,
            steps_log=[{"title": "Step 3: YouTube Highlight Scraper", "status": status, "events": events}],
            pipeline_type="YOUTUBE_SCRAPER",
            pipeline_cycle_index=self.highlight_sweep_index,
        )
        return {"status": status, "events": events}
