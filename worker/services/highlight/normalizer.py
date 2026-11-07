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
    t = re.sub(r"(\w)['\u2019](\w)", r"\1\2", t)
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
    """Cleans parentheticals, prefixes, years, and common suffixes to generate concise search keywords."""
    cleaned = name.lower().strip()
    cleaned = re.sub(r'\s*\([^)]*\)', '', cleaned)
    # Check if DB has aliases for this club
    aliases = channel_repository.get_club_aliases(cleaned)
    if aliases and aliases[0] != cleaned:
        return aliases[0]

    cleaned = re.sub(r'^\d+\.\s*', '', cleaned)
    cleaned = re.sub(r'\bde madrid\b', '', cleaned, flags=re.IGNORECASE)
    cleaned = re.sub(r'\b(18\d{2}|19\d{2}|20\d{2}|\d{2})\b', '', cleaned)
    cleaned = re.sub(
        r'\b(Racing Club de|Racing de|SS|AS|US|AC|BC|CFC|BSC|Olympique|TSG|VfB|VfL|07|04|FC|FK|SK|BK|IF|FF|IK|SC|KAA|KRC|GNK|CF|AFC|SSC|SV|OGC|RCD|Club|Stade|ES|AJ|SCO|ESTAC|Calcio|Fútbol|Futbol|Balompié|Balompie|RC|de|la|le)\b',
        '',
        cleaned,
        flags=re.IGNORECASE,
    )
    cleaned = re.sub(r'\b(juniors?|plate|old boys|central)\b', '', cleaned, flags=re.IGNORECASE)
    return re.sub(r'\s+', ' ', cleaned).strip()


