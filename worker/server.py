import asyncio
import os
import sys
from contextlib import asynccontextmanager
from datetime import UTC, datetime
from typing import Any

from fastapi import BackgroundTasks, FastAPI, Query, Request
from fastapi.middleware.cors import CORSMiddleware
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

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


_cached_metrics_text: str = ""
_last_metrics_fetch_time: float = 0.0


@app.get("/")
@app.get("/dashboard")
async def dashboard_view():
    from fastapi.responses import RedirectResponse

    return RedirectResponse("https://fc.dirzaaulia.com/telemetry", status_code=307)


@app.get("/health")
async def health_check():
    return {
        "status": "ready",
        "service": "football-clips-worker-http",
        "timestamp": datetime.now(UTC).isoformat(),
        "pipeline_status_url": "https://api.fc.dirzaaulia.com/api/status/v1",
    }


@app.get("/status")
async def get_status():
    return {
        "cycle_index": orchestrator.cycle_index,
        "fixture_cycle_index": orchestrator.fixture_cycle_index,
        "highlight_sweep_index": orchestrator.highlight_sweep_index,
        "provider": settings.match_data_provider,
        "last_youtube_sync_timestamp": orchestrator.last_youtube_sync_timestamp,
        "last_websub_renewal_timestamp": orchestrator.last_websub_renewal_timestamp,
        "timestamp": datetime.now(UTC).isoformat(),
    }


@app.get("/metrics")
async def get_metrics():
    """Prometheus / OpenMetrics APM telemetry endpoint (60s in-memory cache)."""
    global _cached_metrics_text, _last_metrics_fetch_time
    import time
    from fastapi.responses import PlainTextResponse

    now = time.monotonic()
    if _cached_metrics_text and (now - _last_metrics_fetch_time < 60.0):
        return PlainTextResponse(content=_cached_metrics_text, media_type="text/plain; version=0.0.4")

    try:
        from worker.clients.supabase_client import supabase_client
        from worker.clients.youtube_client import youtube_client

        # Accurate live matches count query
        live_res = supabase_client.table("matches").select("id", count="exact").in_("status", ["IN_PLAY", "PAUSED", "LIVE"]).execute()
        live_matches = live_res.count if live_res.count is not None else len(live_res.data or [])

        # Total matches count query
        total_res = supabase_client.table("matches").select("id", count="exact").execute()
        total_matches = total_res.count if total_res.count is not None else len(total_res.data or [])

        # Finished matches count query
        fin_res = supabase_client.table("matches").select("id", count="exact").eq("status", "FINISHED").execute()
        finished_matches = fin_res.count if fin_res.count is not None else len(fin_res.data or [])

        # Unlinked finished matches count query
        unlinked_res = supabase_client.table("matches").select("id", count="exact").eq("status", "FINISHED").is_("highlight_video_id", "null").execute()
        unlinked_finished = unlinked_res.count if unlinked_res.count is not None else len(unlinked_res.data or [])
        highlight_linked = max(0, finished_matches - unlinked_finished)

        # YouTube quota telemetry
        quota_status = youtube_client.get_quota_status()
        quota_hits = quota_status.get("hits_today", 0)
        quota_remaining = quota_status["remaining"]
    except Exception:
        return PlainTextResponse("# Metrics temporarily unavailable; no synthetic values emitted", status_code=503)

    lines = [
        "# HELP footballclips_worker_cycle_index Current cycle sequence index",
        "# TYPE footballclips_worker_cycle_index counter",
        f"footballclips_worker_cycle_index {orchestrator.cycle_index}",
        "",
        "# HELP footballclips_fixture_cycle_index Fixture and live match engine cycle index",
        "# TYPE footballclips_fixture_cycle_index counter",
        f"footballclips_fixture_cycle_index {orchestrator.fixture_cycle_index}",
        "",
        "# HELP footballclips_highlight_sweep_index YouTube highlight scraper sweep index",
        "# TYPE footballclips_highlight_sweep_index counter",
        f"footballclips_highlight_sweep_index {orchestrator.highlight_sweep_index}",
        "",
        "# HELP footballclips_worker_is_running Worker orchestrator status",
        "# TYPE footballclips_worker_is_running gauge",
        f"footballclips_worker_is_running {1 if orchestrator.is_running else 0}",
        "",
        "# HELP footballclips_matches_total Total matches tracked in database",
        "# TYPE footballclips_matches_total gauge",
        f"footballclips_matches_total {total_matches}",
        "",
        "# HELP footballclips_matches_live Live matches in play or paused",
        "# TYPE footballclips_matches_live gauge",
        f"footballclips_matches_live {live_matches}",
        "",
        "# HELP footballclips_matches_finished Completed matches count",
        "# TYPE footballclips_matches_finished gauge",
        f"footballclips_matches_finished {finished_matches}",
        "",
        "# HELP footballclips_highlights_linked Matches with verified video highlight",
        "# TYPE footballclips_highlights_linked gauge",
        f"footballclips_highlights_linked {highlight_linked}",
        "",
        "# HELP footballclips_highlights_unlinked Finished matches awaiting highlight",
        "# TYPE footballclips_highlights_unlinked gauge",
        f"footballclips_highlights_unlinked {unlinked_finished}",
        "",
        "# HELP footballclips_youtube_quota_hits YouTube Data API v3 units consumed today",
        "# TYPE footballclips_youtube_quota_hits gauge",
        f"footballclips_youtube_quota_hits {quota_hits}",
        "",
        "# HELP footballclips_youtube_quota_remaining YouTube Data API v3 units remaining today",
        "# TYPE footballclips_youtube_quota_remaining gauge",
        f"footballclips_youtube_quota_remaining {quota_remaining}",
        ""
    ]
    _cached_metrics_text = "\n".join(lines)
    _last_metrics_fetch_time = now
    return PlainTextResponse(content=_cached_metrics_text, media_type="text/plain; version=0.0.4")


