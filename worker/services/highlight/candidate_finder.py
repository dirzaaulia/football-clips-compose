import html
from datetime import UTC, datetime, timedelta
from typing import Any

from loguru import logger

from worker.clients.youtube_client import youtube_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.config.settings import settings
from worker.repositories.channel_repository import channel_repository
from worker.services.highlight.filters import (
    check_forbidden_title,
    classify_candidate_tier,
    get_geoblock_status,
    get_worldwide_watchability_score,
    has_highlight_keyword,
    has_scoreline,
    is_target_market_catered,
)
from worker.services.highlight.handle_resolver import resolve_target_handles
from worker.services.highlight.normalizer import (
    clean_team_name_for_search,
    get_club_handle,
    get_team_aliases,
    matches_alias,
    normalize_for_alias_matching,
)
from worker.utils.telemetry_logger import append_event


def _build_scanned_summary(raw_items: list[dict[str, Any]] | None, source: str) -> list[dict[str, Any]]:
    summary = []
    for it in (raw_items or []):
        vid = (
            it.get("snippet", {}).get("resourceId", {}).get("videoId")
            or (it.get("id", {}).get("videoId") if isinstance(it.get("id"), dict) else it.get("id") if isinstance(it.get("id"), str) else "")
        )
        if vid:
            summary.append({
                "id": vid,
                "title": html.unescape(it.get("snippet", {}).get("title", "")),
                "publishedAt": it.get("snippet", {}).get("publishedAt"),
                "source": source,
            })
    return summary


