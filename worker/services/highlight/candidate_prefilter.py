"""Pre-filtering and metadata validation for discovered YouTube highlight candidates.

Emits explicit Step 3 CANDIDATE_REJECTED telemetry verdicts whenever a discovered
video fails title, duration, Shorts, or target-market geoblock guardrails.
"""

import html
from typing import Any

from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings
from worker.services.highlight.filters import (
    check_forbidden_title,
    classify_candidate_tier,
    get_geoblock_status,
    get_worldwide_watchability_score,
    is_target_market_catered,
)
from worker.utils.telemetry_logger import append_event


def filter_and_enrich_matched_items(
    matched_items: list[dict[str, Any]],
    handle: str,
    match: dict[str, Any],
    log_events: list[dict[str, Any]],
) -> list[dict[str, Any]]:
    """Fetches YouTube video details for matched items and emits explicit rejection reasons if filtered out."""
    v_ids = [it["id"]["videoId"] for it in matched_items if it.get("id", {}).get("videoId")]
    if not v_ids:
        return []

    durs, regs, embeds = youtube_client.get_video_details(v_ids)
    valid_candidates: list[dict[str, Any]] = []
    match_id = match.get("id")

    for item in matched_items:
        vid = item.get("id", {}).get("videoId")
        if not vid:
            continue
        title = html.unescape(item.get("snippet", {}).get("title", ""))
        src_tag = "RSS" if item.get("source") == "rss" else "API"
        dur = durs.get(vid, 0)
        v_reg = regs.get(vid)
        is_embed = embeds.get(vid, True)
        penalty, geo_desc = get_geoblock_status(v_reg)

        is_forbid, forbid_reason = check_forbidden_title(
            title,
            match.get("home_team_name"),
            match.get("away_team_name"),
        )
        if is_forbid:
            reason_str = f"Forbidden title pattern ({forbid_reason})"
            append_event(
                log_events,
                f"   [CANDIDATE_REJECTED] Video {vid} ('{title}') from {handle} rejected: {reason_str}",
                "INFO",
                "YOUTUBE",
                "CANDIDATE_REJECTED",
                match_id=match_id,
                attributes={"candidate_id": vid, "title": title, "channel": handle, "reason": reason_str},
            )
            continue

        if not (settings.min_duration_seconds <= dur <= settings.max_duration_seconds):
            bound_desc = "Full Stream/Replay" if dur > settings.max_duration_seconds else "Too Short"
            reason_str = (
                f"Duration {dur}s outside {settings.min_duration_seconds}s-{settings.max_duration_seconds}s "
                f"highlight bounds ({bound_desc}; embed={is_embed}, geo={geo_desc})"
            )
            append_event(
                log_events,
                f"   [CANDIDATE_REJECTED] Video {vid} ('{title}') from {handle} rejected: {reason_str}",
                "INFO",
                "YOUTUBE",
                "CANDIDATE_REJECTED",
                match_id=match_id,
                attributes={"candidate_id": vid, "title": title, "channel": handle, "reason": reason_str, "duration": dur},
            )
            continue

        if youtube_client.is_youtube_short(vid):
            reason_str = "Detected as vertical YouTube Short (/shorts/)"
            append_event(
                log_events,
                f"   [CANDIDATE_REJECTED] Video {vid} ('{title}') from {handle} rejected: {reason_str}",
                "INFO",
                "YOUTUBE",
                "CANDIDATE_REJECTED",
                match_id=match_id,
                attributes={"candidate_id": vid, "title": title, "channel": handle, "reason": reason_str},
            )
            continue

        if penalty >= 9999:
            reason_str = f"Geo-blocked across all 15 target football markets ({geo_desc}; {dur}s, embed={is_embed})"
            append_event(
                log_events,
                f"   [CANDIDATE_REJECTED] Video {vid} ('{title}') from {handle} rejected: {reason_str}",
                "INFO",
                "YOUTUBE",
                "CANDIDATE_REJECTED",
                match_id=match_id,
                attributes={"candidate_id": vid, "title": title, "channel": handle, "reason": reason_str},
            )
            continue

        tier_num, tier_desc = classify_candidate_tier(handle, v_reg)
        watchability = get_worldwide_watchability_score(v_reg)

        append_event(
            log_events,
            f"   [CANDIDATE] Discovered from {handle} [{src_tag}]: '{title}' -> https://youtu.be/{vid} ({dur}s, embed={is_embed}, reach={watchability:.0f}%)",
            "INFO",
            "YOUTUBE",
            "CANDIDATE_ITEM",
            match_id=match_id,
            attributes={
                "candidate_id": vid,
                "title": title,
                "channel": handle,
                "source": src_tag,
                "duration": dur,
                "is_embeddable": is_embed,
                "url": f"https://youtu.be/{vid}",
                "watchability_score": watchability,
            },
        )

        valid_candidates.append({
            "id": vid,
            "title": title,
            "description": item.get("snippet", {}).get("description", "")[:300],
            "published_at": item.get("snippet", {}).get("publishedAt"),
            "duration": dur,
            "is_embeddable": is_embed,
            "region": v_reg,
            "geo_penalty": penalty,
            "geo_desc": geo_desc,
            "channel": handle,
            "tier": tier_num,
            "tier_desc": tier_desc,
            "is_target_catered": is_target_market_catered(v_reg),
            "watchability_score": watchability,
            "source": item.get("source", "rss"),
        })

    return valid_candidates
