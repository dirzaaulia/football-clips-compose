"""Clean facade delegating club entity resolution and alias matching to ClubEntityResolver."""

from worker.services.highlight.club_entity_resolver import club_entity_resolver


def normalize_for_alias_matching(text: str) -> str:
    return club_entity_resolver.normalize_for_alias_matching(text)


def matches_alias(alias: str, normalized_text: str) -> bool:
    return club_entity_resolver.matches_alias(alias, normalized_text)


def clean_team_name_for_search(name: str) -> str:
    return club_entity_resolver.clean_team_name_for_search(name)


def get_team_aliases(team_name: str) -> list[str]:
    return club_entity_resolver.get_team_aliases(team_name)


def get_club_handle(team_name: str) -> str | None:
    return club_entity_resolver.get_club_handle(team_name)


def mask_club_entities(title: str, home_team: str | None = None, away_team: str | None = None) -> str:
    return club_entity_resolver.mask_club_entities(title, home_team, away_team)


__all__ = [
    "clean_team_name_for_search",
    "get_club_handle",
    "get_team_aliases",
    "mask_club_entities",
    "matches_alias",
    "normalize_for_alias_matching",
]
