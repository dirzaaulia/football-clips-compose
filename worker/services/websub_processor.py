"""Unified Event-Driven WebSub Highlight Evaluator with AI Reranking & Waterfall Tracing."""

from datetime import UTC, datetime
import sys
from typing import Any

from loguru import logger

from worker.clients.ai_gateway_client import ai_gateway_client
from worker.clients.database_client import db_client as supabase_client
from worker.clients.edge_cache_client import edge_cache_client
from worker.repositories.channel_repository import channel_repository
from worker.services.highlight.filters import (
    TOP_FOOTBALL_FAN_MARKETS,
    get_worldwide_watchability_score,
    is_target_country_catered,
)
from worker.services.highlight.websub_archive_matcher import (
    find_matching_fixture_for_websub,
)
from worker.services.notification_service import notification_service
from worker.services.telemetry_service import telemetry_service
from worker.utils.telemetry_logger import append_event


def _persist_websub_events(events: list):
    if events:
        try:
            telemetry_service.record_cycle("WEBSUB_PUSH", "0s", 1, events, pipeline_type="WEBSUB_PUSH")
        except Exception as e:
            logger.debug(f"[WebSub Unified] Could not persist telemetry: {e}")


def process_websub_video(
    highlight_service: Any,
    video_id: str,
    title: str,
    channel_id: str,
    author_name: str = "",
    published_at: str | None = None,
    log_events: list | None = None,
) -> dict[str, Any]:
    """Unified Event-Driven WebSub Highlight Evaluator with AI Reranking & Waterfall Tracing."""
    if log_events is None:
        log_events = []

    if not video_id or not title:
        return {"status": "INVALID_INPUT"}

    now_utc = datetime.now(UTC)
    now_iso = now_utc.isoformat()

    shp = sys.modules.get("worker.services.single_highlight_processor")
    active_sb = getattr(shp, "supabase_client", supabase_client)
    active_ai = getattr(shp, "ai_gateway_client", ai_gateway_client)
    active_edge = getattr(shp, "edge_cache_client", edge_cache_client)
    active_notify = getattr(shp, "notification_service", notification_service)

    channel_handle = channel_repository.get_handle_for_channel_id(channel_id) or author_name or channel_id
    push_meta = {
        "video_id": video_id,
        "title": title,
        "channel": channel_handle,
        "channel_id": channel_id,
        "published_at": published_at,
    }

    append_event(
        log_events,
        f"[WEBSUB EVAL] Evaluating upload '{title}' ({video_id}) from channel {channel_handle}",
        "INFO",
        "WEBSUB",
        "EVAL_START",
        attributes={"step": "EVAL_START", "verdict": "EVALUATING", **push_meta},
    )

    is_forbidden, reason = highlight_service.check_forbidden_title(title)
    if is_forbidden:
        append_event(
            log_events,
            f"[WEBSUB REJECT] Video '{title}' ({video_id}) rejected: {reason}",
            "WARN",
            "WEBSUB",
            "FORBIDDEN",
            attributes={"verdict": "REJECTED", "reason": reason, **push_meta},
        )
        _persist_websub_events(log_events)
        return {"status": "FORBIDDEN", "reason": reason}

    best_match, db_err = find_matching_fixture_for_websub(active_sb, highlight_service, title, now_utc)
    if db_err:
        return {"status": "DB_ERROR", "error": db_err}

    if not best_match:
        append_event(
            log_events,
            f"[WEBSUB UNMATCHED] Video '{title}' did not match any active or historical fixture",
            "INFO",
            "WEBSUB",
            "UNMATCHED",
            attributes={"verdict": "REJECTED", "reason": "No matching fixture in recent or archive matches", **push_meta},
        )
        _persist_websub_events(log_events)
        return {"status": "UNMATCHED"}

    match = best_match
    match_id = match["id"]
    home = match["home_team_name"]
    away = match["away_team_name"]
    comp = match.get("competition_name") or match.get("competition_id") or "League"
    match_meta = {
        "match_id": str(match_id),
        "home_team": home,
        "away_team": away,
        "competition_name": comp,
        "utc_date": str(match.get("utc_date") or ""),
        **push_meta,
    }

    candidate_dict = {"id": video_id, "title": title, "channel": channel_handle}
    best_idx, ai_reason, evaluator_name = active_ai.rerank_highlights(home, away, comp, [candidate_dict])

    if best_idx == 0:
        append_event(
            log_events,
            f"   [AI EVAL] Video accepted by {evaluator_name}: {ai_reason}",
            "SUCCESS",
            "AI_GATEWAY",
            "AI_ACCEPTED",
            match_id=match_id,
            attributes={"step": "AI_EVAL", "verdict": "ACCEPTED", "reason": ai_reason, "evaluator": evaluator_name, **match_meta},
        )
    else:
        append_event(
            log_events,
            f"   [AI REJECT] Video rejected by {evaluator_name}: {ai_reason}",
            "WARN",
            "AI_GATEWAY",
            "AI_REJECTED",
            match_id=match_id,
            attributes={"step": "AI_EVAL", "verdict": "REJECTED", "reason": ai_reason, "evaluator": evaluator_name, **match_meta},
        )
        _persist_websub_events(log_events)
        return {"status": "AI_REJECTED", "reason": ai_reason}

    reg = None
    duration_sec = 0
    embeddable = True
    try:
        reg, duration_sec, embeddable = highlight_service.get_video_region_info(video_id)
        if not embeddable:
            append_event(
                log_events,
                f"[WEBSUB REJECT] Video '{title}' ({video_id}) has third-party embed disabled",
                "WARN",
                "WEBSUB",
                "EMBED_RESTRICTED",
                match_id=match_id,
                attributes={"verdict": "REJECTED", "reason": "Embedding disabled by uploader", **match_meta},
            )
            _persist_websub_events(log_events)
            return {"status": "EMBED_RESTRICTED"}
        if duration_sec and duration_sec < 45:
            append_event(
                log_events,
                f"[WEBSUB REJECT] Video '{title}' ({video_id}) duration too short ({duration_sec}s)",
                "WARN",
                "WEBSUB",
                "TOO_SHORT",
                match_id=match_id,
                attributes={"verdict": "REJECTED", "reason": f"Duration too short ({duration_sec}s)", **match_meta},
            )
            _persist_websub_events(log_events)
            return {"status": "TOO_SHORT"}
    except Exception as e:
        logger.debug(f"Failed to fetch region info for {video_id}: {e}")

    existing_vid = match.get("highlight_video_id")
    existing_reg = match.get("youtube_region_info")
    existing_watchability = get_worldwide_watchability_score(existing_reg) if existing_vid else 0.0
    new_watchability = get_worldwide_watchability_score(reg)

    if existing_vid and existing_watchability >= 95.0:
        if not embeddable or new_watchability < existing_watchability:
            append_event(
                log_events,
                f"   [OVERWRITE BLOCKED] Existing video {existing_vid} ({existing_watchability}%) beats candidate {video_id} ({new_watchability}%, embed={embeddable})",
                "INFO",
                "YOUTUBE",
                "REGRESSION_BLOCKED",
                match_id=match_id,
                attributes={"verdict": "REJECTED", "reason": f"Existing highlight ({existing_watchability}%) beats candidate", **match_meta},
            )
            _persist_websub_events(log_events)
            return {"status": "OVERWRITE_PREVENTED"}

    if not is_target_country_catered(reg, TOP_FOOTBALL_FAN_MARKETS):
        append_event(
            log_events,
            f"   [GEO BLOCKED] Video {video_id} is not playable in major football markets",
            "WARN",
            "YOUTUBE",
            "GEO_BLOCKED",
            match_id=match_id,
            attributes={"verdict": "REJECTED", "reason": "Not playable in major football markets", **match_meta},
        )
        _persist_websub_events(log_events)
        return {"status": "GEO_BLOCKED"}

    update_data = {"highlight_video_id": video_id, "last_youtube_check": now_iso}
    if existing_vid and existing_vid != video_id:
        update_data["backup_highlight_video_id"] = existing_vid
        if existing_reg:
            update_data["backup_youtube_region_info"] = existing_reg
    if reg is not None:
        update_data["youtube_region_info"] = reg

    active_sb.table("matches").update(update_data).eq("id", match_id).execute()
    active_edge.purge_matches_cache()
    active_notify.notify_highlight_ready(match, video_id, "Official Highlights", log_events)

    append_event(
        log_events,
        f"   [WEBSUB LINKED] {home} vs {away} -> https://youtu.be/{video_id} verified via {evaluator_name}.",
        "SUCCESS",
        "WEBSUB",
        "DB_LINKED",
        match_id=match_id,
        attributes={"step": "RESULT", "status": "LINKED", "verdict": "LINKED", "evaluator": evaluator_name, **match_meta},
    )
    _persist_websub_events(log_events)

    return {"status": "LINKED", "match_id": match_id, "video_id": video_id, "evaluator": evaluator_name}
