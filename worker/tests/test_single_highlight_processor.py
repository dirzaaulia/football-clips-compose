import pytest
from unittest.mock import MagicMock, patch
from worker.services.single_highlight_processor import process_single_match_highlight


def test_process_single_match_highlight_not_found():
    mock_service = MagicMock()
    with patch("worker.services.single_highlight_processor.supabase_client") as mock_sb:
        mock_sb.table.return_value.select.return_value.eq.return_value.execute.return_value.data = []
        result = process_single_match_highlight(mock_service, match_id=999999)
        assert result["status"] == "NOT_FOUND"


def test_process_single_match_highlight_already_linked():
    mock_service = MagicMock()
    mock_service.get_worldwide_watchability_score.return_value = 100.0

    mock_match = {
        "id": 1001,
        "home_team_name": "Liverpool",
        "away_team_name": "Man City",
        "highlight_video_id": "existing123",
        "youtube_region_info": {"allowed": []},
    }

    with patch("worker.services.single_highlight_processor.supabase_client") as mock_sb:
        mock_sb.table.return_value.select.return_value.eq.return_value.execute.return_value.data = [mock_match]
        result = process_single_match_highlight(mock_service, match_id=1001)
        assert result["status"] == "ALREADY_LINKED"
        assert result["video_id"] == "existing123"


def test_process_single_match_highlight_links_new_video():
    mock_service = MagicMock()
    mock_service.get_worldwide_watchability_score.return_value = 100.0
    mock_service.search_highlight_for_match.return_value = (
        "newvid12345",
        {"allowed": []},
        None,
        None,
        [{"video_id": "newvid12345"}],
    )

    mock_match = {
        "id": 2002,
        "home_team_name": "Barcelona",
        "away_team_name": "Real Madrid",
        "highlight_video_id": None,
        "youtube_region_info": None,
    }

    with patch("worker.services.single_highlight_processor.supabase_client") as mock_sb, \
         patch("worker.services.single_highlight_processor.edge_cache_client") as mock_edge, \
         patch("worker.services.single_highlight_processor.notification_service") as mock_notify:

        # First call is select match, second call is select claimed videos
        mock_sb.table.return_value.select.return_value.eq.return_value.execute.return_value.data = [mock_match]
        mock_sb.table.return_value.select.return_value.not_.is_.return_value.execute.return_value.data = []

        log_events = []
        result = process_single_match_highlight(mock_service, match_id=2002, log_events=log_events)

        assert result["status"] == "LINKED"
        assert result["video_id"] == "newvid12345"
        mock_sb.table.return_value.update.assert_called_once()
        mock_edge.purge_matches_cache.assert_called_once()
        mock_notify.notify_highlight_ready.assert_called_once()
        assert any(getattr(e, "code", None) == "DB_LINKED" for e in log_events)


def test_process_websub_video_forbidden_title():
    from worker.services.single_highlight_processor import process_websub_video
    mock_service = MagicMock()
    mock_service.check_forbidden_title.return_value = (True, "Contains press conference")

    result = process_websub_video(
        mock_service,
        video_id="vid_forbidden",
        title="Arteta Post Match Press Conference Highlights",
        channel_id="UC_arsenal",
    )
    assert result["status"] == "FORBIDDEN"
    assert "press conference" in result["reason"]


def test_process_websub_video_links_multi_market():
    from worker.services.single_highlight_processor import process_websub_video
    mock_service = MagicMock()
    mock_service.check_forbidden_title.return_value = (False, "")
    mock_service.has_highlight_keyword.return_value = True
    mock_service.get_team_aliases.side_effect = lambda team: [team.lower()]
    # Region info allowed in GB (Sky/TNT sports in target trio ['ID', 'GB', 'US'])
    mock_service.get_video_region_info.return_value = ({"allowed": ["GB"]}, 180, True)

    mock_match = {
        "id": 5005,
        "home_team_name": "Arsenal",
        "away_team_name": "Chelsea",
        "competition_name": "Premier League",
        "highlight_video_id": None,
        "utc_date": "2026-11-01T15:00:00Z",
    }

    with patch("worker.services.single_highlight_processor.supabase_client") as mock_sb, \
         patch("worker.services.single_highlight_processor.ai_gateway_client") as mock_ai, \
         patch("worker.services.single_highlight_processor.edge_cache_client") as mock_edge, \
         patch("worker.services.single_highlight_processor.notification_service") as mock_notify:

        mock_sb.table.return_value.select.return_value.eq.return_value.gte.return_value.order.return_value.limit.return_value.execute.return_value.data = [mock_match]
        mock_ai.rerank_highlights.return_value = (0, "Official match recap", "DeepSeek-R1")

        log_events = []
        result = process_websub_video(
            mock_service,
            video_id="valid_gb_vid",
            title="Arsenal vs Chelsea Highlights",
            channel_id="UC_sky",
            author_name="Sky Sports",
            log_events=log_events,
        )

        assert result["status"] == "LINKED"
        assert result["video_id"] == "valid_gb_vid"
        mock_sb.table.return_value.update.assert_called_once()
        mock_edge.purge_matches_cache.assert_called_once()
        mock_notify.notify_highlight_ready.assert_called_once()


def test_process_websub_video_geo_blocked_when_outside_target_markets():
    from worker.services.single_highlight_processor import process_websub_video
    mock_service = MagicMock()
    mock_service.check_forbidden_title.return_value = (False, "")
    mock_service.has_highlight_keyword.return_value = True
    mock_service.get_team_aliases.side_effect = lambda team: [team.lower()]
    # Allowed only in Australia, outside target markets ['ID', 'GB', 'US']
    mock_service.get_video_region_info.return_value = ({"allowed": ["AU"]}, 180, True)

    mock_match = {
        "id": 5006,
        "home_team_name": "Arsenal",
        "away_team_name": "Chelsea",
        "competition_name": "Premier League",
        "highlight_video_id": None,
        "utc_date": "2026-11-01T15:00:00Z",
    }

    with patch("worker.services.single_highlight_processor.supabase_client") as mock_sb, \
         patch("worker.services.single_highlight_processor.ai_gateway_client") as mock_ai:

        mock_sb.table.return_value.select.return_value.eq.return_value.gte.return_value.order.return_value.limit.return_value.execute.return_value.data = [mock_match]
        mock_ai.rerank_highlights.return_value = (0, "Official match recap", "DeepSeek-R1")

        result = process_websub_video(
            mock_service,
            video_id="au_only_vid",
            title="Arsenal vs Chelsea Highlights",
            channel_id="UC_stan",
            author_name="Stan Sport",
        )

        assert result["status"] == "GEO_BLOCKED"
