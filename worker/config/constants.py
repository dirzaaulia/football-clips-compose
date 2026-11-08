"""Clean taxonomy facade backed by PostgreSQL / seed TaxonomyEntry models."""

from worker.models.taxonomy_models import TaxonomyCategory
from worker.repositories.taxonomy_seed_loader import load_seed_taxonomy

_SEED_ENTRIES = load_seed_taxonomy()

HIGHLIGHT_TAXONOMY: dict[str, list[str]] = {
    entry.language: list(entry.terms)
    for entry in _SEED_ENTRIES
    if entry.category == TaxonomyCategory.HIGHLIGHT_POSITIVE
}

HIGHLIGHT_REQUIRED_KEYWORDS: list[str] = sorted({
    rf"\b{term.strip().lower()}\b"
    for terms in HIGHLIGHT_TAXONOMY.values()
    for term in terms
    if term.strip()
} | {
    r"\bhighlights?\b",
    r"\bmini[\s-]*match\b",
    r"\bgols?\b",
    r"\bextended highlights?\b",
})

FORBIDDEN_TITLE_PATTERNS: list[tuple[str, str]] = [
    (
        "|".join(
            [rf"\b{t}\b" if not t.startswith("#") else t for t in entry.terms] + list(entry.regex_patterns),
        ),
        entry.rejection_label or entry.category.value,
    )
    for entry in _SEED_ENTRIES
    if entry.category not in (TaxonomyCategory.HIGHLIGHT_POSITIVE, TaxonomyCategory.CLUB_AFFIXES, TaxonomyCategory.FULL_REPLAY)
]

__all__ = [
    "FORBIDDEN_TITLE_PATTERNS",
    "HIGHLIGHT_REQUIRED_KEYWORDS",
    "HIGHLIGHT_TAXONOMY",
]
