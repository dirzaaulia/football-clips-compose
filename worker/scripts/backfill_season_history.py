"""Backfill full season match history from ESPN to Supabase."""

import argparse
import asyncio
from datetime import UTC, datetime
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

import httpx
from loguru import logger

from worker.clients.database_client import db_client as supabase_client
from worker.clients.espn_client import espn_client
from worker.scripts.backfill_helpers import (
    SEASON_SEED_DATES,
    fetch_calendar_for_league,
    fetch_day_events,
    find_historical_highlight,
)


async def backfill_competition(
    client: httpx.AsyncClient,
    sem: asyncio.Semaphore,
    comp_code: str,
    season_year: int,
    dry_run: bool = False,
    batch_size: int = 100,
    link_highlights: bool = False,
) -> int:
    """Backfill all fixtures for one competition across an entire season."""
    info = espn_client.COMPETITION_MAP.get(comp_code)
    if not info:
        logger.warning(f"[Backfill] Unknown competition code: {comp_code}")
        return 0

    slug = info["slug"]
    comp_name = info["name"]
    league_info = {"code": comp_code, **info}

    seed_date = SEASON_SEED_DATES.get(season_year, f"{season_year}0915")
    logger.info(f"[{comp_code}] [{comp_name}] Discovering season {season_year}/{season_year + 1} match dates...")

    match_dates = await fetch_calendar_for_league(client, slug, seed_date, season_year)
    if not match_dates:
        for m in [8, 9, 10, 11, 12, 1, 2, 3, 4, 5]:
            y = season_year if m >= 8 else season_year + 1
            match_dates.append(f"{y}{m:02d}15")

    tasks = [fetch_day_events(client, sem, slug, d, league_info) for d in match_dates]
    results = await asyncio.gather(*tasks, return_exceptions=True)

    all_matches = []
    seen_ids = set()
    for res in results:
        if isinstance(res, Exception) or not res:
            continue
        for m in res:
            if m["id"] not in seen_ids:
                seen_ids.add(m["id"])
                all_matches.append(m)

    logger.info(f"[{comp_code}] Parsed {len(all_matches)} fixtures for {comp_name} ({season_year}/{season_year + 1}).")
    if dry_run or not all_matches:
        return len(all_matches)

    match_ids = [m["id"] for m in all_matches]
    existing_highlights = {}
    try:
        for i in range(0, len(match_ids), 100):
            chunk = match_ids[i : i + 100]
            res_db = supabase_client.table("matches").select("id, highlight_video_id").in_("id", chunk).execute()
            for row in res_db.data or []:
                if row.get("highlight_video_id"):
                    existing_highlights[row["id"]] = row["highlight_video_id"]
    except Exception as e:
        logger.warning(f"[{comp_code}] Could not check existing highlights: {e}")

    now_iso = datetime.now(UTC).isoformat()
    for m in all_matches:
        m["season"] = season_year
        m["updated_at"] = now_iso
        if m["id"] in existing_highlights:
            m["highlight_video_id"] = existing_highlights[m["id"]]

    if link_highlights:
        highlight_sem = asyncio.Semaphore(6)
        linked_counter = 0

        async def check_match_highlight(m_item: dict):
            nonlocal linked_counter
            if m_item.get("highlight_video_id") or m_item.get("status") not in ["FINISHED", "AWARDED"]:
                return
            async with highlight_sem:
                vid = await find_historical_highlight(client, m_item)
                if vid:
                    m_item["highlight_video_id"] = vid
                    linked_counter += 1
                    logger.info(
                        f"[{comp_code}] [0-QUOTA LINKED #{linked_counter}] "
                        f"{m_item['home_team_name']} vs {m_item['away_team_name']} -> https://youtu.be/{vid}",
                    )

        await asyncio.gather(*[check_match_highlight(m) for m in all_matches], return_exceptions=True)

    total_upserted = 0
    for i in range(0, len(all_matches), batch_size):
        batch = all_matches[i : i + batch_size]
        try:
            supabase_client.table("matches").upsert(batch, on_conflict="id").execute()
            total_upserted += len(batch)
        except Exception as e:
            logger.error(f"[{comp_code}] Failed to upsert batch {i // batch_size + 1}: {e}")

    try:
        standings_rows = await espn_client.get_standings(comp_code, season=season_year, client=client)
        if standings_rows and not dry_run:
            supabase_client.table("standings").upsert(standings_rows, on_conflict="id").execute()
    except Exception as e:
        logger.warning(f"[{comp_code}] Failed to backfill standings for season {season_year}: {e}")

    return total_upserted


async def run_backfill(
    competitions: list[str],
    season_year: int,
    dry_run: bool = False,
    batch_size: int = 100,
    concurrency: int = 10,
    link_highlights: bool = False,
):
    sem = asyncio.Semaphore(concurrency)
    total_all_comps = 0
    async with httpx.AsyncClient(timeout=20.0, headers=espn_client.headers) as client:
        for comp_code in competitions:
            count = await backfill_competition(
                client=client,
                sem=sem,
                comp_code=comp_code,
                season_year=season_year,
                dry_run=dry_run,
                batch_size=batch_size,
                link_highlights=link_highlights,
            )
            total_all_comps += count
    logger.info(f"BACKFILL COMPLETE: Total fixtures processed: {total_all_comps}")


def main():
    parser = argparse.ArgumentParser(description="Backfill full season match history from ESPN to Supabase.")
    parser.add_argument("--season", type=int, default=2025)
    parser.add_argument("--competitions", type=str, default="all")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--batch-size", type=int, default=100)
    parser.add_argument("--concurrency", type=int, default=10)
    parser.add_argument("--link-highlights", action="store_true")
    args = parser.parse_args()

    target_comps = (
        list(espn_client.COMPETITION_MAP.keys())
        if args.competitions.lower() == "all"
        else [c.strip().upper() for c in args.competitions.split(",") if c.strip()]
    )

    asyncio.run(
        run_backfill(
            competitions=target_comps,
            season_year=args.season,
            dry_run=args.dry_run,
            batch_size=args.batch_size,
            concurrency=args.concurrency,
            link_highlights=args.link_highlights,
        ),
    )


if __name__ == "__main__":
    main()
