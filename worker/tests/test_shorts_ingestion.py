from worker.models.short_video import FootballShortCandidate, ShortCategory
from worker.services.shorts_ingestion_service import ShortsIngestionService


def test_is_candidate_short_detection():
    service = ShortsIngestionService()

    # Short with duration <= 65s
    assert service._is_candidate_short({"duration": 45, "title": "Bicycle kick"}) is True
    # Long video without tag
    assert service._is_candidate_short({"duration": 720, "title": "Full Match Replay"}) is False
    # Long video with #shorts tag
    assert service._is_candidate_short({"duration": 120, "title": "Unreal goal #Shorts"}) is True


def test_evaluate_candidate_heuristics():
    service = ShortsIngestionService()

    # Commercial spam candidate
    spam_candidate = FootballShortCandidate(
        video_id="abc12345678",
        title="Shop the new 2026/27 Away Kit Now at Official Store!",
        channel_id="UC123",
        channel_name="Arsenal",
        thumbnail_url="https://img.youtube.com/vi/abc12345678/hqdefault.jpg",
        published_at="2026-11-08T00:00:00Z",
    )
    result = service._evaluate_candidate(spam_candidate)
    assert result.verdict == "REJECTED"
    assert result.category == ShortCategory.COMMERCIAL_SPAM

    # On-pitch action candidate
    action_candidate = FootballShortCandidate(
        video_id="xyz98765432",
        title="Bukayo Saka unreal solo goal vs Liverpool! 🎯",
        channel_id="UC123",
        channel_name="Arsenal",
        thumbnail_url="https://img.youtube.com/vi/xyz98765432/hqdefault.jpg",
        published_at="2026-11-08T00:00:00Z",
    )
    res_action = service._evaluate_candidate(action_candidate)
    assert res_action.verdict == "ACCEPTED"
    assert res_action.category == ShortCategory.GOALS_SKILLS
