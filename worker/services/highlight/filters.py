import json
import re
from typing import Any
from worker.config.constants import FORBIDDEN_TITLE_PATTERNS, HIGHLIGHT_REQUIRED_KEYWORDS

KEY_FOOTBALL_MARKETS: set[str] = {
    # ASEAN
    "ID", "MY", "TH", "VN", "SG", "PH",
    # UK & Europe
    "GB", "ES", "DE", "IT", "FR", "PT", "NL", "TR",
    # South America
    "BR", "AR", "CO", "CL", "UY",
    # Africa
    "NG", "EG", "ZA", "GH", "KE", "MA", "SN", "DZ",
    # Americas & Middle East
    "US", "MX", "SA", "AE",
}


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


def check_forbidden_title(title: str) -> tuple[bool, str | None]:
    t = title.lower()
    for pattern, reason in FORBIDDEN_TITLE_PATTERNS:
        if re.search(pattern, t, flags=re.IGNORECASE):
            return True, reason
    return False, None


def has_highlight_keyword(title: str) -> bool:
    t = title.lower()
    for kw in HIGHLIGHT_REQUIRED_KEYWORDS:
        if re.search(kw, t, flags=re.IGNORECASE):
            return True
    return False


def has_scoreline(title: str) -> bool:
    return bool(re.search(r'\b\d+\s*[-–]\s*\d+\b', title))


def is_target_market_catered(region_info: Any) -> bool:
    r_dict = _parse_region_info(region_info)
    if not r_dict:
        return True
    blocked = set(r_dict.get("blocked") or [])
    if blocked.intersection(KEY_FOOTBALL_MARKETS):
        return False
    allowed = set(r_dict.get("allowed") or [])
    if allowed:
        primary = {"ID", "GB", "US"}
        return primary.issubset(allowed)
    return True


def is_target_country_catered(region_info: Any, country_or_countries: str | list[str] = "ID") -> bool:
    """
    Checks if a video is playable in a target country or at least one target country in a list
    (e.g. ['ID', 'GB', 'US']). Returns True if playable in ANY target country.
    """
    r_dict = _parse_region_info(region_info)
    if not r_dict:
        return True

    targets = (
        [country_or_countries.upper()]
        if isinstance(country_or_countries, str)
        else [c.upper() for c in country_or_countries]
    )
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
    """Returns the exact list of target countries (e.g. ['ID', 'GB', 'US']) where this video is watchable."""
    if target_countries is None:
        target_countries = ["ID", "GB", "US"]
    r_dict = _parse_region_info(region_info)
    if not r_dict:
        return target_countries

    blocked = set(r_dict.get("blocked") or [])
    allowed = set(r_dict.get("allowed") or [])
    catered = []
    for c in target_countries:
        c_upper = c.upper()
        if c_upper in blocked:
            continue
        if allowed and c_upper not in allowed:
            continue
        catered.append(c_upper)
    return catered


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

    try:
        from worker.config.broadcaster_registry import GLOBAL_BROADCASTER_REGISTRY
        for b in GLOBAL_BROADCASTER_REGISTRY:
            if b.get("channel_handle", "").lower() == handle:
                allowed = b.get("allowed_countries", [])
                if len(allowed) == 0:
                    return 1, f"Tier 1: {b.get('name', 'Global')} (Worldwide)"
                elif len(allowed) > 3:
                    return 2, f"Tier 2: {b.get('name', 'Regional')} ({len(allowed)} countries)"
                else:
                    return 3, f"Tier 3: {b.get('name', 'Domestic')} ({', '.join(allowed)})"
    except Exception:
        pass

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
