from unittest.mock import MagicMock, patch
from worker.services.notification_service import NotificationService


def test_notification_deduplication():
    service = NotificationService()
    # Mock _send_to_club_topics to avoid real FCM network calls in unit tests
    service._send_to_club_topics = MagicMock(return_value=2)

    sample_match = {
        "id": 999123,
        "home_team": "Arsenal FC",
        "away_team": "Chelsea FC",
        "home_team_id": 57,
        "away_team_id": 61,
        "home_score": 1,
        "away_score": 0,
        "competition_name": "Premier League"
    }

    # First event should send
    sent1 = service.notify_match_event(sample_match, "score")
    assert sent1 is True
    service._send_to_club_topics.assert_called_once()

    # Identical score event should be suppressed by deduplication
    service._send_to_club_topics.reset_mock()
    sent2 = service.notify_match_event(sample_match, "score")
    assert sent2 is False
    service._send_to_club_topics.assert_not_called()

    # Different score should send
    sample_match["home_score"] = 2
    sent3 = service.notify_match_event(sample_match, "score")
    assert sent3 is True
    service._send_to_club_topics.assert_called_once()


def test_highlight_notification():
    service = NotificationService()
    service._send_to_club_topics = MagicMock(return_value=2)

    sample_match = {
        "id": 999124,
        "home_team": "Real Madrid CF",
        "away_team": "FC Barcelona",
        "home_team_id": 86,
        "away_team_id": 81,
    }

    sent = service.notify_highlight_ready(sample_match, "vid_abc123", "Highlights Video")
    assert sent is True
    service._send_to_club_topics.assert_called_once()
    assert service._send_to_club_topics.call_args[0][0] == [86, 81]

    # Duplicate highlight send check
    service._send_to_club_topics.reset_mock()
    sent_dup = service.notify_highlight_ready(sample_match, "vid_abc123", "Highlights Video")
    assert sent_dup is False
    service._send_to_club_topics.assert_not_called()
