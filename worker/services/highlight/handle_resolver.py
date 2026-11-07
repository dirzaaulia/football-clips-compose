"""
Channel handle resolution for FootballClips highlight pipeline.
Resolves Tier 1 (official clubs and league), Tier 2 (primary broadcasters),
and Tier 3 (regional broadcasters) channels for a given match.
"""

from worker.config.broadcaster_registry import GLOBAL_BROADCASTER_REGISTRY
from worker.repositories.channel_repository import channel_repository
from worker.services.highlight.normalizer import get_club_handle


def resolve_target_handles(
    home: str,
    away: str,
    comp_id: str,
    comp_name: str,
) -> tuple[list[str], list[str], list[str]]:
    """Resolve target YouTube handles ordered by authoritative priority."""
    comp_name_upper = comp_name.upper()
    comp_id_upper = comp_id.upper()

    tier1: list[str] = []
    h_h = get_club_handle(home)
    a_h = get_club_handle(away)
    if h_h:
        tier1.append(h_h)
    if a_h and a_h not in tier1:
        tier1.append(a_h)

    league_h, _ = channel_repository.get_competition_channel(comp_id_upper)
    if not league_h:
        league_h, _ = channel_repository.get_competition_channel(comp_name_upper)
    if league_h and league_h not in tier1:
        tier1.append(league_h)

    tier2: list[str] = []
    backups = channel_repository.get_competition_backups(comp_id_upper) or channel_repository.get_competition_backups(comp_name_upper)
    for bh in backups:
        if bh not in tier1 and bh not in tier2:
            tier2.append(bh)

    for b in GLOBAL_BROADCASTER_REGISTRY:
        comps = b.get("competitions", [])
        if comp_id_upper in comps or comp_name_upper in comps:
            h = b.get("channel_handle")
            if h and h not in tier1 and h not in tier2:
                if len(b.get("allowed_countries", [])) > 1 or not b.get("allowed_countries"):
                    tier2.append(h)

    tier3: list[str] = []
    for b in GLOBAL_BROADCASTER_REGISTRY:
        comps = b.get("competitions", [])
        if comp_id_upper in comps or comp_name_upper in comps:
            h = b.get("channel_handle")
            if h and h not in tier1 and h not in tier2 and h not in tier3:
                tier3.append(h)

    return tier1, tier2, tier3