@app.post("/websub/renew")
async def renew_websub_subscriptions():
    """Batch renew all official football club and league WebSub subscriptions."""
    from worker.scripts.subscribe_youtube_websub import websub_subscriber
    logger.info("[WebSub Serverless] Starting scheduled batch subscription renewal...")
    await websub_subscriber.subscribe_all()
    return {
        "status": "QUEUED",
        "message": "Durable subscription coordination queued; actual confirmations are recorded separately",
        "timestamp": datetime.now(UTC).isoformat(),
    }


@app.post("/sync/matches")
async def sync_matches_endpoint(
    background_tasks: BackgroundTasks,
    force_full: bool = Query(default=False, description="Force full historical sync"),
    async_execution: bool = Query(default=False, description="Run in background task"),
) -> dict[str, Any]:
    """
    Decoupled endpoint for Step 1: Live & Upcoming match sync.
    Runs separately from YouTube highlight scraper without blocking.
    """
    if async_execution and not os.getenv("FC_DISCOVERY_URL"):
        background_tasks.add_task(orchestrator.run_match_sync_only, is_full_sync=force_full)
        return {
            "status": "QUEUED",
            "message": f"Match sync queued in background (force_full={force_full})",
            "timestamp": datetime.now(UTC).isoformat(),
        }

    res = await orchestrator.run_match_sync_only(is_full_sync=force_full)
    return {
        "status": res["status"],
        "pipeline": "MATCH_AND_LIVE_SCORE_SYNC",
        "event_count": len(res["events"]),
        "timestamp": datetime.now(UTC).isoformat(),
    }


@app.post("/sync/highlights")
async def sync_highlights_endpoint(
    background_tasks: BackgroundTasks,
    max_batch: int = Query(default=25, description="Maximum matches to process"),
    async_execution: bool = Query(default=False, description="Run in background task"),
) -> dict[str, Any]:
    """
    Decoupled endpoint for Step 3: YouTube Official Highlight Scraping & Quota Maximizer.
    Runs independently of live matches to utilize daily quota on finished matches.
    """
    if async_execution and not os.getenv("FC_DISCOVERY_URL"):
        background_tasks.add_task(orchestrator.run_highlight_sync_only, batch_limit=max_batch)
        return {
            "status": "QUEUED",
            "message": f"Highlight scraper queued in background (max_batch={max_batch})",
            "timestamp": datetime.now(UTC).isoformat(),
        }

    res = await orchestrator.run_highlight_sync_only(batch_limit=max_batch)
    return {
        "status": res["status"],
        "pipeline": "YOUTUBE_HIGHLIGHT_SCRAPER",
        "event_count": len(res["events"]),
        "timestamp": datetime.now(UTC).isoformat(),
    }


