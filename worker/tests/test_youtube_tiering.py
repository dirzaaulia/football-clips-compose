from datetime import UTC, datetime, timedelta
import pytest
from worker.services.highlight_service import highlight_service


def test_tier1_fresh_finish_cooldown():
    now = datetime(2026, 10, 11, 14, 0, tzinfo=UTC)
    # Match finished 2 hours ago (Tier 1: < 6h, cooldown 30m)
    match_time = now - timedelta(hours=2)

    # 1. Never checked -> True
    match_new = {"utc_date": match_time.isoformat(), "last_youtube_check": None}
    should, reason = highlight_service.should_check_youtube_for_match(match_new, now)
    assert should is True

    # 2. Checked 10 minutes ago -> False (cooldown is 30m)
    match_recent = {
        "utc_date": match_time.isoformat(),
        "last_youtube_check": (now - timedelta(minutes=10)).isoformat()
    }
    should, reason = highlight_service.should_check_youtube_for_match(match_recent, now)
    assert should is False
    assert "Tier 1 (<6h) active" in reason

    # 3. Checked 35 minutes ago -> True (cooldown expired)
    match_ready = {
        "utc_date": match_time.isoformat(),
        "last_youtube_check": (now - timedelta(minutes=35)).isoformat()
    }
    should, reason = highlight_service.should_check_youtube_for_match(match_ready, now)
    assert should is True
    assert "Tier 1 (<6h) expired" in reason


def test_tier2_same_day_cooldown():
    now = datetime(2026, 10, 11, 20, 0, tzinfo=UTC)
    # Match finished 12 hours ago (Tier 2: 6-24h, cooldown 120m / 2h)
    match_time = now - timedelta(hours=12)

    # Checked 60 minutes ago -> False
    match_cooldown = {
        "utc_date": match_time.isoformat(),
        "last_youtube_check": (now - timedelta(minutes=60)).isoformat()
    }
    should, reason = highlight_service.should_check_youtube_for_match(match_cooldown, now)
    assert should is False
    assert "Tier 2 (6-24h) active" in reason

    # Checked 130 minutes ago -> True
    match_ready = {
        "utc_date": match_time.isoformat(),
        "last_youtube_check": (now - timedelta(minutes=130)).isoformat()
    }
    should, reason = highlight_service.should_check_youtube_for_match(match_ready, now)
    assert should is True


def test_tier4_old_archive_cooldown():
    now = datetime(2026, 10, 11, 20, 0, tzinfo=UTC)
    # Match finished 5 days ago (Tier 4: >72h, cooldown 24h / 1440m)
    match_time = now - timedelta(days=5)

    # Checked 8 hours ago -> False
    match_cooldown = {
        "utc_date": match_time.isoformat(),
        "last_youtube_check": (now - timedelta(hours=8)).isoformat()
    }
    should, reason = highlight_service.should_check_youtube_for_match(match_cooldown, now)
    assert should is False
    assert "Tier 4 (>72h) active" in reason

    # Checked 25 hours ago -> True
    match_ready = {
        "utc_date": match_time.isoformat(),
        "last_youtube_check": (now - timedelta(hours=25)).isoformat()
    }
    should, reason = highlight_service.should_check_youtube_for_match(match_ready, now)
    assert should is True


def test_priority_sorting_newest_first():
    now = datetime(2026, 10, 14, 12, 0, tzinfo=UTC)

    # Historical match: 10 days ago (Tier 3)
    m_historical = {"id": 1, "utc_date": (now - timedelta(days=10)).isoformat(), "last_youtube_check": None}
    # Recent match: 30 hours ago (Tier 2)
    m_recent = {"id": 2, "utc_date": (now - timedelta(hours=30)).isoformat(), "last_youtube_check": None}
    # Fresh match: 2 hours ago (Tier 1)
    m_fresh_1 = {"id": 3, "utc_date": (now - timedelta(hours=2)).isoformat(), "last_youtube_check": None}
    # Fresh match: 5 hours ago (Tier 1)
    m_fresh_2 = {"id": 4, "utc_date": (now - timedelta(hours=5)).isoformat(), "last_youtube_check": None}
    # Fresh match: 1 hour ago but already checked (Tier 1)
    m_fresh_checked = {"id": 5, "utc_date": (now - timedelta(hours=1)).isoformat(), "last_youtube_check": (now - timedelta(minutes=40)).isoformat()}

    matches = [m_historical, m_recent, m_fresh_2, m_fresh_checked, m_fresh_1]
    sorted_matches = highlight_service.sort_unlinked_matches_by_priority(matches, now)

    # Expectation:
    # Tier 1 never-checked: m_fresh_1 (2h ago) before m_fresh_2 (5h ago)
    # Tier 1 checked: m_fresh_checked
    # Tier 2 never-checked: m_recent
    # Tier 3 never-checked: m_historical
    sorted_ids = [m["id"] for m in sorted_matches]
    assert sorted_ids == [3, 4, 5, 2, 1]

