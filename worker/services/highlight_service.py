"""
Unified Highlight Engine Facade
Provides clean hexagonal interface to highlight search, ranking, batching, and websub matching.
"""

from typing import Any

from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings
from worker.services.highlight.batch_coordinator import BatchCoordinator
from worker.services.highlight.candidate_finder import CandidateFinder
from worker.services.highlight.candidate_ranker import CandidateRanker
from worker.services.highlight.cooldown import (
    COMPETITION_PRIORITY,
    parse_iso_date,
    should_check_youtube_for_match,
    sort_unlinked_matches_by_priority,
)
from worker.services.highlight.filters import (
    KEY_FOOTBALL_MARKETS,
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
from worker.clients.database_client import db_client as supabase_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.services.notification_service import notification_service
from worker.services.highlight.quota_maximizer import QuotaMaximizer
from worker.services.highlight.scorer import fallback_score_video
from worker.services.highlight.search_pipeline import SearchPipeline
from worker.services.highlight.websub_matcher import WebsubMatcher


class HighlightService:
    def __init__(self):
        self.search_pipeline = SearchPipeline(self.get_safe_quota_remaining)
        self.quota_maximizer = QuotaMaximizer(self.search_pipeline, self.mark_match_unavailable)
        self.coordinator = BatchCoordinator(self.search_pipeline, self.quota_maximizer)
        self.cycle_search_api_calls = 0

    @staticmethod
    def get_safe_quota_remaining() -> int:
        try:
            return youtube_client.get_quota_status().get("remaining", settings.max_quota_per_day)
        except Exception:
            return 5000

    def search_highlight_for_match(
        self,
        match: dict[str, Any],
        log_events: list[dict[str, Any]] | None = None,
        claimed_video_ids: set[str] | None = None,
        allow_search_api: bool = False,
    ) -> tuple[str | None, dict[str, Any] | None, str | None, dict[str, Any] | None, list[dict[str, Any]]]:
        return self.search_pipeline.search_highlight_for_match(match, log_events, claimed_video_ids, allow_search_api)

    def mark_match_unavailable(
        self,
        match: dict[str, Any],
        reason: str = "NO_OFFICIAL_BROADCASTER",
        log_events: list[dict[str, Any]] | None = None,
    ) -> None:
        self.coordinator.mark_match_unavailable(match, reason, log_events)

    def sync_unlinked_highlights(
        self,
        max_batch: int = 25,
        log_events: list[dict[str, Any]] | None = None,
    ) -> int:
        return self.coordinator.sync_unlinked_highlights(max_batch, log_events)

    def maximize_daily_quota_usage(
        self,
        max_batch: int = 10,
        log_events: list[dict[str, Any]] | None = None,
        claimed_video_ids: set[str] | None = None,
    ) -> int:
        return self.quota_maximizer.maximize_daily_quota_usage(max_batch, log_events, claimed_video_ids)

    def link_websub_video(
        self,
        video_id: str,
        title: str,
        channel_id: str,
        author_name: str = "",
        log_events: list[dict[str, Any]] | None = None,
    ) -> bool:
        return WebsubMatcher.link_websub_video(video_id, title, channel_id, author_name, log_events)

    def process_single_match_highlight(
        self,
        match_id: int | str,
        log_events: list[dict[str, Any]] | None = None,
    ) -> dict[str, Any]:
        from worker.services.single_highlight_processor import process_single_match_highlight

        return process_single_match_highlight(self, match_id, log_events)

    def process_websub_video(
        self,
        video_id: str,
        title: str,
        channel_id: str,
        author_name: str = "",
        published_at: str | None = None,
        log_events: list[dict[str, Any]] | None = None,
    ) -> dict[str, Any]:
        from worker.services.websub_processor import process_websub_video

        return process_websub_video(self, video_id, title, channel_id, author_name, published_at, log_events)

    @staticmethod
    def get_video_region_info(video_id: str) -> tuple[dict | None, int, bool]:
        durations, regions, embeddables = youtube_client.get_video_details([video_id])
        return regions.get(video_id), durations.get(video_id, 0), embeddables.get(video_id, True)

    # Re-export helper methods as class/instance attributes for test & caller compatibility
    clean_team_name_for_search = staticmethod(clean_team_name_for_search)
    get_club_handle = staticmethod(get_club_handle)
    get_team_aliases = staticmethod(get_team_aliases)
    matches_alias = staticmethod(matches_alias)
    normalize_for_alias_matching = staticmethod(normalize_for_alias_matching)
    check_forbidden_title = staticmethod(check_forbidden_title)
    has_highlight_keyword = staticmethod(has_highlight_keyword)
    has_scoreline = staticmethod(has_scoreline)
    classify_candidate_tier = staticmethod(classify_candidate_tier)
    get_geoblock_status = staticmethod(get_geoblock_status)
    get_worldwide_watchability_score = staticmethod(get_worldwide_watchability_score)
    is_target_market_catered = staticmethod(is_target_market_catered)
    fallback_score_video = staticmethod(fallback_score_video)
    should_check_youtube_for_match = staticmethod(should_check_youtube_for_match)
    sort_unlinked_matches_by_priority = staticmethod(sort_unlinked_matches_by_priority)
    parse_iso_date = staticmethod(parse_iso_date)


highlight_service = HighlightService()
