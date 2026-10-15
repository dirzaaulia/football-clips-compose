from datetime import UTC, datetime, timedelta
import pytest
from worker.services.highlight_service import highlight_service


def test_throwback_keywords_rejected():
    """Historical/retro videos disguised as highlights must be severely penalized."""
    score = highlight_service.fallback_score_video(
        title="Arsenal vs Chelsea Classic Clash | Iconic Goals",
        duration_sec=300,
        home_team="Arsenal FC",
        away_team="Chelsea FC",
        h_aliases=["Arsenal", "Gunners"],
        a_aliases=["Chelsea", "Blues"],
        published_at="2026-10-15T20:00:00Z",
        match_utc="2026-10-15T18:00:00Z",
        channel_handle="@arsenal",
        official_channel_targets=["@arsenal"],
    )
    assert score <= -100, f"Expected instant rejection, got {score}"


def test_past_year_throwback_rejected():
    """Old season highlight (e.g. 2022) must be rejected for a 2026 fixture."""
    score = highlight_service.fallback_score_video(
        title="Real Madrid vs Barcelona 2022 Extended Highlights",
        duration_sec=600,
        home_team="Real Madrid CF",
        away_team="FC Barcelona",
        h_aliases=["Real Madrid", "Madrid"],
        a_aliases=["Barcelona", "Barca"],
        published_at="2026-10-15T20:00:00Z",
        match_utc="2026-10-15T18:00:00Z",
        channel_handle="@LaLiga",
        official_channel_targets=["@LaLiga"],
    )
    assert score <= -100, f"Expected past year rejection, got {score}"


def test_side_content_press_interview_rejected():
    """Press conference or interview videos must be rejected."""
    score = highlight_service.fallback_score_video(
        title="Mikel Arteta Post-Match Press Conference | Arsenal vs Chelsea",
        duration_sec=400,
        home_team="Arsenal FC",
        away_team="Chelsea FC",
        h_aliases=["Arsenal"],
        a_aliases=["Chelsea"],
        published_at="2026-10-15T20:00:00Z",
        match_utc="2026-10-15T18:00:00Z",
    )
    assert score <= -100, f"Expected non-highlight side content rejection, got {score}"


def test_pre_match_video_rejected():
    """Videos published BEFORE kickoff must be rejected."""
    score = highlight_service.fallback_score_video(
        title="Arsenal vs Chelsea Match Preview & Highlights",
        duration_sec=300,
        home_team="Arsenal FC",
        away_team="Chelsea FC",
        h_aliases=["Arsenal"],
        a_aliases=["Chelsea"],
        published_at="2026-10-15T10:00:00Z",  # 8 hours before kickoff
        match_utc="2026-10-15T18:00:00Z",
    )
    assert score <= -100, f"Expected pre-match rejection, got {score}"


def test_exact_scoreline_bonus_vs_contradictory_penalty():
    """Verified scoreline must receive high bonus; contradictory scoreline must be penalized."""
    # Match finished 3-1
    exact_score = highlight_service.fallback_score_video(
        title="Arsenal 3 - 1 Chelsea | Extended Highlights",
        duration_sec=450,
        home_team="Arsenal FC",
        away_team="Chelsea FC",
        h_aliases=["Arsenal"],
        a_aliases=["Chelsea"],
        home_score=3,
        away_score=1,
        published_at="2026-10-15T20:30:00Z",
        match_utc="2026-10-15T18:00:00Z",
        channel_handle="@arsenal",
        official_channel_targets=["@arsenal"],
    )

    contradictory_score = highlight_service.fallback_score_video(
        title="Arsenal 0 - 2 Chelsea | Highlights",
        duration_sec=450,
        home_team="Arsenal FC",
        away_team="Chelsea FC",
        h_aliases=["Arsenal"],
        a_aliases=["Chelsea"],
        home_score=3,
        away_score=1,
        published_at="2026-10-15T20:30:00Z",
        match_utc="2026-10-15T18:00:00Z",
        channel_handle="@arsenal",
        official_channel_targets=["@arsenal"],
    )

    assert exact_score >= 80, f"Exact scoreline should score very high, got {exact_score}"
    assert contradictory_score < exact_score - 70, f"Contradictory scoreline must have massive penalty (got {contradictory_score} vs {exact_score})"


