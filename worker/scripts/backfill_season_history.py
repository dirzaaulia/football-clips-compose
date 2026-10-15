import argparse
import asyncio
from datetime import UTC, datetime
from typing import Any

import httpx
from loguru import logger
from worker.clients.espn_client import espn_client
from worker.clients.supabase_client import supabase_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.services.highlight_service import highlight_service


# Season start dates used as seed queries to retrieve season calendar
SEASON_SEED_DATES = {
    2024: "20240915",
    2025: "20250915",
    2026: "20260915",
}


async def find_historical_highlight(client: httpx.AsyncClient, match: dict) -> str | None:
    """Find and verify official highlight for a historical match with 0 YouTube Data API quota."""
    home = match.get("home_team_name", "")
    away = match.get("away_team_name", "")
    h_score = match.get("home_score")
    a_score = match.get("away_score")
    utc_date = match.get("utc_date", "")
    m_year = utc_date[:4] if len(utc_date) >= 4 else "2024"

    query = f"{home} vs {away} {m_year} highlights official"
    try:
        candidates = await youtube_rss_client.search_public_highlights_async(query, client=client)
        if not candidates:
            return None

        h_aliases = highlight_service.get_team_aliases(home)
        a_aliases = highlight_service.get_team_aliases(away)

        scored_candidates = []
        for c in candidates:
            vid = c.get("id", {}).get("videoId")
            if not vid:
                continue
            title = c.get("snippet", {}).get("title", "")
            dur = c.get("duration", 0)
            score = highlight_service.fallback_score_video(
                title=title,
                duration_sec=dur,
                home_team=home,
                away_team=away,
                h_aliases=h_aliases,
                a_aliases=a_aliases,
                home_score=h_score,
                away_score=a_score,
                match_utc=utc_date,
                channel_handle=c.get("channel"),
            )
            if score > 0:
                scored_candidates.append((score, vid, title))

        if not scored_candidates:
            return None

        scored_candidates.sort(key=lambda x: x[0], reverse=True)
        best_score, best_vid, best_title = scored_candidates[0]

        # Only link if candidate achieves strong confidence score
        if best_score >= 45:
            # Check embeddability via oEmbed (0 quota)
            if youtube_rss_client.check_oembed_embeddable(best_vid):
                return best_vid
    except Exception as e:
        logger.debug(f"[Historical Highlight] Error checking {home} vs {away}: {e}")

    return None


async def fetch_calendar_for_league(
    client: httpx.AsyncClient,
    slug: str,
    seed_date: str
) -> list[str]:
    """Retrieve all match dates for a given league season from ESPN calendar metadata."""
    url = f"{espn_client.BASE_URL}/{slug}/scoreboard?dates={seed_date}"
    try:
        resp = await client.get(url)
        if resp.status_code != 200:
            logger.warning(f"[Backfill] Failed to fetch calendar for {slug} (seed {seed_date}): HTTP {resp.status_code}")
            return []
        data = resp.json()
        leagues = data.get("leagues", [])
        if not leagues:
            return []
        raw_calendar = leagues[0].get("calendar", [])
        dates = []
        for item in raw_calendar:
            # ESPN calendar entries are ISO strings like "2024-08-16T07:00Z"
            if isinstance(item, str) and len(item) >= 10:
                dates.append(item[:10].replace("-", ""))
        return sorted(list(set(dates)))
    except Exception as e:
        logger.error(f"[Backfill] Error fetching calendar for {slug}: {e}")
        return []


