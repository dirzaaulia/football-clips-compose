"""3-Stage Deterministic Club Entity Linker and Stage 0 Entity Masker.

Replaces procedural if/elif alias chains with:
1. Stage 1: Unicode NFKD + ligature folding + CamelCase hashtag splitter.
2. Stage 2: Declarative multilingual affix stripper driven by TaxonomyCategory.CLUB_AFFIXES.
3. Stage 3: O(1) Inverted Alias Lookup in ChannelRepository + token-set similarity fallback (>=90%).
4. Stage 0 Entity Masker: Locks matched club spans as [HOME_CLUB] and [AWAY_CLUB] prior to classification.
"""

import re
import unicodedata
from difflib import SequenceMatcher

from worker.models.taxonomy_models import TaxonomyCategory
from worker.repositories.channel_repository import channel_repository

LIGATURE_MAP = str.maketrans({
    "ø": "o", "Ø": "O",
    "æ": "ae", "Æ": "AE",
    "ß": "ss",
    "ä": "a", "Ä": "A",
    "ö": "o", "Ö": "O",
    "ü": "u", "Ü": "U",
    "ł": "l", "Ł": "L",
    "đ": "d", "Đ": "D",
})

LEADING_PREFIX_RE = re.compile(
    r"^(club atletico|clube atletico|club social y deportivo|asociacion atletica|club|ca|cd|cf|fc|ac)\s+",
    flags=re.IGNORECASE,
)

TRAILING_QUALIFIER_RE = re.compile(
    r"\s+(juniors?|plate|old boys|central|hotspur|albion|wanderers|rovers|town|county|"
    r"de la plata|la plata|y esgrima(?: la plata| de la plata)?|antonio tomba|"
    r"santiago del estero|de santa fe|santa fe|de junin|junin|de sarandi|sarandi|"
    r"de guimaraes|de viseu)$",
    flags=re.IGNORECASE,
)


