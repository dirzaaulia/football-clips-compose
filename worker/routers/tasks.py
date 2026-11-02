import asyncio
import os
import uuid
from datetime import UTC, datetime
from typing import Any

from fastapi import APIRouter, BackgroundTasks, Query, Request
from loguru import logger
from pydantic import BaseModel

from worker.clients.database_client import db_client
from worker.services.highlight_service import highlight_service
from worker.services.telemetry_service import telemetry_service

router = APIRouter(tags=["Background Tasks & Sync"])


class ProcessHighlightTaskPayload(BaseModel):
    match_id: int | str
    home: str = ""
    away: str = ""
    competition: str = ""
    dispatched_at: float | None = None


class WebNotificationSubscriptionRequest(BaseModel):
    token: str
    club_ids: list[Any] = []
    action: str = "subscribe"
    platform: str = "web"
    device_name: str | None = "Web Browser"


@router.post("/sync/matches")
async def sync_matches_endpoint(
    background_tasks: BackgroundTasks,
    force_full: bool = Query(default=False, description="Force full historical sync"),
    async_execution: bool = Query(default=False, description="Run in background task"),
) -> dict[str, Any]:
    from worker.server import orchestrator

    if async_execution:
        background_tasks.add_task(orchestrator.run_match_sync_only, is_full_sync=force_full)
        return {"status": "QUEUED", "message": "Match sync queued in background", "timestamp": datetime.now(UTC).isoformat()}

    res = await orchestrator.run_match_sync_only(is_full_sync=force_full)
    return {"status": res["status"], "pipeline": "MATCH_AND_LIVE_SCORE_SYNC", "event_count": len(res["events"]), "timestamp": datetime.now(UTC).isoformat()}


@router.post("/sync/highlights")
async def sync_highlights_endpoint(
    background_tasks: BackgroundTasks,
    max_batch: int = Query(default=25, description="Maximum matches to process"),
    async_execution: bool = Query(default=False, description="Run in background task"),
) -> dict[str, Any]:
    from worker.server import orchestrator

    if async_execution:
        background_tasks.add_task(orchestrator.run_highlight_sync_only, batch_limit=max_batch)
        return {"status": "QUEUED", "message": f"Highlight scraper queued (batch={max_batch})", "timestamp": datetime.now(UTC).isoformat()}

    res = await orchestrator.run_highlight_sync_only(batch_limit=max_batch)
    return {"status": res["status"], "pipeline": "YOUTUBE_HIGHLIGHT_SCRAPER", "event_count": len(res["events"]), "timestamp": datetime.now(UTC).isoformat()}


@router.post("/tasks/process-highlight")
async def process_highlight_task(
    payload: ProcessHighlightTaskPayload,
    request: Request,
) -> dict[str, Any]:
    internal_token = request.headers.get("X-FC-Internal")
    expected_token = os.getenv("FC_INTERNAL_TOKEN", "NGnR7DZlXCQyDBHecirt7SER2Flryb2HTuY5-ETTkUnsag28mWDtLhqgMJbr27dm")
    if internal_token and internal_token != expected_token:
        logger.warning("[CloudTasks Handler] Header token mismatch on task invocation.")

    logger.info(f"[CloudTasks Handler] Processing highlight task for match {payload.match_id}")
    log_events: list = []

    loop = asyncio.get_running_loop()
    result = await loop.run_in_executor(None, highlight_service.process_single_match_highlight, payload.match_id, log_events)

    try:
        match_idx = int(payload.match_id) if str(payload.match_id).isdigit() else 1
        telemetry_service.record_cycle(
            mode="TASK_EXECUTION",
            next_sleep_str="0s",
            cycle_index=match_idx,
            steps_log=log_events,
            pipeline_type="YOUTUBE_SCRAPER",
        )
    except Exception as e:
        logger.warning(f"Could not record task cycle to telemetry: {e}")

    return {
        "status": result.get("status", "COMPLETED"),
        "match_id": payload.match_id,
        "result": result,
        "event_count": len(log_events),
        "timestamp": datetime.now(UTC).isoformat(),
    }


@router.post("/run-cycle")
async def trigger_cycle(
    background_tasks: BackgroundTasks,
    pipeline: str = Query(default="all", description="Target pipeline: 'all', 'fixture', or 'highlight'"),
    force_full: bool = Query(default=False, description="Force full historical sync cycle"),
    async_execution: bool = Query(default=False, description="Run cycle in background"),
) -> dict[str, Any]:
    from worker.server import orchestrator

    target = pipeline.lower().strip()
    if async_execution:
        if target == "fixture":
            background_tasks.add_task(orchestrator.run_match_sync_only, is_full_sync=force_full)
        elif target == "highlight":
            background_tasks.add_task(orchestrator.run_highlight_sync_only)
        else:
            background_tasks.add_task(orchestrator.run_single_cycle, is_full_sync=force_full)
        return {"status": "QUEUED", "pipeline": target, "message": "Cycle triggered in background", "timestamp": datetime.now(UTC).isoformat()}

    if target == "fixture":
        res = await orchestrator.run_match_sync_only(is_full_sync=force_full)
        return {"status": "COMPLETED", "pipeline": "fixture", "result": res, "timestamp": datetime.now(UTC).isoformat()}
    elif target == "highlight":
        res = await orchestrator.run_highlight_sync_only()
        return {"status": "COMPLETED", "pipeline": "highlight", "result": res, "timestamp": datetime.now(UTC).isoformat()}
    else:
        sleep_sec, sleep_desc = await orchestrator.run_single_cycle(is_full_sync=force_full)
        return {
            "status": "COMPLETED",
            "pipeline": "all",
            "cycle_index": orchestrator.cycle_index,
            "next_sleep_interval_sec": sleep_sec,
            "next_sleep_desc": sleep_desc,
            "timestamp": datetime.now(UTC).isoformat(),
        }


@router.post("/reset-telemetry")
async def reset_telemetry_endpoint() -> dict[str, Any]:
    from worker.server import orchestrator

    orchestrator.cycle_index = 0
    orchestrator.fixture_cycle_index = 0
    orchestrator.highlight_sweep_index = 0
    telemetry_service.reset_telemetry()
    return {"status": "RESET", "message": "All pipeline cycles and telemetry reset to zero.", "timestamp": datetime.now(UTC).isoformat()}


@router.post("/notifications/subscribe")
async def handle_web_subscription(req: WebNotificationSubscriptionRequest):
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
            results[str(cid)] = f"error: {e}"

    try:
        audience_service.register_web_device(token=req.token, club_ids=req.club_ids if req.action == "subscribe" else [], device_name=req.device_name or "Web Browser")
        if db_client:
            res = db_client.table("telemetry_state").select("payload").eq("key", "subscriptions").execute()
            cur_subs = res.data[0].get("payload", {}).get("items", []) if res.data else []
            new_entry = {
                "id": f"sub_{uuid.uuid4().hex[:10]}",
                "timestamp": datetime.now(UTC).isoformat(),
                "platform": req.platform or "web",
                "device_name": req.device_name or "Web Browser",
                "action": req.action,
                "club_ids": [int(c) for c in req.club_ids if str(c).isdigit()],
            }
            db_client.table("telemetry_state").upsert({
                "key": "subscriptions",
                "payload": {"items": ([new_entry] + cur_subs)[:100], "updated_at": datetime.now(UTC).isoformat()},
                "updated_at": datetime.now(UTC).isoformat(),
            }, on_conflict="key").execute()
    except Exception as e:
        logger.debug(f"[AudienceService] Web device registration deferred: {e}")

    return {"status": "ok", "action": req.action, "results": results}