class CandidateFinder:
    def __init__(self, quota_checker):
        self.get_safe_quota = quota_checker
        self.cycle_search_api_calls = 0

    def resolve_target_handles(self, home: str, away: str, comp_id: str, comp_name: str) -> tuple[list[str], list[str], list[str]]:
        return resolve_target_handles(home, away, comp_id, comp_name)

    def find_candidates(
        self,
        match: dict[str, Any],
        log_events: list[dict[str, Any]],
        allow_search_api: bool = False,
    ) -> tuple[list[dict[str, Any]], list[str], str]:
        home = match["home_team_name"]
        away = match["away_team_name"]
        comp = match.get("competition_name") or match.get("competition_id") or "League"
        comp_id = (match.get("competition_id") or "").upper()
        match_start = datetime.fromisoformat(match["utc_date"].replace("Z", "+00:00"))
        now_utc = datetime.now(UTC)
        match_age_h = (now_utc - match_start).total_seconds() / 3600.0

        t1, t2, t3 = self.resolve_target_handles(home, away, comp_id, comp)
        target_handles = t1 + t2 + t3
        if not target_handles:
            return [], [], ""

        h_search = clean_team_name_for_search(home)
        a_search = clean_team_name_for_search(away)
        h_aliases = get_team_aliases(home)
        a_aliases = get_team_aliases(away)
        query = f"{h_search} {a_search} highlights"

        pub_after = (match_start - timedelta(hours=2)).strftime("%Y-%m-%dT%H:%M:%SZ")
        pub_before = (match_start + timedelta(hours=96)).strftime("%Y-%m-%dT%H:%M:%SZ")
        scan_limit = 15 if match_age_h <= 24.0 else 25

        candidates: list[dict[str, Any]] = []
        tier_hierarchy = [
            ("Tier 1: Club & League Official", t1),
            ("Tier 2: Regional Broadcasters", t2),
            ("Tier 3: Domestic Broadcasters", t3),
        ]

        league_handle, _ = channel_repository.get_competition_channel(comp_id)

        for tier_label, handles in tier_hierarchy:
            if not handles:
                continue

            if any(
                c.get("is_embeddable", True)
                and not (c.get("region") or {}).get("blocked")
                and not (c.get("region") or {}).get("allowed")
                for c in candidates
            ):
                break

            for handle in handles:
                try:
                    c_id = youtube_client.resolve_channel_id(handle)
                    if not c_id:
                        continue

                    append_event(
                        log_events,
                        f"   [CHANNEL SCAN] Checking channel {handle} uploads...",
                        "INFO",
                        "YOUTUBE",
                        "CHANNEL_SCAN",
                        match_id=match.get("id"),
                    )

                    matched_items = []
                    is_league = bool(league_handle and handle.lower() == league_handle.lower())
                    source_mode = "rss"
                    raw_items = youtube_rss_client.fetch_channel_recent_videos(c_id)
                    scanned_summary = _build_scanned_summary(raw_items, "rss")

                    if raw_items:
                        append_event(
                            log_events,
                            f"   [RSS DISCOVERY] Scanned {len(raw_items)} uploads from channel {handle} (0 quota units)",
                            "INFO",
                            "YOUTUBE",
                            "RSS_DISCOVERY",
                            match_id=match.get("id"),
                            attributes={"scanned_videos": scanned_summary, "channel": handle},
                        )
                    elif self.get_safe_quota() >= 10:
                        source_mode = "api_playlist"
                        raw_items = youtube_client.get_playlist_recent_videos(c_id, max_results=scan_limit)
                        scanned_summary = _build_scanned_summary(raw_items, "api_playlist")
                        if raw_items:
                            append_event(
                                log_events,
                                f"   [API PLAYLIST] Fetched {len(raw_items)} uploads from {handle} via YouTube Data API (Cost: 1 quota)",
                                "INFO",
                                "YOUTUBE",
                                "API_CHANNEL_FALLBACK",
                                match_id=match.get("id"),
                                attributes={"scanned_videos": scanned_summary, "channel": handle},
                            )
                        else:
                            append_event(
                                log_events,
                                f"   [CHANNEL EMPTY] 0 public uploads available on channel {handle}",
                                "INFO",
                                "YOUTUBE",
                                "CHANNEL_EMPTY",
                                match_id=match.get("id"),
                                attributes={"scanned_videos": [], "channel": handle},
                            )
                    else:
                        append_event(
                            log_events,
                            f"   [CHANNEL EMPTY] 0 public uploads available on channel {handle}",
                            "INFO",
                            "YOUTUBE",
                            "CHANNEL_EMPTY",
                            match_id=match.get("id"),
                            attributes={"scanned_videos": [], "channel": handle},
                        )

                    for it in raw_items or []:
                        snip = it.get("snippet", {})
                        title = html.unescape(snip.get("title", ""))
                        v_id = snip.get("resourceId", {}).get("videoId")
                        if not v_id:
                            continue

                        pub_str = snip.get("publishedAt")
                        if pub_str:
                            try:
                                p_dt = datetime.fromisoformat(pub_str.replace("Z", "+00:00"))
                                if not (match_start - timedelta(hours=2) <= p_dt <= match_start + timedelta(hours=96)):
                                    continue
                            except Exception:
                                pass

                        norm_t = normalize_for_alias_matching(title)
                        has_h = any(matches_alias(a, norm_t) for a in h_aliases)
                        has_a = any(matches_alias(a, norm_t) for a in a_aliases)

                        is_matched = False
                        if has_h and has_a:
                            if is_league or has_highlight_keyword(title) or has_scoreline(title):
                                is_matched = True
                        elif not is_league:
                            target_a = a_aliases if handle == get_club_handle(home) else h_aliases
                            desc = snip.get("description", "")
                            norm_d = normalize_for_alias_matching(desc)
                            opponent_found = any(matches_alias(a, norm_t) for a in target_a) or any(matches_alias(a, norm_d) for a in target_a)
                            if opponent_found and has_highlight_keyword(title):
                                is_matched = True

                        if is_matched:
                            matched_items.append({"id": {"videoId": v_id}, "snippet": snip, "source": source_mode})
                            src_name = "RSS" if source_mode == "rss" else "API"
                            append_event(
                                log_events,
                                f"   [{src_name} ITEM] Discovered from {handle} [{src_name}]: '{title}' -> https://youtu.be/{v_id}",
                                "INFO",
                                "YOUTUBE",
                                "CANDIDATE_ITEM",
                                match_id=match.get("id"),
                                attributes={
                                    "candidate_id": v_id,
                                    "title": title,
                                    "channel": handle,
                                    "source": src_name,
                                    "url": f"https://youtu.be/{v_id}",
                                },
                            )

                    if (
                        not matched_items
                        and (allow_search_api or self.get_safe_quota() >= 500)
                    ):
                        self.cycle_search_api_calls += 1
                        append_event(
                            log_events,
                            f"   [API SEARCH] Querying YouTube Data API: '{query}' for channel {handle} (Cost: 100 quota)",
                            "INFO",
                            "YOUTUBE",
                            "API_GENERAL_SEARCH",
                            match_id=match.get("id"),
                        )
                        api_search_items = youtube_client.search_videos(query, c_id, pub_after, pub_before)
                        if api_search_items:
                            append_event(
                                log_events,
                                f"   [API RESULT] Discovered {len(api_search_items)} search results from {handle}",
                                "INFO",
                                "YOUTUBE",
                                "API_GENERAL_SEARCH",
                                match_id=match.get("id"),
                            )
                            for it in api_search_items:
                                it["source"] = "api_search"
                                matched_items.append(it)
                                s_vid = it.get("id", {}).get("videoId")
                                s_title = html.unescape(it.get("snippet", {}).get("title", ""))
                                if s_vid:
                                    append_event(
                                        log_events,
                                        f"   [API RESULT] Discovered from {handle} [API]: '{s_title}' -> https://youtu.be/{s_vid}",
                                        "INFO",
                                        "YOUTUBE",
                                        "CANDIDATE_ITEM",
                                        match_id=match.get("id"),
                                        attributes={
                                            "candidate_id": s_vid,
                                            "title": s_title,
                                            "channel": handle,
                                            "source": "API",
                                            "url": f"https://youtu.be/{s_vid}",
                                        },
                                    )

                    if not matched_items:
                        append_event(
                            log_events,
                            f"   [NO VIDEOS] 0 matching videos from channel {handle}",
                            "INFO",
                            "YOUTUBE",
                            "CHANNEL_EMPTY",
                            match_id=match.get("id"),
                        )
                        continue

                    v_ids = [it["id"]["videoId"] for it in matched_items if it.get("id", {}).get("videoId")]
                    durs, regs, embeds = youtube_client.get_video_details(v_ids)

                    for item in matched_items:
                        vid = item["id"]["videoId"]
                        title = html.unescape(item["snippet"]["title"])
                        is_forbid, _ = check_forbidden_title(title)
                        if is_forbid:
                            continue
                        dur = durs.get(vid, 0)
                        if not (settings.min_duration_seconds <= dur <= settings.max_duration_seconds):
                            continue
                        if youtube_client.is_youtube_short(vid):
                            continue

                        v_reg = regs.get(vid)
                        penalty, desc = get_geoblock_status(v_reg)
                        if penalty >= 9999:
                            continue

                        tier_num, tier_desc = classify_candidate_tier(handle, v_reg)
                        watchability = get_worldwide_watchability_score(v_reg)
                        is_embed = embeds.get(vid, True)
                        src_tag = "RSS" if item.get("source") == "rss" else "API"

                        append_event(
                            log_events,
                            f"   [CANDIDATE] Discovered from {handle} [{src_tag}]: '{title}' -> https://youtu.be/{vid} ({dur}s, embed={is_embed}, reach={watchability:.0f}%)",
                            "INFO",
                            "YOUTUBE",
                            "CANDIDATE_ITEM",
                            match_id=match.get("id"),
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

                        candidates.append({
                            "id": vid,
                            "title": title,
                            "description": item.get("snippet", {}).get("description", "")[:300],
                            "published_at": item.get("snippet", {}).get("publishedAt"),
                            "duration": dur,
                            "is_embeddable": is_embed,
                            "region": v_reg,
                            "geo_penalty": penalty,
                            "geo_desc": desc,
                            "channel": handle,
                            "tier": tier_num,
                            "tier_desc": tier_desc,
                            "is_target_catered": is_target_market_catered(v_reg),
                            "watchability_score": watchability,
                            "source": item.get("source", "rss"),
                        })
                except Exception as e:
                    logger.debug(f"[CandidateFinder] Channel {handle} scan error: {e}")

        return candidates, target_handles, query
