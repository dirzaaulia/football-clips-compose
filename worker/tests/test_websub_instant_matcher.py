from unittest.mock import MagicMock, patch
from worker.services.highlight_service import HighlightService


def test_websub_matcher_forbidden_title():
    service = HighlightService()
    # Press conference should be immediately rejected
    result = service.link_websub_video(
        video_id="abc12345",
        title="Mikel Arteta Post Match Press Conference vs Chelsea",
        channel_id="UCpryVRk_VDudG8SHXgWcG0w"
    )
    assert result is False


def test_websub_matcher_no_highlight_keyword():
    service = HighlightService()
    # No highlight keyword
    result = service.link_websub_video(
        video_id="abc12345",
        title="Bukayo Saka skills and tricks vs Chelsea",
        channel_id="UCpryVRk_VDudG8SHXgWcG0w"
    )
    assert result is False


def test_websub_matcher_successful_link():
    service = HighlightService()
    mock_matches = [
        {
            "id": 1001,
            "home_team_name": "Arsenal",
            "away_team_name": "Chelsea",
            "utc_date": "2026-10-15T19:00:00+00:00",
            "home_score": 3,
            "away_score": 1,
            "competition_name": "Premier League",
        }
    ]

    mock_query = MagicMock()
    mock_query.select.return_value = mock_query
    mock_query.eq.return_value = mock_query
    mock_query.is_.return_value = mock_query
    mock_query.gte.return_value = mock_query
    mock_query.order.return_value = mock_query
    mock_query.limit.return_value = mock_query
    mock_query.execute.return_value = MagicMock(data=mock_matches)

    mock_update = MagicMock()
    mock_update.update.return_value = mock_update
    mock_update.eq.return_value = mock_update
    mock_update.execute.return_value = MagicMock(data=[{"id": 1001}])

    with patch("worker.services.highlight_service.supabase_client.table") as mock_table, \
         patch("worker.services.highlight_service.youtube_rss_client.check_video_global_availability") as mock_avail, \
         patch("worker.services.highlight_service.notification_service.notify_highlight_ready") as mock_notify:

        def table_router(table_name):
            if table_name == "matches":
                # Return query or update mock depending on call
                m = MagicMock()
                m.select.return_value.eq.return_value.is_.return_value.gte.return_value.order.return_value.limit.return_value.execute.return_value = MagicMock(data=mock_matches)
                m.update.return_value.eq.return_value.execute.return_value = MagicMock(data=[{"id": 1001}])
                return m
            return MagicMock()

        mock_table.side_effect = table_router
        mock_avail.return_value = (True, True, 200, "100% Global")

        result = service.link_websub_video(
            video_id="match_vid_999",
            title="HIGHLIGHTS | Arsenal 3-1 Chelsea | Havertz, Saka, Martinelli",
            channel_id="UCpryVRk_VDudG8SHXgWcG0w"
        )

        assert result is True
        mock_notify.assert_called_once()
