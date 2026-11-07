import re
from datetime import UTC, datetime
from typing import Any

from fastapi import APIRouter, BackgroundTasks, Query, Request
from fastapi.responses import PlainTextResponse
from loguru import logger

from worker.services.highlight_service import highlight_service
from worker.services.telemetry_service import telemetry_service

router = APIRouter(tags=["WebSub Push"])


@router.get("/websub")
async def websub_handshake(
    hub_mode: str | None = Query(default=None, alias="hub.mode"),
    hub_challenge: str | None = Query(default=None, alias="hub.challenge"),
    hub_topic: str | None = Query(default=None, alias="hub.topic"),
):
    """Google PubSubHubbub Handshake Verification."""
    if hub_challenge and hub_mode in ["subscribe", "unsubscribe"]:
        logger.info(f"[WebSub Handshake] Confirmed {hub_mode} for topic: {hub_topic}")
        try:
            telemetry_service.record_cycle(
                "WEBSUB_HANDSHAKE",
                "0s",
                1,
                [{
                    "line": f"[WEBSUB HANDSHAKE] Verified Google Hub challenge for topic: {hub_topic}",
                    "level": "SUCCESS",
                    "subsystem": "WEBSUB_PUSH",
                    "code": "HANDSHAKE_OK",
                }],
                pipeline_type="WEBSUB_PUSH",
            )
        except Exception as e:
            logger.debug(f"[WebSub Handshake] Telemetry recording error: {e}")
        return PlainTextResponse(content=hub_challenge, status_code=200)
    return PlainTextResponse(content="Invalid handshake request", status_code=400)


@router.post("/websub")
async def websub_notification(
    request: Request,
    background_tasks: BackgroundTasks,
):
    """Receives YouTube Atom XML real-time upload notification and triggers highlight sync."""
    try:
        body = await request.body()
        xml = body.decode("utf-8", errors="ignore")
        if not xml or "<entry>" not in xml:
            return PlainTextResponse("OK", status_code=200)

        entry_match = re.search(r"<entry[^>]*>([\s\S]*?)</entry>", xml, re.IGNORECASE)
        entry_content = entry_match.group(1) if entry_match else xml

        vid_match = re.search(r"<(?:yt:)?videoId[^>]*>(.*?)</(?:yt:)?videoId>", entry_content, re.IGNORECASE)
        title_match = re.search(r"<title[^>]*>(.*?)</title>", entry_content, re.IGNORECASE)
        chan_match = re.search(r"<(?:yt:)?channelId[^>]*>(.*?)</(?:yt:)?channelId>", entry_content, re.IGNORECASE)
        author_match = re.search(r"<name[^>]*>(.*?)</name>", entry_content, re.IGNORECASE)
        pub_match = re.search(r"<published[^>]*>(.*?)</published>", entry_content, re.IGNORECASE)

        video_id = vid_match.group(1).strip() if vid_match else None
        title = title_match.group(1).strip() if title_match else None
        channel_id = chan_match.group(1).strip() if chan_match else None
        author_name = author_match.group(1).strip() if author_match else None
        published_at = pub_match.group(1).strip() if pub_match else None

        from worker.repositories.channel_repository import channel_repository
        channel_handle = channel_repository.get_handle_for_channel_id(channel_id or "") or author_name or channel_id or "YouTube Hub"

        logger.info(f"[WebSub Push] Received upload notification: {video_id} ('{title}') from {channel_handle} (published: {published_at})")
        try:
            telemetry_service.record_cycle(
                "WEBSUB_PUSH",
                "0s",
                1,
                [{
                    "line": f"[WEBSUB PUSH RECEIVED] Ingested webhook for '{title}' ({video_id}) channel: {channel_handle}",
                    "level": "INFO",
                    "subsystem": "WEBSUB_PUSH",
                    "code": "PUSH_RECEIVED",
                    "attributes": {
                        "video_id": video_id,
                        "title": title,
                        "channel": channel_handle,
                        "channel_id": channel_id,
                        "published_at": published_at,
                    },
                }],
                pipeline_type="WEBSUB_PUSH",
            )
        except Exception as e:
            logger.debug(f"[WebSub Push] Telemetry recording error: {e}")

        if video_id and title:
            background_tasks.add_task(
                highlight_service.process_websub_video,
                video_id,
                title,
                channel_id or "",
                channel_handle,
                published_at,
            )
        else:
            background_tasks.add_task(highlight_service.sync_unlinked_highlights, max_batch=10)

        return PlainTextResponse("Accepted", status_code=200)
    except Exception as e:
        logger.error(f"[WebSub Push] Error processing notification: {e}")
        return PlainTextResponse("Error", status_code=200)


@router.post("/websub/renew")
async def renew_websub_subscriptions(
    background_tasks: BackgroundTasks,
    async_execution: bool = Query(default=True, description="Run renewal asynchronously in background"),
) -> dict[str, Any]:
    """Batch subscription renewal for all club & league YouTube channels to Google WebSub Hub."""
    from worker.scripts.subscribe_youtube_websub import WebSubSubscriber

    subscriber = WebSubSubscriber()
    if async_execution:
        background_tasks.add_task(subscriber.subscribe_all)
        return {
            "status": "QUEUED",
            "message": "Batch WebSub subscription renewal queued in background.",
            "timestamp": datetime.now(UTC).isoformat(),
        }

    await subscriber.subscribe_all()
    return {
        "status": "COMPLETED",
        "message": "Batch WebSub subscription renewal finished.",
        "timestamp": datetime.now(UTC).isoformat(),
    }
