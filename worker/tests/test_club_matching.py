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


def test_espanyol_does_not_match_barcelona():
    service = HighlightService()
    assert service.get_club_handle("RCD Espanyol de Barcelona") != "@fcbarcelona"
    assert service.get_club_handle("RCD Espanyol") != "@fcbarcelona"
    assert service.get_club_handle("FC Barcelona") == "@fcbarcelona"


def test_league_channel_routing():
    from worker.repositories.channel_repository import channel_repository

    # Verify official league channels exist in channel_repository
    for code, expected_handle in [("PD", "@LaLiga"), ("BL1", "@bundesliga"), ("FL1", "@ligue1"), ("SA", "@SerieA"), ("MLS", "@MLS")]:
        handle, cid = channel_repository.get_competition_channel(code)
        assert handle == expected_handle, f"Expected {expected_handle} for {code}, got {handle}"
        assert cid.startswith("UC"), f"Channel ID must start with UC: {code} -> {cid}"


def test_permanent_channel_ids_integrity():
    from worker.repositories.channel_repository import channel_repository

    channel_repository.ensure_loaded()
    for handle, channel_id in channel_repository._channel_ids_by_handle.items():
        assert handle.startswith("@"), f"Handle must start with @: {handle}"
        assert channel_id.startswith("UC"), f"Channel ID must start with UC: {handle} -> {channel_id}"
        assert len(channel_id) >= 20, f"Channel ID too short: {handle} -> {channel_id}"


def test_lens_vs_sporting_cp_matching():
    service = HighlightService()
    home = "Racing Club de Lens"
    away = "Sporting Clube de Portugal"

    h_aliases = service.get_team_aliases(home)
    a_aliases = service.get_team_aliases(away)

    assert "lens" in h_aliases or "rc lens" in h_aliases
    assert "sporting cp" in a_aliases or "sporting" in a_aliases

    title = "Highlights | UEFA Champions League: RC Lens 0-2 Sporting CP"
    t_lower = title.lower()
    has_h = any(a in t_lower for a in h_aliases)
    has_a = any(a in t_lower for a in a_aliases)
    assert has_h, f"Expected home team alias in '{title}'"
    assert has_a, f"Expected away team alias in '{title}'"


def test_geoblock_status():
    service = HighlightService()

    # Global video
    pen, desc = service.get_geoblock_status(None)
    assert pen == 0
    assert "Global" in desc

    # Whitelist
    pen_wl, desc_wl = service.get_geoblock_status({"allowed": ["PT", "FR"]})
    assert pen_wl > 0
    assert "Whitelist" in desc_wl

    # Total geoblock
    pen_block, desc_block = service.get_geoblock_status({"allowed": []})
    assert pen_block >= 9999