def get_team_aliases(team_name: str) -> list[str]:
    """Retrieves all recognized search/matching aliases from database with resilient fallback."""
    norm = normalize_for_alias_matching(team_name).strip()
    raw = team_name.lower().strip()
    db_aliases = channel_repository.get_club_aliases(team_name)
    cleaned = clean_team_name_for_search(team_name).lower()

    aliases = set(db_aliases + [cleaned, raw, norm])

    # Strip parentheticals e.g. "Belgrano (Córdoba)" -> "Belgrano"
    no_parens = re.sub(r'\s*\([^)]*\)', '', raw).strip()
    norm_no_parens = normalize_for_alias_matching(no_parens).strip()
    if norm_no_parens:
        aliases.add(norm_no_parens)
        aliases.add(no_parens)

    # Strip club prefixes: "club atletico", "ca", "cd", etc.
    no_prefix = re.sub(r'^(club atletico|clube atletico|club social y deportivo|asociacion atletica|club|ca|cd|cf|fc|ac)\s+', '', norm_no_parens)
    if no_prefix and len(no_prefix) >= 3:
        aliases.add(no_prefix)

    # Strip club suffixes: "juniors", "plate", "old boys", "central", "hotspur", "albion", "wanderers", "rovers", "town", "county"
    no_suffix = re.sub(r'\s+(juniors?|plate|old boys|central|hotspur|albion|wanderers|rovers|town|county)$', '', no_prefix)
    if no_suffix and len(no_suffix) >= 3:
        aliases.add(no_suffix)

    # Specific club nickname augmentations
    if "bayern" in norm:
        aliases.update(["bayern", "fc bayern", "bayern munchen", "bayern munich"])
    if "atletico madrid" in norm or norm in ("atletico", "atleti", "club atletico de madrid"):
        aliases.update(["atletico", "atleti", "atletico madrid", "atletico de madrid"])
    if "dortmund" in norm:
        aliases.update(["dortmund", "bvb", "borussia dortmund"])
    if "leverkusen" in norm:
        aliases.update(["leverkusen", "bayer leverkusen"])
    if "monchengladbach" in norm or "m gladbach" in norm:
        aliases.update(["gladbach", "m gladbach", "borussia m gladbach"])
    if "internazionale" in norm or "inter milan" in norm:
        aliases.update(["inter", "inter milan", "internazionale"])
    if "paris saint germain" in norm:
        aliases.update(["psg", "paris", "paris sg"])
    if "boca" in norm:
        aliases.update(["boca", "boca juniors", "xeneize"])
    if "river" in norm:
        aliases.update(["river", "river plate", "millonario"])
    if "argentinos" in norm:
        aliases.update(["argentinos", "argentinos juniors", "bicho"])
    if "newell" in norm:
        aliases.update(["newell's", "newells", "newell's old boys", "nob"])
    if "tucuman" in norm:
        aliases.update(["tucuman", "atletico tucuman", "atlético tucumán"])
    if "racing" in norm and "santander" not in norm and "ferrol" not in norm and "lens" not in norm:
        aliases.update(["racing", "racing club", "la academia"])
    if "san lorenzo" in norm:
        aliases.update(["san lorenzo", "ciclon"])
    if "velez" in norm:
        aliases.update(["velez", "velez sarsfield", "fortin"])
    if "independiente" in norm and "rivadavia" not in norm:
        aliases.update(["independiente", "rojo"])
    if "gimnasia" in norm:
        aliases.update(["gimnasia", "gimnasia la plata", "lobo"])
    if "flamengo" in norm:
        aliases.update(["flamengo", "mengao", "mengo"])
    if "palmeiras" in norm:
        aliases.update(["palmeiras", "verdao"])
    if "corinthians" in norm:
        aliases.update(["corinthians", "timao"])
    if "sao paulo" in norm:
        aliases.update(["sao paulo", "spfc", "tricolor"])
    if "gremio" in norm:
        aliases.update(["gremio", "imortal"])
    if "al hilal" in norm:
        aliases.update(["al hilal", "al-hilal", "hilal"])
    if "al nassr" in norm:
        aliases.update(["al nassr", "al-nassr", "nassr"])
    if "al ittihad" in norm:
        aliases.update(["al ittihad", "al-ittihad", "ittihad"])
    if "al ahli" in norm:
        aliases.update(["al ahli", "al-ahli", "ahli"])
    if "celta" in norm:
        aliases.update(["celta", "celta vigo", "celta de vigo", "rc celta"])
    if "union st" in norm or "union saint" in norm or "gilloise" in norm:
        aliases.update(["union saint-gilloise", "union saint gilloise", "union st-gilloise", "union st gilloise", "saint-gilloise", "saint gilloise", "st gilloise", "usg"])
    if "beer" in norm or "be'er" in norm or "hbsfc" in norm:
        aliases.update(["hapoel beer sheva", "hapoel be'er sheva", "beer sheva", "be'er sheva", "beer-sheva", "be'er", "beer", "hbsfc"])
    if "salzburg" in norm:
        aliases.update(["salzburg", "rb salzburg", "fc salzburg", "red bull salzburg", "fc red bull salzburg", "rbs"])
    if "leipzig" in norm:
        aliases.update(["leipzig", "rb leipzig", "rasenballsport leipzig", "rbl"])
    if "anderlecht" in norm:
        aliases.update(["anderlecht", "rsc anderlecht", "rsca"])
    if "zalgiris" in norm or "žalgiris" in norm:
        aliases.update(["kauno zalgiris", "kauno žalgiris", "fk kauno zalgiris", "fk kauno žalgiris", "zalgiris", "žalgiris"])
    if "ajax" in norm:
        aliases.update(["ajax", "afc ajax", "ajax amsterdam"])
    if "red star" in norm or "crvena zvezda" in norm:
        aliases.update(["red star", "crvena zvezda", "red star belgrade"])
    if "borac" in norm:
        aliases.update(["borac", "borac banja luka", "fk borac"])
    if "midlothian" in norm or "hearts" in norm:
        aliases.update(["hearts", "heart of midlothian"])
    if "kups" in norm or "ku ps" in norm:
        aliases.update(["kups", "kups kuopio"])
    if "brann" in norm:
        aliases.update(["brann", "sk brann"])
    if "craiova" in norm:
        aliases.update(["craiova", "universitatea craiova", "csu craiova"])
    if "sint-truidense" in norm or "sint truidense" in norm or "truiden" in norm:
        aliases.update(["sint-truiden", "sint truiden", "stvv", "sint-truidense", "sint truidense"])
    if "kobenhavn" in norm or "københavn" in norm or "copenhagen" in norm:
        aliases.update(["kobenhavn", "copenhagen", "fc kobenhavn", "fck"])

    # Bidirectional St. <-> Saint expansion
    extra_st = set()
    for a in list(aliases):
        if re.search(r'\bst\b', a):
            extra_st.add(re.sub(r'\bst\b', 'saint', a))
        elif re.search(r'\bsaint\b', a):
            extra_st.add(re.sub(r'\bsaint\b', 'st', a))
    aliases.update(extra_st)

    # Bidirectional RB <-> Red Bull expansion
    extra_rb = set()
    for a in list(aliases):
        if re.search(r'\brb\b', a):
            extra_rb.add(re.sub(r'\brb\b', 'red bull', a))
        elif re.search(r'\bred bull\b', a):
            extra_rb.add(re.sub(r'\bred bull\b', 'rb', a))
    aliases.update(extra_rb)

    return [a for a in aliases if len(a) >= 3]


def get_club_handle(team_name: str) -> str | None:
    """Retrieves verified YouTube handle (@handle) from database."""
    cleaned = team_name.lower().strip()
    if "espanyol" in cleaned and "barcelona" in cleaned:
        return None
    return channel_repository.get_club_handle(team_name)
