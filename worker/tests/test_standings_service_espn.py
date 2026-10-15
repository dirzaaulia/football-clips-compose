import pytest
from worker.clients.espn_client import espn_client
from worker.services.standings_service import standings_service


@pytest.mark.anyio
async def test_espn_get_standings_pl():
    """Verify ESPN client fetches and parses Premier League standings."""
    rows = await espn_client.get_standings("PL")
    assert len(rows) == 20, f"Premier league should have 20 teams, got {len(rows)}"

    first = rows[0]
    assert first["competition_code"] == "PL"
    assert first["position"] == 1
    assert first["played_games"] > 0
    assert first["points"] >= 0
    assert first["crest_url"] is not None
    assert first["team_name"] is not None


@pytest.mark.anyio
async def test_espn_get_standings_cl():
    """Verify ESPN client fetches and parses Champions League 36-team single league table."""
    rows = await espn_client.get_standings("CL")
    assert len(rows) == 36, f"Champions league league phase should have 36 teams, got {len(rows)}"


@pytest.mark.anyio
async def test_standings_service_sync_espn():
    """Verify StandingsService runs cleanly with ESPN provider."""
    events = []
    rows = await standings_service.sync_standings(force=True, log_events=events)
    assert len(rows) == 132, f"Total standings across 6 competitions should be 132, got {len(rows)}"
    assert any("PL: 20 clubs processed via ESPN" in str(e) for e in events)
