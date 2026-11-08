"""Entity-Masked Multilingual Title Classifier with Structural Override Rules.

Replaces the raw regex wall in constants.py with:
1. Stage 0 Entity-Masking (masks [HOME_CLUB] and [AWAY_CLUB] so club names like
   'Newell's Old Boys', 'BSC Young Boys', 'Talleres', or 'Estudiantes' never false-trigger).
2. Token-Set & Phrase Boundary Classification backed by PostgreSQL / seed TaxonomyEntry models.
3. Structural Override Rules (e.g., FULL_REPLAY phrases like 'full match' are allowed when
   accompanied by a positive highlight keyword like 'highlights' or 'resumen').
"""

import re

from worker.models.taxonomy_models import (
    TaxonomyCategory,
    TaxonomyEntry,
    TitleClassificationResult,
)
from worker.repositories.channel_repository import channel_repository
from worker.services.highlight.club_entity_resolver import club_entity_resolver

STRUCTURAL_OVERRIDE_KEYWORDS = (
    "highlight", "highlights", "resumen", "resumo", "résumé", "resume",
    "sintesi", "zusammenfassung", "samenvatting", "recap", "summary",
    "extended", "goals", "goles", "gols", "buts", "tore", "özet", "ملخص", "cuplikan",
)


class MultilingualTitleClassifier:
    """Classifies YouTube titles across 11+ languages using TaxonomyEntry models and entity masking."""

    def __init__(self):
        self._compiled_positive: list[tuple[str, re.Pattern]] = []
        self._compiled_negative: list[tuple[TaxonomyEntry, list[tuple[str, re.Pattern]], list[re.Pattern]]] = []
        self._loaded_count: int = 0

    def _ensure_compiled(self):
        entries = channel_repository.get_taxonomy_entries()
        if self._compiled_negative and len(entries) == self._loaded_count:
            return

        pos_list: list[tuple[str, re.Pattern]] = []
        neg_list: list[tuple[TaxonomyEntry, list[tuple[str, re.Pattern]], list[re.Pattern]]] = []

        for entry in entries:
            if entry.category == TaxonomyCategory.CLUB_AFFIXES:
                continue
            if entry.category == TaxonomyCategory.HIGHLIGHT_POSITIVE:
                for term in entry.terms:
                    clean = term.strip().lower()
                    if clean:
                        pos_list.append((clean, re.compile(rf"\b{re.escape(clean)}\b", flags=re.IGNORECASE)))
            else:
                term_pats: list[tuple[str, re.Pattern]] = []
                for term in entry.terms:
                    clean = term.strip().lower()
                    if clean:
                        if clean.startswith("#") or clean.endswith(":"):
                            pat = re.compile(re.escape(clean), flags=re.IGNORECASE)
                        else:
                            pat = re.compile(rf"\b{re.escape(clean)}\b", flags=re.IGNORECASE)
                        term_pats.append((clean, pat))
                raw_pats = [re.compile(rp, flags=re.IGNORECASE) for rp in entry.regex_patterns if rp]
                neg_list.append((entry, term_pats, raw_pats))

        # Ensure plural/hyphenated positive forms are always covered
        for extra_term, extra_pat in (
            ("highlights", re.compile(r"\bhighlights?\b", flags=re.IGNORECASE)),
            ("mini-match", re.compile(r"\bmini[\s-]*match\b", flags=re.IGNORECASE)),
            ("goals", re.compile(r"\bgols?\b", flags=re.IGNORECASE)),
            ("extended highlights", re.compile(r"\bextended highlights?\b", flags=re.IGNORECASE)),
        ):
            pos_list.append((extra_term, extra_pat))

        self._compiled_positive = pos_list
        self._compiled_negative = neg_list
        self._loaded_count = len(entries)

    def classify(
        self,
        title: str,
        home_team: str | None = None,
        away_team: str | None = None,
    ) -> TitleClassificationResult:
        """Runs Stage 0 entity masking followed by positive & negative taxonomy evaluation."""
        self._ensure_compiled()
        if not title:
            return TitleClassificationResult()

        masked_title = club_entity_resolver.mask_club_entities(title, home_team, away_team)
        t_eval = masked_title.lower()

        pos_matched: list[str] = []
        for term, pat in self._compiled_positive:
            if pat.search(t_eval) and term not in pos_matched:
                pos_matched.append(term)

        has_pos = len(pos_matched) > 0
        has_override_kw = has_pos or any(
            re.search(rf"\b{re.escape(kw)}\b", t_eval) for kw in STRUCTURAL_OVERRIDE_KEYWORDS
        )

        for entry, term_pats, raw_pats in self._compiled_negative:
            if entry.override_if_positive_highlight and has_override_kw:
                continue

            for term, pat in term_pats:
                if pat.search(t_eval):
                    return TitleClassificationResult(
                        is_forbidden=True,
                        rejection_reason=entry.rejection_label or entry.category.value,
                        matched_category=entry.category,
                        matched_term=term,
                        has_positive_highlight_keyword=has_pos,
                        positive_keywords_matched=pos_matched,
                        masked_title=masked_title,
                    )

            for r_pat in raw_pats:
                if r_pat.search(t_eval):
                    return TitleClassificationResult(
                        is_forbidden=True,
                        rejection_reason=entry.rejection_label or entry.category.value,
                        matched_category=entry.category,
                        matched_term=r_pat.pattern,
                        has_positive_highlight_keyword=has_pos,
                        positive_keywords_matched=pos_matched,
                        masked_title=masked_title,
                    )

        return TitleClassificationResult(
            is_forbidden=False,
            has_positive_highlight_keyword=has_pos,
            positive_keywords_matched=pos_matched,
            masked_title=masked_title,
        )

    def check_forbidden_title(
        self,
        title: str,
        home_team: str | None = None,
        away_team: str | None = None,
    ) -> tuple[bool, str | None]:
        res = self.classify(title, home_team, away_team)
        return res.is_forbidden, res.rejection_reason

    def has_highlight_keyword(self, title: str) -> bool:
        res = self.classify(title)
        return res.has_positive_highlight_keyword


title_classifier = MultilingualTitleClassifier()
