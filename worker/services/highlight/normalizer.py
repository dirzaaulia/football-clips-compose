"""Normalization and alias matching utilities for clubs and videos."""

import re
import unicodedata

from worker.repositories.channel_repository import channel_repository


def normalize_for_alias_matching(text: str) -> str:
    """Normalizes video titles and club aliases for bulletproof matching.

    Handles Scandinavian / Germanic characters (ø -> o, æ -> ae, ß -> ss),
    splits CamelCase / PascalCase joined hashtags (#NapoliBodøGlimt -> Napoli Bodo Glimt),
    separates letters and numbers, decomposes accents, and pads with spaces.
    """
    if not text:
        return "  "
    trans = str.maketrans({
        'ø': 'o', 'Ø': 'O',
        'æ': 'ae', 'Æ': 'AE',
        'ß': 'ss',
        'ä': 'a', 'Ä': 'A',
        'ö': 'o', 'Ö': 'O',
        'ü': 'u', 'Ü': 'U',
    })
    t = text.translate(trans)
    t = re.sub(r'([a-z])([A-Z])', r'\1 \2', t)
    t = re.sub(r'([a-zA-Z])(\d)', r'\1 \2', t)
    t = re.sub(r'(\d)([a-zA-Z])', r'\1 \2', t)
    t = unicodedata.normalize('NFKD', t)
    t = ''.join(c for c in t if not unicodedata.combining(c))
    clean = re.sub(r'[^a-zA-Z0-9]', ' ', t.lower())
    return ' ' + ' '.join(clean.split()) + ' '


def matches_alias(alias: str, normalized_text: str) -> bool:
    """Checks if an alias exists in the space-padded normalized text with exact word boundaries."""
    clean_alias = normalize_for_alias_matching(alias).strip()
    if not clean_alias:
        return False
    return f" {clean_alias} " in normalized_text or bool(re.search(rf"\b{re.escape(clean_alias)}\b", normalized_text))


def clean_team_name_for_search(name: str) -> str:
    """Cleans prefixes, years, and common club suffixes to generate concise search keywords."""
    cleaned = name.lower().strip()
    # Check if DB has aliases for this club
    aliases = channel_repository.get_club_aliases(cleaned)
    if aliases and aliases[0] != cleaned:
        return aliases[0]

    cleaned = re.sub(r'^\d+\.\s*', '', cleaned)
    cleaned = re.sub(r'\bde madrid\b', '', cleaned, flags=re.IGNORECASE)
    cleaned = re.sub(r'\b(18\d{2}|19\d{2}|20\d{2}|\d{2})\b', '', cleaned)
    cleaned = re.sub(
        r'\b(Racing Club de|Racing de|SS|AS|US|AC|BC|CFC|BSC|Olympique|TSG|VfB|VfL|07|04|FC|CF|AFC|SSC|SV|OGC|RCD|Club|Stade|ES|AJ|SCO|ESTAC|Calcio|Fútbol|Futbol|Balompié|Balompie|RC|de|la|le)\b',
        '',
        cleaned,
        flags=re.IGNORECASE,
    )
    return re.sub(r'\s+', ' ', cleaned).strip()


def get_team_aliases(team_name: str) -> list[str]:
    """Retrieves all recognized search/matching aliases from database."""
    cleaned = clean_team_name_for_search(team_name).lower()
    db_aliases = channel_repository.get_club_aliases(team_name)
    aliases = set(db_aliases + [cleaned, team_name.lower().strip()])
    return [a for a in aliases if len(a) >= 3]


def get_club_handle(team_name: str) -> str | None:
    """Retrieves verified YouTube handle (@handle) from database."""
    cleaned = team_name.lower().strip()
    if "espanyol" in cleaned and "barcelona" in cleaned:
        return None
    return channel_repository.get_club_handle(team_name)
