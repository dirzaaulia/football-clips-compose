import pytest
from unittest.mock import AsyncMock, patch

from worker.config.settings import settings
from worker.services.match_service import match_service


@pytest.mark.anyio
async def test_match_service_with_espn_provider():
    mock_espn_matches = [
        {
            "id": 700101,
            "competition_id": "PL",
            "competition_name": "Premier League",
            "utc_date": "2026-10-17T11:30:00+00:00",
            "status": "TIMED",
            "matchday": 7,
            "round_name": "Matchweek 7",
            "display_clock": "0'",
            "home_team_id": 359,
            "home_team_name": "Arsenal",
            "home_team_crest": "https://a.espncdn.com/i/teamlogos/soccer/500/359.png",
            "away_team_id": 363,
            "away_team_name": "Chelsea",
            "away_team_crest": "https://a.espncdn.com/i/teamlogos/soccer/500/363.png",
            "home_score": None,
            "away_score": None,
            "provider": "espn",
        }
    ]

    with patch.object(settings, "match_data_provider", "espn"):
        with patch("worker.services.match_service.espn_client.get_matches", new=AsyncMock(return_value=mock_espn_matches)):
            with patch("worker.services.match_service.supabase_client") as mock_sb:
                # Mock select returning empty (new fixture)
                mock_select = AsyncMock()
                mock_select.data = []
                mock_sb.table.return_value.select.return_value.in_.return_value.execute.return_value = mock_select
                mock_sb.table.return_value.upsert.return_value.execute.return_value = None

                log_events = []
                synced = await match_service.sync_matches(is_full_sync=False, log_events=log_events)

                assert len(synced) == 1
                assert synced[0]["id"] == 700101
                assert synced[0]["round_name"] == "Matchweek 7"
                assert synced[0]["display_clock"] == "0'"
                assert synced[0]["provider"] == "espn"
                assert synced[0]["home_team_name"] == "Arsenal"
