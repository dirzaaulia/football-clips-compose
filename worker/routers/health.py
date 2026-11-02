import time
from datetime import UTC, datetime
from typing import Any

from fastapi import APIRouter
from fastapi.responses import PlainTextResponse, RedirectResponse
from loguru import logger

from worker.clients.database_client import db_client
from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings

router = APIRouter(tags=["Health & Telemetry"])

_cached_metrics_text: str = ""
_last_metrics_fetch_time: float = 0.0


@router.get("/")
@router.get("/dashboard")
async def dashboard_view():
    return RedirectResponse("https://fc.dirzaaulia.com/telemetry", status_code=307)


@router.get("/livez")
async def liveness_probe():
    """Kubernetes & Cloud Run liveness probe. Fast 200 OK indicating container process is alive."""
    return {"status": "alive", "timestamp": datetime.now(UTC).isoformat()}


@router.get("/readyz")
async def readiness_probe():
    """Deep readiness probe verifying critical dependencies (PostgREST, AI Gateway, Quota)."""
    checks = {}
    is_ready = True

    # 1. Database check
    if db_client:
        try:
            res = db_client.table("competitions").select("id").limit(1).execute()
            checks["database"] = "ok" if res.data is not None else "degraded"
        except Exception as e:
            checks["database"] = f"error: {str(e)[:50]}"
            is_ready = False
    else:
        checks["database"] = "not_configured"
        is_ready = False

    # 2. AI Gateway configuration check
    checks["ai_gateway"] = "configured" if settings.ai_gateway_url else "disabled"

    # 3. YouTube Quota status
    try:
        q_stat = youtube_client.get_quota_status()
        checks["youtube_quota"] = {
            "hits_today": q_stat.get("hits_today", 0),
            "remaining": q_stat.get("remaining", 0),
        }
    except Exception:
        checks["youtube_quota"] = "unknown"

    return {
        "status": "ready" if is_ready else "degraded",
        "ready": is_ready,
        "timestamp": datetime.now(UTC).isoformat(),
        "checks": checks,
    }


@router.get("/health")
async def health_check():
    return {
        "status": "ready",
        "service": "football-clips-worker-http",
        "timestamp": datetime.now(UTC).isoformat(),
        "pipeline_status_url": "https://api.fc.dirzaaulia.com/api/status/v1",
    }


@router.get("/status")
async def get_status():
    from worker.server import orchestrator

    return {
        "cycle_index": orchestrator.cycle_index,
        "fixture_cycle_index": orchestrator.fixture_cycle_index,
        "highlight_sweep_index": orchestrator.highlight_sweep_index,
        "provider": settings.match_data_provider,
        "last_youtube_sync_timestamp": orchestrator.last_youtube_sync_timestamp,
        "last_websub_renewal_timestamp": orchestrator.last_websub_renewal_timestamp,
        "timestamp": datetime.now(UTC).isoformat(),
    }


@router.get("/metrics")
async def get_metrics():
    global _cached_metrics_text, _last_metrics_fetch_time
    now = time.monotonic()
    if _cached_metrics_text and (now - _last_metrics_fetch_time < 60.0):
        return PlainTextResponse(_cached_metrics_text, media_type="text/plain; version=0.0.4")

    try:
        quota_data = youtube_client.get_quota_status()
        hits_today = quota_data.get("hits_today", 0)
        remaining = quota_data.get("remaining", settings.max_quota_per_day)
    except Exception:
        hits_today, remaining = 0, settings.max_quota_per_day

    from worker.server import orchestrator

    metrics = [
        "# HELP fc_worker_cycle_index Current iteration index of the primary synchronization loop",
        "# TYPE fc_worker_cycle_index counter",
        f"fc_worker_cycle_index {orchestrator.cycle_index}",
        "# HELP fc_youtube_quota_hits_today YouTube Data API quota units consumed today",
        "# TYPE fc_youtube_quota_hits_today gauge",
        f"fc_youtube_quota_hits_today {hits_today}",
        "# HELP fc_youtube_quota_remaining YouTube Data API quota units remaining today",
        "# TYPE fc_youtube_quota_remaining gauge",
        f"fc_youtube_quota_remaining {remaining}",
    ]
    _cached_metrics_text = "\n".join(metrics) + "\n"
    _last_metrics_fetch_time = now
    return PlainTextResponse(_cached_metrics_text, media_type="text/plain; version=0.0.4")
