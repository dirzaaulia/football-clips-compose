"""Single match highlight processor for event-driven, isolated Cloud Tasks execution.
Ensures per-match fault isolation: Match #1 never blocks, crashes, or delays Match #2.
"""

import html
import re
from datetime import UTC, datetime, timedelta
from typing import Any
from loguru import logger
from worker.clients.ai_gateway_client import ai_gateway_client
from worker.clients.database_client import db_client as supabase_client
from worker.clients.edge_cache_client import edge_cache_client
from worker.services.highlight.filters import is_target_country_catered
from worker.services.notification_service import notification_service
from worker.services.telemetry_service import telemetry_service
from worker.utils.telemetry_logger import append_event


def process_single_match_highlight(
    highlight_service: Any,
    match_id: int | str,
    log_events: list | None = None,
) -> dict[str, Any]:
    """Processes official YouTube highlight discovery for a single match in complete isolation."""
    if log_events is None:
        log_events = []

    now_iso = datetime.now(UTC).isoformat()

    try:
        res = (
            supabase_client.table("matches")
            .select(
                "id, home_team_name, away_team_name, competition_id, competition_name, "
                "utc_date, last_youtube_check, highlight_video_id, youtube_region_info, "
                "home_score, away_score, status"
            )
            .eq("id", match_id)
            .execute()
        )
    except Exception as e:
        logger.error(f"[SingleHighlightProcessor] DB error fetching match {match_id}: {e}")
        return {"status": "DB_ERROR", "match_id": match_id, "error": str(e)}

    if not res.data:
        logger.warning(f"[SingleHighlightProcessor] Match {match_id} not found in database.")
        return {"status": "NOT_FOUND", "match_id": match_id}

    match = res.data[0]
    home = match.get("home_team_name", "Home")
    away = match.get("away_team_name", "Away")
    comp = match.get("competition_name") or match.get("competition_id") or "League"
    m_date = str(match.get("utc_date", ""))[:10]
    existing_vid = match.get("highlight_video_id")
    existing_reg = match.get("youtube_region_info")

    if existing_vid and highlight_service.get_worldwide_watchability_score(existing_reg) >= 99.0:
        return {
            "status": "ALREADY_LINKED",
            "match_id": match_id,
            "video_id": existing_vid,
            "reason": "Match already has 100% worldwide highlights linked",
        }

    try:
        res_claimed = (
            supabase_client.table("matches")
            .select("highlight_video_id")
            .not_.is_("highlight_video_id", "null")
            .execute()
        )
        claimed_video_ids = {
            r["highlight_video_id"]
            for r in (res_claimed.data or [])
            if r.get("highlight_video_id")
        }
    except Exception:
        claimed_video_ids = set()

    match_meta = {
        "match_id": str(match["id"]),
        "home_team": home,
        "away_team": away,
        "competition_name": comp,
        "utc_date": str(match.get("utc_date") or ""),
    }

    match_status = match.get("status")
    if match_status and match_status not in ["FINISHED", "AWARDED"]:
        append_event(
            log_events,
            f"[SKIP UNFINISHED] {home} vs {away} status is {match_status}. Only FINISHED matches are eligible.",
            "INFO",
            "YOUTUBE",
            "SKIP_UNFINISHED",
            match_id=match["id"],
            attributes={"step": "RESULT", "status": "SKIPPED", "reason": f"Match status is {match_status}", **match_meta},
        )
        return {"status": "SKIP_UNFINISHED", "match_id": match_id, "reason": f"Match status is {match_status}"}

    append_event(
        log_events,
        f"[SEARCH] Searching official highlights for: {home} vs {away} ({m_date})",
        "INFO",
        "YOUTUBE",
        "SEARCH_START",
        match_id=match["id"],
        attributes={"step": "SEARCH_START", **match_meta},
    )

    try:
        vid, reg, backup_vid, backup_reg, highlight_videos = highlight_service.search_highlight_for_match(
            match,
            log_events,
            claimed_video_ids,
        )

        if vid:
            if vid in claimed_video_ids and vid != existing_vid:
                append_event(
                    log_events,
                    f"   [DUPLICATE DETECTED] Video {vid} already assigned to another fixture. Entering cooldown.",
                    "WARN",
                    "YOUTUBE",
                    "DUPLICATE_CLAIM",
                    match_id=match["id"],
                    attributes={"step": "RESULT", "status": "NOT_INSERTED", "reason": f"Video {vid} already assigned", **match_meta},
                )
                supabase_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
                return {"status": "DUPLICATE_CLAIM", "match_id": match_id, "video_id": vid}

            update_data: dict[str, Any] = {"highlight_video_id": vid, "last_youtube_check": now_iso}
            if existing_vid and existing_vid != vid:
                update_data["backup_highlight_video_id"] = existing_vid
                if existing_reg is not None:
                    update_data["backup_youtube_region_info"] = existing_reg
                append_event(
                    log_events,
                    f"   [VIDEO PROMOTED] Promoted worldwide video {vid} to primary; demoted {existing_vid} to backup.",
                    "SUCCESS",
                    "YOUTUBE",
                    "VIDEO_PROMOTED",
                    match_id=match["id"],
                    attributes={"promoted_id": vid, "backup_id": existing_vid, **match_meta},
                )
            elif backup_vid:
                update_data["backup_highlight_video_id"] = backup_vid
                if backup_reg is not None:
                    update_data["backup_youtube_region_info"] = backup_reg

            if reg is not None:
                update_data["youtube_region_info"] = reg
            if highlight_videos:
                update_data["highlight_videos"] = highlight_videos

            supabase_client.table("matches").update(update_data).eq("id", match["id"]).execute()
            edge_cache_client.purge_matches_cache()
            notification_service.notify_highlight_ready(match, vid, "Official Highlights", log_events)

            append_event(
                log_events,
                f"   [HIGHLIGHT LINKED] {home} vs {away} -> https://youtu.be/{vid} verified and saved.",
                "SUCCESS",
                "YOUTUBE",
                "DB_LINKED",
                match_id=match["id"],
                attributes={"step": "RESULT", "status": "LINKED", "inserted_to_db": True, "video_id": vid, **match_meta},
            )
            return {
                "status": "LINKED",
                "match_id": match_id,
                "video_id": vid,
                "url": f"https://youtu.be/{vid}",
                "global_watchability_pct": highlight_service.get_worldwide_watchability_score(reg),
            }
        else:
            supabase_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
            append_event(
                log_events,
                f"   [NO HIGHLIGHT FOUND] No verified highlight video found for {home} vs {away}.",
                "INFO",
                "YOUTUBE",
                "NOT_FOUND",
                match_id=match["id"],
                attributes={"step": "RESULT", "status": "NOT_FOUND", "inserted_to_db": False, **match_meta},
            )
            return {"status": "NOT_FOUND", "match_id": match_id}

    except Exception as e:
        logger.error(f"[SingleHighlightProcessor] Search failed for {home} vs {away}: {e}")
        supabase_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
        append_event(
            log_events,
            f"   [ERROR] Search failed for {home} vs {away}: {e}",
            "ERROR",
            "YOUTUBE",
            "ERROR",
            match_id=match["id"],
            attributes={"step": "RESULT", "status": "ERROR", "error": str(e), **match_meta},
        )
        return {"status": "ERROR", "match_id": match_id, "error": str(e)}


