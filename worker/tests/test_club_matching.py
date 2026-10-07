from worker.services.highlight_service import HighlightService


def test_clean_team_names():
    service = HighlightService()

    cases = [
        ("Arsenal FC", "arsenal"),
        ("1. FC Köln", "köln"),
        ("Borussia Dortmund", "dortmund"),
        ("Manchester United FC", "man united"),
        ("Club Atlético de Madrid", "atletico madrid"),
        ("Paris Saint-Germain FC", "psg"),
        ("AFC Bournemouth", "bournemouth"),
    ]

    for raw, expected in cases:
        cleaned = service.clean_team_name_for_search(raw)
        assert expected in cleaned.lower(), f"Expected '{expected}' in '{cleaned}'"

def test_club_handle_resolution():
    service = HighlightService()

    handles = [
        ("Arsenal FC", "@arsenal"),
        ("Manchester City FC", "@mancity"),
        ("Sunderland AFC", "@sunderlandafc"),
        ("Leeds United FC", "@leedsunited"),
        ("Coventry City FC", "@coventrycityfc"),
        ("Real Madrid CF", "@realmadrid"),
        ("Bayern München", "@fcbayern"),
        ("AC Milan", "@acmilan"),
    ]

    for team, expected_handle in handles:
        handle = service.get_club_handle(team)
        assert handle == expected_handle, f"Expected {expected_handle} for {team}, got {handle}"
