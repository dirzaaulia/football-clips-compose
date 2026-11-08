from worker.models.short_video import FootballShortCandidate, ShortCategory
from worker.services.shorts_ingestion_service import ShortsIngestionService


def test_pitch_radar_coordinate_contract():
    """Validates that any 2D canvas coordinates comply with [0..100] normalized pitch contract."""
    sample_roster = [
        {"name": "Raya", "position": "G", "x": 10.0, "y": 50.0},
        {"name": "Saliba", "position": "D", "x": 30.0, "y": 65.0},
        {"name": "Gabriel", "position": "D", "x": 30.0, "y": 35.0},
        {"name": "Saka", "position": "F", "x": 80.0, "y": 85.0},
    ]

    for player in sample_roster:
        assert 0.0 <= player["x"] <= 100.0, f"X coordinate {player['x']} out of bounds for {player['name']}"
        assert 0.0 <= player["y"] <= 100.0, f"Y coordinate {player['y']} out of bounds for {player['name']}"


def test_shorts_ai_pipeline_diagnostics():
    """Validates multi-tier AI evaluation pipeline rejecting spam and categorizing goals."""
    service = ShortsIngestionService()

    candidates = [
        FootballShortCandidate(
            video_id="vid_merch_01",
            title="Grab the 2026/27 Training Kit 20% OFF! #shorts",
            channel_id="UC_club",
            channel_name="Arsenal",
            thumbnail_url="https://img.youtube.com/vi/vid_merch_01/hqdefault.jpg",
            published_at="2026-11-08T00:00:00Z",
        ),
        FootballShortCandidate(
            video_id="vid_goal_02",
            title="Declan Rice incredible 95th minute curler! 🚀 #shorts",
            channel_id="UC_club",
            channel_name="Arsenal",
            thumbnail_url="https://img.youtube.com/vi/vid_goal_02/hqdefault.jpg",
            published_at="2026-11-08T00:00:00Z",
        ),
        FootballShortCandidate(
            video_id="vid_press_03",
            title="Mikel Arteta Post-Match Press Conference vs Chelsea",
            channel_id="UC_club",
            channel_name="Arsenal",
            thumbnail_url="https://img.youtube.com/vi/vid_press_03/hqdefault.jpg",
            published_at="2026-11-08T00:00:00Z",
        ),
    ]

    # Evaluate 1: Merch spam
    eval_1 = service._evaluate_candidate(candidates[0])
    assert eval_1.verdict == "REJECTED"
    assert eval_1.category == ShortCategory.COMMERCIAL_SPAM

    # Evaluate 2: Action Goal
    eval_2 = service._evaluate_candidate(candidates[1])
    assert eval_2.verdict == "ACCEPTED"
    assert eval_2.category == ShortCategory.GOALS_SKILLS

    # Evaluate 3: Press Conference
    eval_3 = service._evaluate_candidate(candidates[2])
    assert eval_3.verdict == "REJECTED"
    assert eval_3.category == ShortCategory.PRESS_CONFERENCE
