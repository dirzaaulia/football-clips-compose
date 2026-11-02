import html
from datetime import UTC, datetime, timedelta
from typing import Any

from loguru import logger

from worker.clients.youtube_client import youtube_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.config.broadcaster_registry import GLOBAL_BROADCASTER_REGISTRY
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
from worker.services.highlight.normalizer import (
    clean_team_name_for_search,
    get_club_handle,
    get_team_aliases,
    matches_alias,
    normalize_for_alias_matching,
)
from worker.utils.telemetry_logger import append_event


class CandidateFinder:
    def __init__(self, quota_checker):
        self.get_safe_quota = quota_checker
        self.cycle_search_api_calls = 0

    def resolve_target_handles(self, home: str, away: str, comp_id: str, comp_name: str) -> tuple[list[str], list[str], list[str]]:
        comp_name_upper = comp_name.upper()
        comp_id_upper = comp_id.upper()

        tier1 = []
        h_h = get_club_handle(home)
        a_h = get_club_handle(away)
        if h_h:
            tier1.append(h_h)
        if a_h and a_h not in tier1:
            tier1.append(a_h)

        league_h, _ = channel_repository.get_competition_channel(comp_id_upper)
        if not league_h:
            league_h, _ = channel_repository.get_competition_channel(comp_name_upper)
        if league_h and league_h not in tier1:
            tier1.append(league_h)

        tier2 = []
        backups = channel_repository.get_competition_backups(comp_id_upper) or channel_repository.get_competition_backups(comp_name_upper)
        for bh in backups:
            if bh not in tier1 and bh not in tier2:
                tier2.append(bh)

        for b in GLOBAL_BROADCASTER_REGISTRY:
            comps = b.get("competitions", [])
            if comp_id_upper in comps or comp_name_upper in comps:
                h = b.get("channel_handle")
                if h and h not in tier1 and h not in tier2:
                    if len(b.get("allowed_countries", [])) > 1 or not b.get("allowed_countries"):
                        tier2.append(h)

        tier3 = []
        for b in GLOBAL_BROADCASTER_REGISTRY:
            comps = b.get("competitions", [])
            if comp_id_upper in comps or comp_name_upper in comps:
                h = b.get("channel_handle")
                if h and h not in tier1 and h not in tier2 and h not in tier3:
                    tier3.append(h)

        return tier1, tier2, tier3

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
                    raw_items = youtube_rss_client.fetch_channel_recent_videos(c_id)
                    if not raw_items and self.get_safe_quota() >= 10:
                        raw_items = youtube_client.get_playlist_recent_videos(c_id, max_results=scan_limit)

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

                        if has_h and has_a:
                            if is_league or has_highlight_keyword(title) or has_scoreline(title):
                                matched_items.append({"id": {"videoId": v_id}, "snippet": snip})
                        elif not is_league:
                            target_a = a_aliases if handle == get_club_handle(home) else h_aliases
                            if any(matches_alias(a, norm_t) for a in target_a) and has_highlight_keyword(title):
                                matched_items.append({"id": {"videoId": v_id}, "snippet": snip})

                    if (
                        not matched_items
                        and (allow_search_api or (self.cycle_search_api_calls < 3 and self.get_safe_quota() >= 1500))
                    ):
                        self.cycle_search_api_calls += 1
                        matched_items = youtube_client.search_videos(query, c_id, pub_after, pub_before)

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
                        candidates.append({
                            "id": vid,
                            "title": title,
                            "published_at": item.get("snippet", {}).get("publishedAt"),
                            "duration": dur,
                            "is_embeddable": embeds.get(vid, True),
                            "region": v_reg,
                            "geo_penalty": penalty,
                            "geo_desc": desc,
                            "channel": handle,
                            "tier": tier_num,
                            "tier_desc": tier_desc,
                            "is_target_catered": is_target_market_catered(v_reg),
                            "watchability_score": get_worldwide_watchability_score(v_reg),
                        })
                except Exception as e:
                    logger.debug(f"[CandidateFinder] Channel {handle} scan error: {e}")

        return candidates, target_handles, query
