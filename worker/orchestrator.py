"""Decoupled dual-engine worker orchestrator.

Coordinates autonomous fixture sync and highlight scraping loops.
"""

import asyncio
from datetime import UTC, datetime
import signal
import sys
from typing import Any

from loguru import logger

from worker.scripts.subscribe_youtube_websub import websub_subscriber
from worker.services.highlight_service import highlight_service
from worker.services.match_service import match_service
from worker.services.pipeline_steps import (
    run_step1_fixture_or_live,
    run_step2_maintenance,
    run_step3_highlights_and_shorts,
    run_step4_standings,
)
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
        self.last_shorts_sync_timestamp: float = 0.0
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
        now_ts = datetime.now(UTC).timestamp()
        hours_since_fixture = (now_ts - self.last_fixture_sync_timestamp) / 3600.0 if self.last_fixture_sync_timestamp > 0 else 999.0
        should_sync_fixtures = is_full_sync or (self.last_fixture_sync_timestamp == 0.0) or (hours_since_fixture >= 12.0)

        step1 = await run_step1_fixture_or_live(should_sync_fixtures, is_full_sync, short_titles=False)
        if should_sync_fixtures and step1["status"] == "OK":
            self.last_fixture_sync_timestamp = now_ts

        step2 = await run_step2_maintenance(short_title=False)

        is_backfill = is_full_sync or ("--force" in sys.argv) or ("--backfill" in sys.argv)
        batch_limit = 100 if is_backfill else 25
        minutes_since_yt = (now_ts - self.last_youtube_sync_timestamp) / 60.0
        should_scrape = is_backfill or (match_service.consume_newly_finished() > 0) or (self.last_youtube_sync_timestamp == 0.0) or (minutes_since_yt >= 2.0)

        if should_scrape:
            self.last_youtube_sync_timestamp = now_ts
            step3 = await run_step3_highlights_and_shorts(batch_limit, include_shorts=False)
        else:
            s3_events: list[dict[str, Any]] = []
            append_event(s3_events, f"YouTube Scraper throttled. Next in {max(0, int(2.0 - minutes_since_yt))}m.", "INFO", "YOUTUBE", "RATE_THROTTLE")
            step3 = {"title": "Step 3: YouTube Highlight Scraper", "status": "THROTTLED", "events": s3_events}

        step4 = await run_step4_standings(now_ts, force=is_full_sync, short_title=False)

        if is_full_sync or self.last_websub_renewal_timestamp == 0.0 or (now_ts - self.last_websub_renewal_timestamp) / 3600.0 >= 72.0:
            self.last_websub_renewal_timestamp = now_ts
            asyncio.create_task(websub_subscriber.subscribe_all())

        sleep_sec, sleep_desc = await self.calculate_dynamic_sleep()
        telemetry_service.record_cycle(
            mode="FULL SYNC" if is_full_sync else "REGULAR SYNC",
            next_sleep_str=sleep_desc,
            cycle_index=self.cycle_index,
            steps_log=[step1, step2, step3, step4],
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

            s1 = await run_step1_fixture_or_live(should_sync, is_full, short_titles=True)
            if should_sync and s1["status"] == "OK":
                self.last_fixture_sync_timestamp = now_ts
            s2 = await run_step2_maintenance(short_title=True)
            s4 = await run_step4_standings(now_ts, force=is_full, short_title=True)

            sleep_sec, sleep_desc = await self.calculate_dynamic_sleep()
            telemetry_service.record_cycle(
                mode="FULL FIXTURE SYNC" if is_full else "FIXTURE SYNC",
                next_sleep_str=sleep_desc,
                cycle_index=self.fixture_cycle_index,
                steps_log=[s1, s2, s4],
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
            now_ts = datetime.now(UTC).timestamp()
            if self.last_websub_renewal_timestamp == 0.0 or (now_ts - self.last_websub_renewal_timestamp) / 3600.0 >= 72.0:
                self.last_websub_renewal_timestamp = now_ts
                asyncio.create_task(websub_subscriber.subscribe_all())

            self.last_youtube_sync_timestamp = now_ts
            include_shorts = (self.last_shorts_sync_timestamp == 0.0) or ((now_ts - self.last_shorts_sync_timestamp) >= 7200.0)
            if include_shorts:
                self.last_shorts_sync_timestamp = now_ts
            s3 = await run_step3_highlights_and_shorts(batch_limit=25, include_shorts=include_shorts)
            s3["title"] = "Step 3: YouTube Highlight & Shorts Scraper"

            sleep_sec, sleep_desc = await self.calculate_highlight_dynamic_sleep()
            telemetry_service.record_cycle(
                mode="YOUTUBE SWEEP",
                next_sleep_str=sleep_desc,
                cycle_index=self.highlight_sweep_index,
                steps_log=[s3],
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
        _, sleep_desc = await self.calculate_dynamic_sleep()
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
        _, sleep_desc = await self.calculate_highlight_dynamic_sleep()
        telemetry_service.record_cycle(
            mode="YOUTUBE SWEEP",
            next_sleep_str=sleep_desc,
            cycle_index=self.highlight_sweep_index,
            steps_log=[{"title": "Step 3: YouTube Highlight Scraper", "status": status, "events": events}],
            pipeline_type="YOUTUBE_SCRAPER",
            pipeline_cycle_index=self.highlight_sweep_index,
        )
        return {"status": status, "events": events}