def _persist_websub_events(events: list):
    if events:
        try:
            telemetry_service.record_cycle(events, mode="WEBSUB_EVENT", pipeline_type="WEBSUB_PUSH")
        except Exception as e:
            logger.debug(f"[WebSub Unified] Could not persist telemetry: {e}")


def process_websub_video(
    highlight_service: Any,
    video_id: str,
    title: str,
    channel_id: str,
    author_name: str = "",
    log_events: list | None = None,
) -> dict[str, Any]:
    """Unified Event-Driven WebSub Highlight Evaluator with AI Reranking & Waterfall Tracing."""
    if log_events is None:
        log_events = []

    if not video_id or not title:
        return {"status": "INVALID_INPUT"}

    now_utc = datetime.now(UTC)
    now_iso = now_utc.isoformat()

    is_forbidden, reason = highlight_service.check_forbidden_title(title)
    if is_forbidden:
        append_event(log_events, f"[WEBSUB REJECT] Video '{title}' ({video_id}) rejected: {reason}", "WARN", "WEBSUB", "FORBIDDEN")
        _persist_websub_events(log_events)
        return {"status": "FORBIDDEN", "reason": reason}

    if not highlight_service.has_highlight_keyword(title):
        append_event(log_events, f"[WEBSUB REJECT] Video '{title}' ({video_id}) lacks required highlight keywords", "WARN", "WEBSUB", "NO_KEYWORD")
        _persist_websub_events(log_events)
        return {"status": "NO_KEYWORD"}

    cutoff_72h = (now_utc - timedelta(hours=72)).isoformat()
    try:
        res = (
            supabase_client.table("matches")
            .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, highlight_video_id, youtube_region_info, home_score, away_score, status")
            .eq("status", "FINISHED")
            .gte("utc_date", cutoff_72h)
            .order("utc_date", desc=True)
            .limit(50)
            .execute()
        )
        candidates = res.data or []
    except Exception as e:
        logger.error(f"[WebSub Unified] DB error fetching candidates: {e}")
        return {"status": "DB_ERROR", "error": str(e)}

    t_clean = html.unescape(title).lower()
    best_match = None
    for m in candidates:
        home = m.get("home_team_name", "")
        away = m.get("away_team_name", "")
        h_aliases = highlight_service.get_team_aliases(home)
        a_aliases = highlight_service.get_team_aliases(away)
        home_hit = any(re.search(rf"\b{re.escape(a)}\b", t_clean) for a in h_aliases)
        away_hit = any(re.search(rf"\b{re.escape(a)}\b", t_clean) for a in a_aliases)
        if home_hit and away_hit:
            best_match = m
            break

    if not best_match:
        append_event(log_events, f"[WEBSUB UNMATCHED] Video '{title}' did not match any unlinked fixture in the last 72h", "INFO", "WEBSUB", "UNMATCHED")
        _persist_websub_events(log_events)
        return {"status": "UNMATCHED"}

    match = best_match
    match_id = match["id"]
    home = match["home_team_name"]
    away = match["away_team_name"]
    comp = match.get("competition_name") or match.get("competition_id") or "League"
    match_meta = {"match_id": str(match_id), "home_team": home, "away_team": away, "competition_name": comp}

    candidate_dict = {"id": video_id, "title": title, "channel": author_name or channel_id}
    best_idx, ai_reason, evaluator_name = ai_gateway_client.rerank_highlights(home, away, comp, [candidate_dict])

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
    try:
        reg, duration_sec, embeddable = highlight_service.get_video_region_info(video_id)
        if not embeddable:
            return {"status": "EMBED_RESTRICTED"}
        if duration_sec and duration_sec < 45:
            return {"status": "TOO_SHORT"}
    except Exception:
        pass

    target_markets = ["ID", "GB", "US"]
    if not is_target_country_catered(reg, target_markets):
        append_event(log_events, f"   [GEO BLOCKED] Video {video_id} is not playable in target markets {target_markets}", "WARN", "YOUTUBE", "GEO_BLOCKED", match_id=match_id)
        _persist_websub_events(log_events)
        return {"status": "GEO_BLOCKED"}

    existing_vid = match.get("highlight_video_id")
    update_data = {"highlight_video_id": video_id, "last_youtube_check": now_iso}
    if existing_vid and existing_vid != video_id:
        update_data["backup_highlight_video_id"] = existing_vid
    if reg is not None:
        update_data["youtube_region_info"] = reg

    supabase_client.table("matches").update(update_data).eq("id", match_id).execute()
    edge_cache_client.purge_matches_cache()
    notification_service.notify_highlight_ready(match, video_id, "Official Highlights", log_events)

    append_event(
        log_events,
        f"   [WEBSUB LINKED] {home} vs {away} -> https://youtu.be/{video_id} verified via {evaluator_name}.",
        "SUCCESS",
        "WEBSUB",
        "DB_LINKED",
        match_id=match_id,
        attributes={"step": "RESULT", "status": "LINKED", "video_id": video_id, **match_meta},
    )
    _persist_websub_events(log_events)

    return {"status": "LINKED", "match_id": match_id, "video_id": video_id, "evaluator": evaluator_name}