def test_word_boundary_alias_safety():
    """Ensure 'Real' does not match 'Villarreal', and 'City' does not match 'Bristol City'."""
    # Searching for Villarreal vs Real Betis
    score_villarreal = highlight_service.fallback_score_video(
        title="Villarreal vs Real Betis Highlights (1-0)",
        duration_sec=300,
        home_team="Real Madrid CF",
        away_team="FC Barcelona",
        h_aliases=["Real Madrid", "Madrid"],  # Should NOT match Villarreal or Real Betis as Real Madrid
        a_aliases=["Barcelona", "Barca"],
        home_score=1,
        away_score=0,
        published_at="2026-10-15T20:30:00Z",
        match_utc="2026-10-15T18:00:00Z",
    )
    # Neither Real Madrid nor Barcelona are in this title
    assert score_villarreal < 0 or score_villarreal < 30, f"Should not match unrelated teams, got {score_villarreal}"


def test_official_channel_authority_boost():
    """Official channel upload should outscore third-party upload."""
    official_score = highlight_service.fallback_score_video(
        title="Arsenal vs Chelsea | Highlights",
        duration_sec=400,
        home_team="Arsenal FC",
        away_team="Chelsea FC",
        h_aliases=["Arsenal"],
        a_aliases=["Chelsea"],
        home_score=2,
        away_score=2,
        published_at="2026-10-15T20:30:00Z",
        match_utc="2026-10-15T18:00:00Z",
        channel_handle="@arsenal",
        official_channel_targets=["@arsenal"],
    )

    third_party_score = highlight_service.fallback_score_video(
        title="Arsenal vs Chelsea | Highlights",
        duration_sec=400,
        home_team="Arsenal FC",
        away_team="Chelsea FC",
        h_aliases=["Arsenal"],
        a_aliases=["Chelsea"],
        home_score=2,
        away_score=2,
        published_at="2026-10-15T20:30:00Z",
        match_utc="2026-10-15T18:00:00Z",
        channel_handle="@random_reuploader",
        official_channel_targets=["@arsenal"],
    )

    assert official_score - third_party_score >= 25, f"Official channel should have +25 authority bonus, got diff: {official_score - third_party_score}"


def test_late_upload_decay():
    """Video uploaded > 5 days after match must receive a heavy decay penalty."""
    fresh_score = highlight_service.fallback_score_video(
        title="Arsenal vs Chelsea Highlights (2-1)",
        duration_sec=400,
        home_team="Arsenal FC",
        away_team="Chelsea FC",
        h_aliases=["Arsenal"],
        a_aliases=["Chelsea"],
        home_score=2,
        away_score=1,
        published_at="2026-10-15T20:00:00Z",  # 2 hours post-match
        match_utc="2026-10-15T18:00:00Z",
    )

    late_score = highlight_service.fallback_score_video(
        title="Arsenal vs Chelsea Highlights (2-1)",
        duration_sec=400,
        home_team="Arsenal FC",
        away_team="Chelsea FC",
        h_aliases=["Arsenal"],
        a_aliases=["Chelsea"],
        home_score=2,
        away_score=1,
        published_at="2026-10-22T18:00:00Z",  # 7 days post-match
        match_utc="2026-10-15T18:00:00Z",
    )

    assert fresh_score - late_score >= 50, f"Fresh upload must strongly beat late upload, got {fresh_score} vs {late_score}"


def test_real_betis_does_not_match_real_madrid():
    """Video with 'Real Betis vs Celta Vigo' must NOT match Real Madrid."""
    score = highlight_service.fallback_score_video(
        title="Real Betis vs Celta Vigo Highlights (2-0)",
        duration_sec=350,
        home_team="Real Madrid CF",
        away_team="FC Barcelona",
        h_aliases=["Real Madrid", "Madrid"],  # 'Real' alone is not an alias; 'Madrid' does not appear
        a_aliases=["Barcelona", "Barca"],
        home_score=2,
        away_score=0,
        published_at="2026-10-15T20:00:00Z",
        match_utc="2026-10-15T18:00:00Z",
    )
    assert score == -100, f"Unrelated team video must be completely rejected, got {score}"

