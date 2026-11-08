"""Seed and database loader for typed BroadcasterEntity, TaxonomyEntry, and Club Alias catalogs."""

import json
from pathlib import Path
from typing import Any

from loguru import logger

from worker.models.taxonomy_models import BroadcasterEntity, TaxonomyEntry

TAXONOMY_DIR = Path(__file__).resolve().parent.parent / "config" / "taxonomy"


def load_seed_broadcasters() -> list[BroadcasterEntity]:
    """Loads default BroadcasterEntity models from broadcasters_seed.json."""
    path = TAXONOMY_DIR / "broadcasters_seed.json"
    try:
        raw = json.loads(path.read_text(encoding="utf-8"))
        return [BroadcasterEntity.model_validate(item) for item in raw]
    except Exception as e:
        logger.warning(f"[TaxonomySeedLoader] Failed to load broadcasters_seed.json: {e}")
        return []


def load_seed_taxonomy() -> list[TaxonomyEntry]:
    """Loads default TaxonomyEntry models from multilingual_taxonomy.json."""
    path = TAXONOMY_DIR / "multilingual_taxonomy.json"
    try:
        raw = json.loads(path.read_text(encoding="utf-8"))
        return [TaxonomyEntry.model_validate(item) for item in raw]
    except Exception as e:
        logger.warning(f"[TaxonomySeedLoader] Failed to load multilingual_taxonomy.json: {e}")
        return []


def load_seed_club_aliases() -> dict[str, list[str]]:
    """Loads canonical club nickname/alias map from club_aliases_seed.json."""
    path = TAXONOMY_DIR / "club_aliases_seed.json"
    try:
        raw = json.loads(path.read_text(encoding="utf-8"))
        return {str(k).lower().strip(): [str(v).lower().strip() for v in vals] for k, vals in raw.items()}
    except Exception as e:
        logger.warning(f"[TaxonomySeedLoader] Failed to load club_aliases_seed.json: {e}")
        return {}


def fetch_db_broadcasters(db_client: Any, fallback: list[BroadcasterEntity]) -> list[BroadcasterEntity]:
    """Fetches active broadcasters from PostgreSQL public.broadcasters with seed fallback."""
    try:
        res = db_client.table("broadcasters").select("*").eq("status", "ACTIVE").execute()
        if res.data:
            return [BroadcasterEntity.model_validate(r) for r in res.data]
    except Exception:
        pass
    return fallback


def fetch_db_taxonomy(db_client: Any, fallback: list[TaxonomyEntry]) -> list[TaxonomyEntry]:
    """Fetches highlight classification taxonomy from PostgreSQL public.highlight_taxonomy with seed fallback."""
    try:
        res = db_client.table("highlight_taxonomy").select("*").execute()
        if res.data:
            return [TaxonomyEntry.model_validate(r) for r in res.data]
    except Exception:
        pass
    return fallback