@app.post("/run-cycle")
async def trigger_cycle(
    background_tasks: BackgroundTasks,
    force_full: bool = Query(default=False, description="Force full historical sync cycle"),
    async_execution: bool = Query(default=False, description="Run cycle in background task instead of awaiting"),
) -> dict[str, Any]:
    """
    Trigger a single full synchronization cycle on demand (all 4 steps).
    Preserves backward compatibility for Cloud Scheduler, webhooks, and legacy orchestrator triggers.
    """
    if async_execution and not os.getenv("FC_DISCOVERY_URL"):
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
        return PlainTextResponse(content=hub_challenge, status_code=200)

    return PlainTextResponse(content="Invalid handshake request", status_code=400)


@app.post("/websub")
async def websub_notification(
    request: Request,
    background_tasks: BackgroundTasks,
):
    """Receives YouTube Atom XML real-time upload notification and triggers highlight sync."""
    import re

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

        if video_id and title:
            background_tasks.add_task(highlight_service.link_websub_video, video_id, title, channel_id or "")
        else:
            background_tasks.add_task(highlight_service.sync_unlinked_highlights, max_batch=10)

        return PlainTextResponse("Accepted", status_code=200)
    except Exception as e:
        logger.error(f"[WebSub Push] Error processing notification: {e}")
        return PlainTextResponse("Error", status_code=200)


from pydantic import BaseModel


class WebNotificationSubscriptionRequest(BaseModel):
    token: str
    club_ids: list[Any] = []
    action: str = "subscribe"
    platform: str = "web"
    device_name: str | None = "Web Browser"


@app.post("/notifications/subscribe")
async def handle_web_subscription(req: WebNotificationSubscriptionRequest):
    """
    Subscribes or unsubscribes a web client's FCM token to club topics via Firebase Admin SDK.
    Also registers the web device in audience_service.
    """
    import firebase_admin
    from firebase_admin import messaging
    from worker.services.audience_service import audience_service

    if not req.token or not req.club_ids:
        return {"status": "error", "message": "Missing token or club_ids"}

    results = {}
    for cid in req.club_ids:
        if not cid:
            continue
        topic = f"club_{cid}"
        try:
            if req.action == "subscribe":
                resp = messaging.subscribe_to_topic([req.token], topic)
                results[str(cid)] = "subscribed" if resp.success_count > 0 else "failed"
            else:
                resp = messaging.unsubscribe_from_topic([req.token], topic)
                results[str(cid)] = "unsubscribed" if resp.success_count > 0 else "failed"
        except Exception as e:
            logger.warning(f"[FCM Web Subscribe] Error on topic {topic}: {e}")
            results[str(cid)] = f"error: {e}"

    # Register/update web device in audience service
    try:
        audience_service.register_web_device(
            token=req.token,
            club_ids=req.club_ids if req.action == "subscribe" else [],
            device_name=req.device_name or "Web Browser"
        )
    except Exception as e:
        logger.debug(f"[AudienceService] Web device registration deferred: {e}")

    logger.info(f"[Web Notification] Token registered for {len(req.club_ids)} clubs (action={req.action}).")
    return {"status": "ok", "action": req.action, "results": results}


@app.get("/broadcast/rights")
async def get_broadcast_rights(
    competition: str = Query(default="PL", description="Competition code (e.g. PL, CL, SA, BL1, etc.)"),
    country: str = Query(default="US", description="ISO-3166-1 alpha-2 country code (e.g. US, GB, ID, ES, DE, FR)")
):
    """
    Returns official live stream broadcast rights holder and legal direct stream redirection URL
    for any country on Earth.
    """
    from worker.services.broadcast_service import broadcast_rights_service
    return broadcast_rights_service.resolve_broadcaster(competition_id=competition, country_code=country)


@app.get("/broadcast/directory")
async def get_broadcast_directory():
    """
    Returns full worldwide broadcast rights directory covering 195+ countries across all 17 competitions.
    """
    from worker.services.broadcast_service import broadcast_rights_service
    return broadcast_rights_service.generate_full_world_directory()

