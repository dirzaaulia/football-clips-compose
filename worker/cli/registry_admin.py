"""Unified PostgreSQL-backed Registry Administration CLI.

Usage:
    python -m worker.cli.registry_admin --seed --audit --heal-cids
"""

import argparse
import os
import re

from loguru import logger
import psycopg

from worker.clients.database_client import db_client
from worker.clients.youtube_client import youtube_client
from worker.repositories.channel_repository import channel_repository
from worker.repositories.taxonomy_seed_loader import (
    load_seed_broadcasters,
    load_seed_club_aliases,
    load_seed_taxonomy,
)

DDL_STATEMENTS = """
CREATE TABLE IF NOT EXISTS public.broadcasters (
    broadcaster_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    channel_handle TEXT NOT NULL,
    channel_id TEXT,
    competitions JSONB DEFAULT '[]'::jsonb,
    allowed_countries JSONB DEFAULT '[]'::jsonb,
    contract_start_year INT DEFAULT 2024,
    contract_end_year INT DEFAULT 2030,
    status TEXT DEFAULT 'ACTIVE',
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.highlight_taxonomy (
    id TEXT PRIMARY KEY,
    category TEXT NOT NULL,
    language TEXT NOT NULL DEFAULT 'global',
    terms JSONB DEFAULT '[]'::jsonb,
    regex_patterns JSONB DEFAULT '[]'::jsonb,
    override_if_positive_highlight BOOLEAN DEFAULT FALSE,
    rejection_label TEXT DEFAULT '',
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

GRANT SELECT ON public.broadcasters TO web_anon;
GRANT SELECT ON public.highlight_taxonomy TO web_anon;
NOTIFY pgrst, 'reload schema';
"""


def _ensure_postgres_tables():
    pg_url = os.getenv("POSTGRES_URL") or os.getenv("DATABASE_URL")
    if not pg_url:
        return
    try:
        with psycopg.connect(pg_url, autocommit=True) as conn, conn.cursor() as cur:
            cur.execute(DDL_STATEMENTS)
            logger.info("[RegistryAdmin] Verified public.broadcasters and public.highlight_taxonomy tables in PostgreSQL.")
    except Exception as e:
        logger.warning(f"[RegistryAdmin] Direct DDL notice (continuing with PostgREST): {e}")


def run_seed():
    """Seeds public.broadcasters, public.highlight_taxonomy, and merges club_aliases_seed into public.supported_clubs."""
    _ensure_postgres_tables()

    broadcasters = load_seed_broadcasters()
    try:
        b_rows = [b.model_dump() for b in broadcasters]
        db_client.table("broadcasters").upsert(b_rows, on_conflict="broadcaster_id").execute()
        logger.info(f"[RegistryAdmin] Upserted {len(b_rows)} BroadcasterEntity records into public.broadcasters.")
    except Exception as e:
        logger.warning(f"[RegistryAdmin] Broadcasters upsert notice: {e}")

    taxonomy = load_seed_taxonomy()
    try:
        t_rows = [
            {
                "id": f"{entry.category.value.lower()}_{entry.language.lower()}",
                "category": entry.category.value,
                "language": entry.language,
                "terms": entry.terms,
                "regex_patterns": entry.regex_patterns,
                "override_if_positive_highlight": entry.override_if_positive_highlight,
                "rejection_label": entry.rejection_label,
            }
            for entry in taxonomy
        ]
        db_client.table("highlight_taxonomy").upsert(t_rows, on_conflict="id").execute()
        logger.info(f"[RegistryAdmin] Upserted {len(t_rows)} TaxonomyEntry records into public.highlight_taxonomy.")
    except Exception as e:
        logger.warning(f"[RegistryAdmin] Taxonomy upsert notice: {e}")

    seed_aliases = load_seed_club_aliases()
    try:
        res = db_client.table("supported_clubs").select("id,name,short_name,aliases").eq("is_active", True).execute()
        updated_clubs = 0
        for row in res.data or []:
            name_low = (row.get("name") or "").lower()
            existing = [str(a).lower().strip() for a in (row.get("aliases") or []) if a]
            merged = set(existing)
            for key, vals in seed_aliases.items():
                if key == "racing club":
                    if "racing" in name_low and not any(x in name_low for x in ("santander", "ferrol", "lens")):
                        merged.update(vals)
                elif key == "independiente":
                    if "independiente" in name_low and "rivadavia" not in name_low:
                        merged.update(vals)
                elif key in name_low:
                    merged.update(vals)
            if len(merged) > len(existing):
                db_client.table("supported_clubs").update({"aliases": sorted(merged)}).eq("id", row["id"]).execute()
                updated_clubs += 1
        logger.info(f"[RegistryAdmin] Enriched aliases for {updated_clubs} clubs in public.supported_clubs.")
    except Exception as e:
        logger.warning(f"[RegistryAdmin] Club alias merge notice: {e}")


def run_audit():
    """Audits all match teams against ChannelRepository to verify 0 missing club entities."""
    channel_repository.ensure_loaded(force=True)
    res = db_client.table("matches").select("home_team_name,away_team_name,competition_id").limit(3000).execute()
    teams: set[str] = set()
    for m in res.data or []:
        if m.get("home_team_name"):
            teams.add(m["home_team_name"])
        if m.get("away_team_name"):
            teams.add(m["away_team_name"])

    missing: list[str] = []
    for t in sorted(teams):
        if t in ("TBD", "To Be Determined"):
            continue
        row = channel_repository._find_club_row(t)
        if not row:
            missing.append(t)

    logger.info(
        f"[RegistryAdmin] Audit Complete: Total unique match clubs={len(teams)}, "
        f"Missing from supported_clubs={len(missing)}, "
        f"Broadcasters={len(channel_repository.get_broadcasters())}, "
        f"Taxonomy entries={len(channel_repository.get_taxonomy_entries())}."
    )
    if missing:
        logger.warning(f"[RegistryAdmin] Missing clubs: {missing[:20]}")


def run_heal_cids():
    """Resolves any missing YouTube UC... channel IDs for active clubs and competitions."""
    res = (
        db_client.table("supported_clubs")
        .select("id,name,youtube_handle,youtube_channel_id")
        .eq("is_active", True)
        .execute()
    )
    healed = 0
    for row in res.data or []:
        handle = (row.get("youtube_handle") or "").strip()
        cid = (row.get("youtube_channel_id") or "").strip()
        if handle and not re.match(r"^UC[A-Za-z0-9_-]{22}$", cid):
            resolved = youtube_client.resolve_channel_id(handle)
            if resolved and re.match(r"^UC[A-Za-z0-9_-]{22}$", resolved):
                db_client.table("supported_clubs").update({"youtube_channel_id": resolved}).eq("id", row["id"]).execute()
                healed += 1
    logger.info(f"[RegistryAdmin] Healed {healed} missing club YouTube channel IDs.")


def main():
    parser = argparse.ArgumentParser(description="FootballClips PostgreSQL Registry Admin CLI")
    parser.add_argument("--seed", action="store_true", help="Seed broadcasters, taxonomy, and club aliases into PostgreSQL")
    parser.add_argument("--audit", action="store_true", help="Audit all match clubs, competitions, and broadcasters")
    parser.add_argument("--heal-cids", action="store_true", help="Resolve missing YouTube channel IDs")
    args = parser.parse_args()

    if args.seed:
        run_seed()
    if args.heal_cids:
        run_heal_cids()
    if args.audit or not (args.seed or args.heal_cids):
        run_audit()


if __name__ == "__main__":
    main()