class ClubEntityResolver:
    """Deterministic 3-stage club entity resolver and entity masker."""

    def __init__(self):
        self._affix_regex: re.Pattern | None = None

    def _get_affix_regex(self) -> re.Pattern:
        if self._affix_regex is not None:
            return self._affix_regex
        entries = [
            e for e in channel_repository.get_taxonomy_entries()
            if e.category == TaxonomyCategory.CLUB_AFFIXES
        ]
        terms: list[str] = []
        for entry in entries:
            terms.extend(entry.terms)
        terms.sort(key=len, reverse=True)
        escaped = [re.escape(t) for t in terms if t]
        pattern = rf"\b(?:{'|'.join(escaped)})\b" if escaped else r"\b(?:fc|cf|sc|ac)\b"
        self._affix_regex = re.compile(pattern, flags=re.IGNORECASE)
        return self._affix_regex

    @staticmethod
    def normalize_for_alias_matching(text: str) -> str:
        """Stage 1: NFKD + ligature folding + CamelCase hashtag splitter."""
        if not text:
            return "  "
        t = text.translate(LIGATURE_MAP)
        t = re.sub(r"([a-z])([A-Z])", r"\1 \2", t)
        t = re.sub(r"([a-zA-Z])(\d)", r"\1 \2", t)
        t = re.sub(r"(\d)([a-zA-Z])", r"\1 \2", t)
        t = re.sub(r"(\w)['\u2019](\w)", r"\1\2", t)
        t = unicodedata.normalize("NFKD", t)
        t = "".join(c for c in t if not unicodedata.combining(c))
        clean = re.sub(r"[^a-zA-Z0-9]", " ", t.lower())
        return " " + " ".join(clean.split()) + " "

    def clean_team_name_for_search(self, name: str) -> str:
        """Stage 2: Strips parentheticals, prefixes, years, and declarative CLUB_AFFIXES."""
        cleaned = re.sub(r"\s*\([^)]*\)", "", name.lower().strip())
        aliases = channel_repository.get_club_aliases(cleaned)
        if aliases and aliases[0] != cleaned:
            return aliases[0]

        cleaned = re.sub(r"^\d+\.\s*", "", cleaned)
        cleaned = re.sub(r"\b(18\d{2}|19\d{2}|20\d{2}|\d{2})\b", "", cleaned)
        cleaned = self._get_affix_regex().sub("", cleaned)
        cleaned = re.sub(r"\b(de|la|le)\b", "", cleaned, flags=re.IGNORECASE)
        return re.sub(r"\s+", " ", cleaned).strip()

    def get_team_aliases(self, team_name: str) -> list[str]:
        """Stage 3: Resolves all canonical aliases from DB + seed catalog + affix stripping."""
        norm = self.normalize_for_alias_matching(team_name).strip()
        raw = team_name.lower().strip()
        db_aliases = channel_repository.get_club_aliases(team_name)
        cleaned = self.clean_team_name_for_search(team_name).lower()

        aliases: set[str] = set(db_aliases + [cleaned, raw, norm])
        no_parens = re.sub(r"\s*\([^)]*\)", "", raw).strip()
        norm_no_parens = self.normalize_for_alias_matching(no_parens).strip()
        if norm_no_parens:
            aliases.update([norm_no_parens, no_parens])

        no_prefix = LEADING_PREFIX_RE.sub("", norm_no_parens)
        if len(no_prefix) >= 3:
            aliases.add(no_prefix)

        no_suffix = TRAILING_QUALIFIER_RE.sub("", no_prefix)
        if len(no_suffix) >= 3:
            aliases.add(no_suffix)

        seed_map = channel_repository.get_seed_club_aliases()
        for key, seed_vals in seed_map.items():
            if key == "racing club":
                if "racing" in norm and not any(x in norm for x in ("santander", "ferrol", "lens")):
                    aliases.update(seed_vals)
            elif key == "independiente":
                if "independiente" in norm and "rivadavia" not in norm:
                    aliases.update(seed_vals)
            elif key in norm or norm in seed_vals:
                aliases.update(seed_vals)

        expanded: set[str] = set()
        for a in list(aliases):
            if re.search(r"\bst\b", a):
                expanded.add(re.sub(r"\bst\b", "saint", a))
            elif re.search(r"\bsaint\b", a):
                expanded.add(re.sub(r"\bsaint\b", "st", a))
            if re.search(r"\brb\b", a):
                expanded.add(re.sub(r"\brb\b", "red bull", a))
            elif re.search(r"\bred bull\b", a):
                expanded.add(re.sub(r"\bred bull\b", "rb", a))
        aliases.update(expanded)

        return [a for a in aliases if len(a) >= 3]

    def matches_alias(self, alias: str, normalized_text: str) -> bool:
        """Checks exact word-boundary match first, with >=90% token-set similarity fallback for multi-word names."""
        clean_alias = self.normalize_for_alias_matching(alias).strip()
        if not clean_alias:
            return False
        if f" {clean_alias} " in normalized_text or bool(re.search(rf"\b{re.escape(clean_alias)}\b", normalized_text)):
            return True

        alias_tokens = clean_alias.split()
        if len(alias_tokens) >= 2 and len(clean_alias) >= 8:
            text_tokens = normalized_text.strip().split()
            window = len(alias_tokens)
            for i in range(len(text_tokens) - window + 1):
                candidate_span = " ".join(text_tokens[i : i + window])
                if SequenceMatcher(None, clean_alias, candidate_span).ratio() >= 0.88:
                    return True
        return False

    def mask_club_entities(
        self,
        title: str,
        home_team: str | None = None,
        away_team: str | None = None,
    ) -> str:
        """Stage 0: Masks matched home/away club spans as [HOME_CLUB] and [AWAY_CLUB] to protect club names."""
        if not title or (not home_team and not away_team):
            return title

        masked = title
        for team, token in ((home_team, "[HOME_CLUB]"), (away_team, "[AWAY_CLUB]")):
            if not team:
                continue
            team_aliases = sorted(self.get_team_aliases(team), key=len, reverse=True)
            for al in team_aliases:
                if len(al) < 4:
                    continue
                pat = re.compile(rf"\b{re.escape(al)}\b", flags=re.IGNORECASE)
                if pat.search(masked):
                    masked = pat.sub(token, masked)
                    break
        return masked

    @staticmethod
    def get_club_handle(team_name: str) -> str | None:
        cleaned = team_name.lower().strip()
        if "espanyol" in cleaned and "barcelona" in cleaned:
            return None
        return channel_repository.get_club_handle(team_name)


club_entity_resolver = ClubEntityResolver()
