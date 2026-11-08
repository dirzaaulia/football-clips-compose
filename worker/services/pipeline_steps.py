"""Reusable pipeline step runners for WorkerOrchestrator."""

import asyncio
from typing import Any

from worker.services.highlight_service import highlight_service
from worker.services.maintenance_service import maintenance_service
from worker.services.match_service import match_service
from worker.services.shorts_ingestion_service import shorts_service
from worker.services.standings_service import standings_service
from worker.utils.telemetry_logger import append_event


async def run_step1_fixture_or_live(
    should_sync_fixtures: bool,
    is_full_sync: bool,
    short_titles: bool = False,
) -> dict[str, Any]:
    if should_sync_fixtures:
        title = "Step 1: Fixture Schedule (12h)" if short_titles else "Step 1: Fixture Schedule & Club Sync (12h)"
    else:
        title = "Step 1: Live Scores (60s)" if short_titles else "Step 1: Rapid Live Score Ticker (60s)"

    events: list[dict[str, Any]] = []
    try:
        if should_sync_fixtures:
            await match_service.sync_matches(is_full_sync=is_full_sync, log_events=events)
        else:
            await match_service.sync_live_scores(log_events=events)
        status = "OK"
    except Exception as e:
        append_event(events, f"Error Step 1: {e}", "ERROR", "MATCH_SYNC", "STEP_ERROR")
        status = "ERROR"
    return {"title": title, "status": status, "events": events}


async def run_step2_maintenance(short_title: bool = False) -> dict[str, Any]:
    title = "Step 2: Maintenance" if short_title else "Step 2: Cache Purge & Database Maintenance"
    events: list[dict[str, Any]] = []
    try:
        await asyncio.to_thread(maintenance_service.cleanup_old_data, log_events=events)
        status = "OK"
    except Exception as e:
        append_event(events, f"Error Step 2: {e}", "ERROR", "MAINTENANCE", "STEP_ERROR")
        status = "ERROR"
    return {"title": title, "status": status, "events": events}


async def run_step3_highlights_and_shorts(
    batch_limit: int,
    include_shorts: bool = False,
) -> dict[str, Any]:
    title = "Step 3: YouTube Highlight & Shorts Scraper" if include_shorts else "Step 3: YouTube Highlight Scraper"
    events: list[dict[str, Any]] = []
    try:
        await asyncio.to_thread(highlight_service.sync_unlinked_highlights, max_batch=batch_limit, log_events=events)
        if include_shorts:
            await asyncio.to_thread(shorts_service.ingest_recent_shorts, max_clubs=25, log_events=events)
        status = "OK"
    except Exception as e:
        append_event(events, f"Error Step 3: {e}", "ERROR", "YOUTUBE", "STEP_ERROR")
        status = "ERROR"
    return {"title": title, "status": status, "events": events}


async def run_step4_standings(
    now_ts: float,
    force: bool = False,
    short_title: bool = False,
) -> dict[str, Any]:
    title = "Step 4: Standings" if short_title else "Step 4: League Standings Sync"
    events: list[dict[str, Any]] = []
    hours_standings = (now_ts - standings_service.last_sync_timestamp) / 3600.0
    if force or (standings_service.last_sync_timestamp == 0.0) or (hours_standings >= 6.0):
        try:
            await standings_service.sync_standings(force=force, log_events=events)
            status = "OK"
        except Exception as e:
            append_event(events, f"Error Step 4: {e}", "ERROR", "STANDINGS", "STEP_ERROR")
            status = "ERROR"
    else:
        status = "SKIPPED"
    return {"title": title, "status": status, "events": events}
