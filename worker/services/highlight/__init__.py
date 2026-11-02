from worker.services.highlight.normalizer import (
    clean_team_name_for_search,
    get_club_handle,
    get_team_aliases,
    matches_alias,
    normalize_for_alias_matching,
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
from worker.services.highlight.scorer import fallback_score_video
from worker.services.highlight.cooldown import (
    COMPETITION_PRIORITY,
    parse_iso_date,
    should_check_youtube_for_match,
    sort_unlinked_matches_by_priority,
)

__all__ = [
    "clean_team_name_for_search",
    "get_club_handle",
    "get_team_aliases",
    "matches_alias",
    "normalize_for_alias_matching",
    "KEY_FOOTBALL_MARKETS",
    "check_forbidden_title",
    "classify_candidate_tier",
    "get_geoblock_status",
    "get_worldwide_watchability_score",
    "has_highlight_keyword",
    "has_scoreline",
    "is_target_market_catered",
    "fallback_score_video",
    "COMPETITION_PRIORITY",
    "parse_iso_date",
    "should_check_youtube_for_match",
    "sort_unlinked_matches_by_priority",
]
