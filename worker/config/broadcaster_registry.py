"""Global Football Broadcaster Registry & Rights Lifecycle Directory.

Tracks official YouTube channels with broadcast & highlight rights across major markets,
including territorial ISO country codes, competition assignments, and contract lifecycle windows.
"""

from typing import Any

GLOBAL_BROADCASTER_REGISTRY: list[dict[str, Any]] = [
    # ---------------------------------------------------------
    # United Kingdom & Ireland
    # ---------------------------------------------------------
    {
        "broadcaster_id": "sky_sports_uk",
        "name": "Sky Sports Football",
        "channel_handle": "@SkySportsFootball",
        "channel_id": "UCZ7wY7MRDSygp63HIEfdQZA",
        "competitions": ["PL", "EFL", "FAC"],
        "allowed_countries": ["GB", "IE"],
        "contract_start_year": 2022,
        "contract_end_year": 2029,
        "status": "ACTIVE",
    },
    {
        "broadcaster_id": "tnt_sports_uk",
        "name": "TNT Sports",
        "channel_handle": "@tntsports",
        "channel_id": "UCfDfvvMARk4TKcC62ALi6eA",
        "competitions": ["CL", "EL", "ECL", "PL", "SA", "FL1"],
        "allowed_countries": ["GB", "IE"],
        "contract_start_year": 2024,
        "contract_end_year": 2027,
        "status": "ACTIVE",
    },

    # ---------------------------------------------------------
    # United States & North America
    # ---------------------------------------------------------
    {
        "broadcaster_id": "cbs_golazo_us",
        "name": "CBS Sports Golazo",
        "channel_handle": "@CBSSportsGolazo",
        "channel_id": "UCX_tjI6Q_4JD1E3234CwemA",
        "competitions": ["CL", "EL", "ECL", "SA", "EFL"],
        "allowed_countries": ["US", "PR", "GU", "VI", "AS"],
        "contract_start_year": 2024,
        "contract_end_year": 2030,
        "status": "ACTIVE",
    },
    {
        "broadcaster_id": "espn_fc_us",
        "name": "ESPN FC",
        "channel_handle": "@espnfc",
        "channel_id": "UCiWLfSweyRNmLpgEHekhoAg",
        "competitions": ["PD", "BL1", "FAC", "CDR", "DFB", "DED"],
        "allowed_countries": ["US", "PR", "VI"],
        "contract_start_year": 2021,
        "contract_end_year": 2028,
        "status": "ACTIVE",
    },
    {
        "broadcaster_id": "mls_official",
        "name": "Major League Soccer",
        "channel_handle": "@MLS",
        "channel_id": "UCSZbXT5TLLW_i-5W8FZpFsg",
        "competitions": ["MLS"],
        "allowed_countries": [],  # Empty = 100% Global
        "contract_start_year": 2023,
        "contract_end_year": 2033,
        "status": "ACTIVE",
    },

    # ---------------------------------------------------------
    # Indonesia & Southeast Asia
    # ---------------------------------------------------------
    {
        "broadcaster_id": "vidio_id",
        "name": "Vidio",
        "channel_handle": "@vidio_sports",
        "channel_id": "UC_Vidio_Sports_Placeholder",
        "competitions": ["PL", "CL", "EL", "ECL"],
        "allowed_countries": ["ID"],
        "contract_start_year": 2022,
        "contract_end_year": 2028,
        "status": "MONITORING",
    },
    {
        "broadcaster_id": "bein_sports_asia",
        "name": "beIN SPORTS Asia",
        "channel_handle": "@beINSPORTSAsia",
        "channel_id": "UC_beIN_Sports_Asia_Placeholder",
        "competitions": ["CL", "EL", "PD", "SA", "FL1"],
        "allowed_countries": ["ID", "MY", "SG", "TH", "PH", "HK"],
        "contract_start_year": 2024,
        "contract_end_year": 2027,
        "status": "MONITORING",
    },

    # ---------------------------------------------------------
    # India & South Asia
    # ---------------------------------------------------------
    {
        "broadcaster_id": "sony_sports_in",
        "name": "Sony Sports Network",
        "channel_handle": "@SonySportsNetwork",
        "channel_id": "UC_WKb6N9iTGc77hxwXLDrbA",
        "competitions": ["CL", "EL", "BL1", "FAC", "SPL"],
        "allowed_countries": ["IN", "PK", "BD", "LK", "NP", "MV", "BT"],
        "contract_start_year": 2024,
        "contract_end_year": 2027,
        "status": "ACTIVE",
    },

    # ---------------------------------------------------------
    # Brazil & Latin America
    # ---------------------------------------------------------
    {
        "broadcaster_id": "caze_tv_br",
        "name": "CazéTV",
        "channel_handle": "@CazeTV",
        "channel_id": "UCZiYbVptd3PVPf4f6eR6UaQ",
        "competitions": ["FL1", "BL1", "EL"],
        "allowed_countries": ["BR"],
        "contract_start_year": 2023,
        "contract_end_year": 2027,
        "status": "ACTIVE",
    },
    {
        "broadcaster_id": "tnt_sports_br",
        "name": "TNT Sports Brasil",
        "channel_handle": "@TNTSportsBR",
        "channel_id": "UCs-6sCz2LJm1PrWQN4ErsPw",
        "competitions": ["CL", "PL"],
        "allowed_countries": ["BR"],
        "contract_start_year": 2024,
        "contract_end_year": 2027,
        "status": "ACTIVE",
    },

    # ---------------------------------------------------------
    # Australia & Oceania
    # ---------------------------------------------------------
    {
        "broadcaster_id": "optus_sport_au",
        "name": "Optus Sport",
        "channel_handle": "@OptusSport",
        "channel_id": "UC_Optus_Sport_Placeholder",
        "competitions": ["PL", "PD", "FAC"],
        "allowed_countries": ["AU"],
        "contract_start_year": 2022,
        "contract_end_year": 2028,
        "status": "MONITORING",
    },
]


def get_broadcasters_for_competition(comp_id: str, country_code: str | None = None) -> list[dict[str, Any]]:
    """Retrieve active registered broadcasters for a competition, optionally filtered by user country."""
    results = []
    comp_clean = comp_id.upper().strip()
    for b in GLOBAL_BROADCASTER_REGISTRY:
        if b.get("status") != "ACTIVE":
            continue
        if comp_clean in b.get("competitions", []):
            if country_code:
                allowed = b.get("allowed_countries", [])
                # If allowed is empty, it's global; otherwise check if country matches
                if not allowed or country_code.upper() in allowed:
                    results.append(b)
            else:
                results.append(b)
    return results
