"""Highlight title classification and regional watchability filters."""

import json
import re
from typing import Any

from worker.repositories.channel_repository import channel_repository
from worker.services.highlight.title_classifier import title_classifier

KEY_FOOTBALL_MARKETS: set[str] = {
    "ID", "MY", "TH", "VN", "SG", "PH",
    "GB", "ES", "DE", "IT", "FR", "PT", "NL", "TR",
    "BR", "AR", "CO", "CL", "UY",
    "NG", "EG", "ZA", "GH", "KE", "MA", "SN", "DZ",
    "US", "MX", "SA", "AE",
}

TOP_FOOTBALL_FAN_MARKETS: list[str] = sorted(list(KEY_FOOTBALL_MARKETS))


def _parse_region_info(val: Any) -> dict[str, Any] | None:
    if isinstance(val, dict):
        return val
    if isinstance(val, str) and val.strip():
        try:
            parsed = json.loads(val)
            if isinstance(parsed, dict):
                return parsed
        except Exception:
            pass
    return None


def check_forbidden_title(
    title: str,
    home_team: str | None = None,
    away_team: str | None = None,
) -> tuple[bool, str | None]:
    return title_classifier.check_forbidden_title(title, home_team, away_team)


def has_highlight_keyword(title: str) -> bool:
    return title_classifier.has_highlight_keyword(title)


def has_scoreline(title: str) -> bool:
    return bool(re.search(r"\b\d+\s*[-–]\s*\d+\b", title))


def is_target_market_catered(region_info: Any) -> bool:
    r_dict = _parse_region_info(region_info)
    if not r_dict:
        return True
    blocked = set(r_dict.get("blocked") or [])
    if blocked.intersection(KEY_FOOTBALL_MARKETS):
        return False
    allowed = set(r_dict.get("allowed") or [])
    if allowed:
        return bool(allowed.intersection(KEY_FOOTBALL_MARKETS))
    return True


def is_target_country_catered(
    region_info: Any,
    country_or_countries: str | list[str] | set[str] | None = None,
) -> bool:
    r_dict = _parse_region_info(region_info)
    if not r_dict:
        return True

    if country_or_countries is None:
        targets = TOP_FOOTBALL_FAN_MARKETS
    elif isinstance(country_or_countries, (list, set)):
        targets = [c.upper() for c in country_or_countries]
    else:
        targets = [country_or_countries.upper()]

    blocked = set(r_dict.get("blocked") or [])
    allowed = set(r_dict.get("allowed") or [])

    for c in targets:
        if c in blocked:
            continue
        if allowed and c not in allowed:
            continue
        return True
    return False


def get_catered_target_countries(
    region_info: Any,
    target_countries: list[str] | None = None,
) -> list[str]:
    if target_countries is None:
        target_countries = TOP_FOOTBALL_FAN_MARKETS
    r_dict = _parse_region_info(region_info)
    if not r_dict:
        return target_countries

    blocked = set(r_dict.get("blocked") or [])
    allowed = set(r_dict.get("allowed") or [])
    return [
        c.upper()
        for c in target_countries
        if c.upper() not in blocked and (not allowed or c.upper() in allowed)
    ]


def get_worldwide_watchability_score(region_info: Any) -> float:
    r_dict = _parse_region_info(region_info)
    if not r_dict:
        return 100.0
    allowed = r_dict.get("allowed") or []
    if allowed:
        return round((len(allowed) / 249.0) * 100.0, 1)
    blocked = r_dict.get("blocked") or []
    if blocked:
        return round(((249 - len(blocked)) / 249.0) * 100.0, 1)
    return 100.0


def get_geoblock_status(region_info: Any) -> tuple[int, str]:
    r_dict = _parse_region_info(region_info)
    if not r_dict:
        return 0, "100% Global"
    if "allowed" in r_dict:
        allowed = r_dict.get("allowed") or []
        if len(allowed) == 0:
            return 9999, "Blocked Globally (0 countries)"
        return 1000 - len(allowed), f"Whitelist ({len(allowed)} countries)"
    if "blocked" in r_dict:
        blocked = r_dict.get("blocked") or []
        return len(blocked), f"Blocked ({len(blocked)} countries)"
    return 0, "100% Global"


def classify_candidate_tier(channel_handle: str | None, region_info: Any) -> tuple[int, str]:
    handle = (channel_handle or "").lower()
    r_dict = _parse_region_info(region_info)
    club_handles = {"@realmadrid", "@fcbarcelona", "@mancity", "@arsenal", "@liverpoolfc", "@chelseafc", "@manutd"}
    league_handles = {"@laliga", "@bundesliga", "@seriea", "@ligue1", "@thefacup", "@carabaocup"}

    if handle in club_handles:
        return 1, "Tier 1: Club Official (Worldwide)"
    if handle in league_handles or handle == "@mls":
        if not r_dict or (not r_dict.get("blocked") and not r_dict.get("allowed")):
            return 1, "Tier 1: League Official (Worldwide)"

    for b in channel_repository.get_broadcasters():
        if b.channel_handle.lower() == handle:
            if len(b.allowed_countries) == 0:
                return 1, f"Tier 1: {b.name} (Worldwide)"
            if len(b.allowed_countries) > 3:
                return 2, f"Tier 2: {b.name} ({len(b.allowed_countries)} countries)"
            return 3, f"Tier 3: {b.name} ({', '.join(b.allowed_countries)})"

    if r_dict:
        allowed = r_dict.get("allowed") or []
        blocked = r_dict.get("blocked") or []
        if allowed:
            if len(allowed) > 10:
                return 2, f"Tier 2: Regional ({len(allowed)} countries)"
            return 3, f"Tier 3: Domestic ({len(allowed)} countries)"
        if blocked:
            if len(blocked) > 50:
                return 3, f"Tier 3: Domestic ({249 - len(blocked)} countries)"
            return 2, f"Tier 2: Broad Regional ({249 - len(blocked)} countries)"

    return 1, "Tier 1: Global Unrestricted"