async def fetch_day_events(
    client: httpx.AsyncClient,
    sem: asyncio.Semaphore,
    slug: str,
    day_str: str,
    league_info: dict[str, Any],
) -> list[dict[str, Any]]:
    """Fetch and parse all matches for a single day with concurrency throttling."""
    async with sem:
        try:
            data = await espn_client.fetch_scoreboard_day(client, slug, day_str)
            if not data:
                return []

            calendar = data.get("leagues", [{}])[0].get("calendar", [])
            events = data.get("events", [])
            parsed_matches = []

            for ev in events:
                m = espn_client.parse_event(ev, league_info, calendar)
                if m:
                    parsed_matches.append(m)

            return parsed_matches
        except Exception as e:
            logger.debug(f"[Backfill] Error fetching {slug} on {day_str}: {e}")
            return []


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
    logger.info(f"[{comp_code}] [{comp_name}] Discovering season {season_year}/{season_year + 1} match dates (seed: {seed_date})...")

    match_dates = await fetch_calendar_for_league(client, slug, seed_date)
    if not match_dates:
        logger.warning(f"[{comp_code}] No calendar dates returned for season {season_year}. Trying standard fallbacks...")
        # Fallback: probe first week of each month August..May
        for m in [8, 9, 10, 11, 12, 1, 2, 3, 4, 5]:
            y = season_year if m >= 8 else season_year + 1
            match_dates.append(f"{y}{m:02d}15")

    logger.info(f"[{comp_code}] Found {len(match_dates)} match dates across season {season_year}/{season_year + 1}. Fetching scoreboards...")

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

    logger.info(f"[{comp_code}] Successfully parsed {len(all_matches)} fixtures for {comp_name} ({season_year}/{season_year + 1}).")

    if dry_run:
        logger.info(f"[{comp_code}] [DRY RUN] Skipping database upsert for {len(all_matches)} matches.")
        return len(all_matches)

    if not all_matches:
        return 0

    # Fetch existing highlight_video_id from DB so we never overwrite already linked highlights
    match_ids = [m["id"] for m in all_matches]
    existing_highlights = {}
    try:
        for i in range(0, len(match_ids), 100):
            chunk = match_ids[i:i + 100]
            res_db = supabase_client.table("matches").select("id, highlight_video_id").in_("id", chunk).execute()
            for row in (res_db.data or []):
                if row.get("highlight_video_id"):
                    existing_highlights[row["id"]] = row["highlight_video_id"]
    except Exception as e:
        logger.warning(f"[{comp_code}] Could not check existing highlights: {e}")

    now_iso = datetime.now(UTC).isoformat()
    for m in all_matches:
        m["updated_at"] = now_iso
        if m["id"] in existing_highlights:
            m["highlight_video_id"] = existing_highlights[m["id"]]

    # Optional 0-Quota Historical Highlight Search & Linking
    if link_highlights:
        logger.info(f"[{comp_code}] Searching and linking historical highlights via 0-quota YouTube engine...")
        linked_count = 0
        for m in all_matches:
            if m.get("highlight_video_id") or m.get("status") not in ["FINISHED", "AWARDED"]:
                continue
            vid = await find_historical_highlight(client, m)
            if vid:
                m["highlight_video_id"] = vid
                linked_count += 1
                logger.info(f"[{comp_code}] [0-QUOTA LINKED] {m['home_team_name']} vs {m['away_team_name']} -> https://youtu.be/{vid}")
        logger.info(f"[{comp_code}] Successfully linked {linked_count} official highlights with 0 quota units.")

    # Batch upsert to Supabase
    total_upserted = 0
    for i in range(0, len(all_matches), batch_size):
        batch = all_matches[i:i + batch_size]
        try:
            supabase_client.table("matches").upsert(batch, on_conflict="id").execute()
            total_upserted += len(batch)
            logger.info(f"[{comp_code}] Upserted batch {i // batch_size + 1}: {len(batch)} matches ({total_upserted}/{len(all_matches)})")
        except Exception as e:
            logger.error(f"[{comp_code}] Failed to upsert batch {i // batch_size + 1}: {e}")

    return total_upserted


async def run_backfill(
    competitions: list[str],
    season_year: int,
    dry_run: bool = False,
    batch_size: int = 100,
    concurrency: int = 10,
    link_highlights: bool = False,
):
    """Run full historical season backfill engine across requested competitions."""
    logger.info("=" * 70)
    logger.info(f"FOOTBALL CLIPS - HISTORICAL SEASON DATA HOARDING ENGINE")
    logger.info(f"Target Season: {season_year}/{season_year + 1} | Competitions: {', '.join(competitions)}")
    logger.info(f"Dry Run: {dry_run} | Batch Size: {batch_size} | Concurrency: {concurrency} | Link Highlights: {link_highlights}")
    logger.info("=" * 70)

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

    logger.info("=" * 70)
    logger.info(f"BACKFILL COMPLETE: Total fixtures processed: {total_all_comps}")
    logger.info("=" * 70)


def main():
    parser = argparse.ArgumentParser(description="Backfill full season match history from ESPN to Supabase.")
    parser.add_argument(
        "--season",
        type=int,
        default=2024,
        help="Season start year (e.g. 2024 for 2024/2025, 2025 for 2025/2026). Default: 2024"
    )
    parser.add_argument(
        "--competitions",
        type=str,
        default="PL,PD,SA,BL1,FL1,CL",
        help="Comma-separated competition codes or 'all'. Default: PL,PD,SA,BL1,FL1,CL"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Fetch and parse matches without writing to Supabase."
    )
    parser.add_argument(
        "--batch-size",
        type=int,
        default=100,
        help="Supabase batch upsert size. Default: 100"
    )
    parser.add_argument(
        "--concurrency",
        type=int,
        default=10,
        help="Concurrent HTTP workers. Default: 10"
    )

    parser.add_argument(
        "--link-highlights",
        action="store_true",
        help="Search and link verified official highlights for finished matches using 0-quota YouTube engine."
    )

    args = parser.parse_args()

    if args.competitions.lower() == "all":
        target_comps = ["PL", "PD", "SA", "BL1", "FL1", "CL", "EL", "FAC", "EFL", "CDR", "DFB", "CIT"]
    else:
        target_comps = [c.strip().upper() for c in args.competitions.split(",") if c.strip()]

    asyncio.run(
        run_backfill(
            competitions=target_comps,
            season_year=args.season,
            dry_run=args.dry_run,
            batch_size=args.batch_size,
            concurrency=args.concurrency,
            link_highlights=args.link_highlights,
        )
    )


if __name__ == "__main__":
    main()
