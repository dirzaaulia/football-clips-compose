"""Candidate video discovery across Tier 1, Tier 2, and Tier 3 channels with Entity-First Discovery."""

from datetime import UTC, datetime, timedelta
import html
from typing import Any

from loguru import logger

from worker.clients.youtube_client import youtube_client
from worker.repositories.channel_repository import channel_repository
from worker.services.highlight.candidate_prefilter import filter_and_enrich_matched_items
from worker.services.highlight.filters import has_highlight_keyword, has_scoreline
from worker.services.highlight.handle_resolver import resolve_target_handles
from worker.services.highlight.normalizer import (
    clean_team_name_for_search,
    get_club_handle,
    get_team_aliases,
    matches_alias,
    normalize_for_alias_matching,
)
from worker.services.highlight.rss_discovery_engine import (
    fetch_channel_uploads,
    prefetch_tier_rss_feeds,
    run_api_search_fallback,
)
from worker.utils.telemetry_logger import append_event


class CandidateFinder:
    def __init__(self, quota_checker):
        self.get_safe_quota = quota_checker
        self.cycle_search_api_calls = 0

    def resolve_target_handles(
        self,
        home: str,
        away: str,
        comp_id: str,
        comp_name: str,
    ) -> tuple[list[str], list[str], list[str]]:
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

        seen_channel_ids: set[str] = set()
        for _, handles in tier_hierarchy:
            if not handles:
                continue
            if any(
                c.get("is_embeddable", True)
                and not (c.get("region") or {}).get("blocked")
                and not (c.get("region") or {}).get("allowed")
                for c in candidates
            ):
                break

            prefetch_tier_rss_feeds(handles)

            for handle in handles:
                try:
                    c_id = youtube_client.resolve_channel_id(handle)
                    if not c_id or c_id in seen_channel_ids:
                        continue
                    seen_channel_ids.add(c_id)

                    append_event(
                        log_events,
                        f"   [CHANNEL SCAN] Checking channel {handle} uploads...",
                        "INFO",
                        "YOUTUBE",
                        "CHANNEL_SCAN",
                        match_id=match.get("id"),
                    )

                    is_league = bool(league_handle and handle.lower() == league_handle.lower())
                    raw_items, source_mode = fetch_channel_uploads(
                        handle, c_id, scan_limit, self.get_safe_quota(), match.get("id"), log_events,
                    )

                    oldest_pub: datetime | None = None
                    matched_items = []
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
                                if oldest_pub is None or p_dt < oldest_pub:
                                    oldest_pub = p_dt
                                if not (match_start - timedelta(hours=2) <= p_dt <= match_start + timedelta(hours=96)):
                                    continue
                            except Exception:
                                pass

                        norm_t = normalize_for_alias_matching(title)
                        has_h = any(matches_alias(a, norm_t) for a in h_aliases)
                        has_a = any(matches_alias(a, norm_t) for a in a_aliases)

                        # Entity-First Discovery on verified official target channels:
                        # If both teams match in title, allow through to Stage 1 & Stage 2 ($0 Cloud LLM)
                        is_matched = False
                        if has_h and has_a:
                            is_matched = True
                        elif not is_league:
                            target_a = a_aliases if handle == get_club_handle(home) else h_aliases
                            desc = snip.get("description", "")
                            norm_d = normalize_for_alias_matching(desc)
                            opponent_found = any(matches_alias(a, norm_t) for a in target_a) or any(
                                matches_alias(a, norm_d) for a in target_a
                            )
                            if opponent_found and (has_highlight_keyword(title) or has_scoreline(title)):
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

                    rss_fully_covers_match = bool(
                        source_mode == "rss"
                        and raw_items
                        and oldest_pub is not None
                        and oldest_pub <= (match_start - timedelta(hours=2))
                    )
                    if not matched_items and not rss_fully_covers_match and (allow_search_api or self.get_safe_quota() >= 500):
                        self.cycle_search_api_calls += 1
                        matched_items.extend(
                            run_api_search_fallback(
                                query, handle, c_id, pub_after, pub_before, match.get("id"), log_events,
                            ),
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

                    candidates.extend(filter_and_enrich_matched_items(matched_items, handle, match, log_events))
                except Exception as e:
                    logger.debug(f"[CandidateFinder] Channel {handle} scan error: {e}")

        return candidates, target_handles, query
