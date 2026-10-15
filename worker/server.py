import asyncio
import os
import sys
from contextlib import asynccontextmanager
from datetime import UTC, datetime
from typing import Any

from fastapi import BackgroundTasks, FastAPI, Query, Request
from fastapi.responses import PlainTextResponse
from loguru import logger
from worker.config.settings import settings
from worker.main import WorkerOrchestrator
from worker.services.highlight_service import highlight_service

orchestrator = WorkerOrchestrator()
_background_worker_task: asyncio.Task | None = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global _background_worker_task
    logger.info("[CloudRun Server] Starting FootballClips Worker Service...")
    mode = os.getenv("WORKER_MODE", "continuous").lower()

    if mode == "continuous":
        logger.info("[CloudRun Server] Launching background continuous orchestrator task...")
        _background_worker_task = asyncio.create_task(orchestrator.start())
    else:
        logger.info("[CloudRun Server] Running in on-demand HTTP mode (Cloud Scheduler / Webhooks).")

    yield

    logger.info("[CloudRun Server] Shutting down Worker Service...")
    orchestrator.is_running = False
    if _background_worker_task and not _background_worker_task.done():
        _background_worker_task.cancel()
        try:
            await _background_worker_task
        except asyncio.CancelledError:
            pass


app = FastAPI(
    title="FootballClips Worker Service",
    description="Asynchronous football fixture, live score, and highlight synchronization engine",
    version="2.0.0",
    lifespan=lifespan,
)


@app.get("/")
@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "football-clips-worker",
        "timestamp": datetime.now(UTC).isoformat(),
        "cycle_index": orchestrator.cycle_index,
        "is_running": orchestrator.is_running,
        "provider": settings.match_data_provider,
        "target_competitions": settings.target_competitions,
    }


@app.get("/status")
async def get_status():
    return {
        "cycle_index": orchestrator.cycle_index,
        "provider": settings.match_data_provider,
        "last_youtube_sync_timestamp": orchestrator.last_youtube_sync_timestamp,
        "last_websub_renewal_timestamp": orchestrator.last_websub_renewal_timestamp,
        "timestamp": datetime.now(UTC).isoformat(),
    }


@app.post("/run-cycle")
async def trigger_cycle(
    background_tasks: BackgroundTasks,
    force_full: bool = Query(default=False, description="Force full historical sync cycle"),
    async_execution: bool = Query(default=False, description="Run cycle in background task instead of awaiting"),
) -> dict[str, Any]:
    """
    Trigger a single synchronization cycle on demand.
    Ideal for Cloud Scheduler, webhooks, or manual administrative triggers.
    """
    if async_execution:
        background_tasks.add_task(orchestrator.run_single_cycle, is_full_sync=force_full)
        return {
            "status": "QUEUED",
            "message": f"Cycle triggered in background (force_full={force_full})",
            "timestamp": datetime.now(UTC).isoformat(),
        }

    logger.info(f"[CloudRun HTTP Trigger] Executing cycle on demand (force_full={force_full})...")
    sleep_sec, sleep_desc = await orchestrator.run_single_cycle(is_full_sync=force_full)
    return {
        "status": "COMPLETED",
        "cycle_index": orchestrator.cycle_index,
        "next_sleep_interval_sec": sleep_sec,
        "next_sleep_desc": sleep_desc,
        "timestamp": datetime.now(UTC).isoformat(),
    }


@app.get("/websub")
async def websub_handshake(
    hub_mode: str | None = Query(default=None, alias="hub.mode"),
    hub_challenge: str | None = Query(default=None, alias="hub.challenge"),
    hub_topic: str | None = Query(default=None, alias="hub.topic"),
):
    """Google PubSubHubbub Handshake Verification."""
    if hub_challenge and hub_mode in ["subscribe", "unsubscribe"]:
        logger.info(f"[WebSub Handshake] Confirmed {hub_mode} for topic: {hub_topic}")
        from fastapi.responses import PlainTextResponse
        return PlainTextResponse(content=hub_challenge, status_code=200)

    from fastapi.responses import PlainTextResponse
    return PlainTextResponse(content="Invalid handshake request", status_code=400)


@app.post("/websub")
async def websub_notification(
    request: Request,
    background_tasks: BackgroundTasks
):
    """Receives YouTube Atom XML real-time upload notification and triggers highlight sync."""
    import re
    from fastapi.responses import PlainTextResponse

    try:
        body = await request.body()
        xml = body.decode("utf-8", errors="ignore")
        if not xml or "<entry>" not in xml:
            return PlainTextResponse("OK", status_code=200)

        vid_match = re.search(r"<(?:yt:)?videoId[^>]*>(.*?)</(?:yt:)?videoId>", xml, re.IGNORECASE)
        title_match = re.search(r"<title[^>]*>(.*?)</title>", xml, re.IGNORECASE)
        chan_match = re.search(r"<(?:yt:)?channelId[^>]*>(.*?)</(?:yt:)?channelId>", xml, re.IGNORECASE)

        video_id = vid_match.group(1).strip() if vid_match else None
        title = title_match.group(1).strip() if title_match else None
        channel_id = chan_match.group(1).strip() if chan_match else None

        logger.info(f"[WebSub Push] Received upload notification: {video_id} ('{title}') from channel {channel_id}")

        # Attempt direct event-driven match linking first (<100ms, 0 quota)
        linked = False
        if video_id and title:
            linked = highlight_service.link_websub_video(
                video_id=video_id,
                title=title,
                channel_id=channel_id or ""
            )

        if linked:
            logger.info(f"[WebSub Push] Direct match linking succeeded for {video_id} ('{title}')!")
        else:
            # Fallback to general highlight sweep in background
            logger.info(f"[WebSub Push] Direct link not resolved immediately. Enqueuing background sweep.")
            background_tasks.add_task(highlight_service.sync_unlinked_highlights, max_batch=10)

        return PlainTextResponse("Accepted", status_code=200)
    except Exception as e:
        logger.error(f"[WebSub Push] Error processing notification: {e}")
        return PlainTextResponse("Error", status_code=200)

