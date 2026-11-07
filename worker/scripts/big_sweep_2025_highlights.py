import argparse
import asyncio
import os
import sys
from datetime import UTC, datetime
from typing import Any

# Ensure project root is in sys.path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

import httpx
from loguru import logger

from worker.clients.database_client import db_client
from worker.clients.espn_client import espn_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.services.highlight_service import highlight_service

MISSING_2025_COMPS: list[str] = ["ARG", "BRA", "ECL", "EFL", "EL", "LIB", "SUD"]


async def fetch_and_upsert_fixtures(
    client: httpx.AsyncClient,
    comp_code: str,
    season: int = 2025,
) -> int:
    """Fetch all season fixtures from ESPN scoreboard and upsert to database."""
    info = espn_client.COMPETITION_MAP.get(comp_code)
    if not info:
        logger.warning(f"[{comp_code}] Unknown competition code.")
        return 0

    slug = info["slug"]
    comp_name = info["name"]
    league_info = {"code": comp_code, **info}

    url = f"{espn_client.BASE_URL}/{slug}/scoreboard?dates={season}&limit=1000"
    logger.info(f"[{comp_code}] [{comp_name}] Fetching season {season} fixtures from ESPN: {url}")

    try:
        resp = await client.get(url, timeout=20.0)
        if resp.status_code != 200:
            logger.error(f"[{comp_code}] ESPN API HTTP {resp.status_code}")
            return 0
        data = resp.json()
    except Exception as e:
        logger.error(f"[{comp_code}] Failed to fetch from ESPN: {e}")
        return 0

    events = data.get("events", [])
    if not events:
        logger.warning(f"[{comp_code}] No events found for season {season}.")
        return 0

    calendar_dates = [d[:10].replace("-", "") for d in data.get("leagues", [{}])[0].get("calendar", []) if isinstance(d, str)]
    parsed_matches: list[dict[str, Any]] = []

    for ev in events:
        m = espn_client.parse_event(ev, league_info, calendar_dates)
        if m:
            m["season"] = season
            m["updated_at"] = datetime.now(UTC).isoformat()
            parsed_matches.append(m)

    logger.info(f"[{comp_code}] Parsed {len(parsed_matches)} matches for season {season}.")

    # Fetch existing highlights to ensure we never overwrite
    match_ids = [m["id"] for m in parsed_matches]
    existing_highlights: dict[int, str] = {}
    try:
        for i in range(0, len(match_ids), 100):
            chunk = match_ids[i:i + 100]
            res_db = db_client.table("matches").select("id, highlight_video_id").in_("id", chunk).execute()
            for row in (res_db.data or []):
                if row.get("highlight_video_id"):
                    existing_highlights[row["id"]] = row["highlight_video_id"]
    except Exception as e:
        logger.warning(f"[{comp_code}] Could not check existing highlights: {e}")

    for m in parsed_matches:
        if m["id"] in existing_highlights:
            m["highlight_video_id"] = existing_highlights[m["id"]]

    # Batch upsert in chunks of 100
    upserted_count = 0
    for i in range(0, len(parsed_matches), 100):
        batch = parsed_matches[i:i + 100]
        try:
            db_client.table("matches").upsert(batch, on_conflict="id").execute()
            upserted_count += len(batch)
        except Exception as e:
            logger.error(f"[{comp_code}] Error upserting batch {i // 100 + 1}: {e}")

    # Upsert standings if available
    try:
        standings = await espn_client.get_standings(comp_code, season=season, client=client)
        if standings:
            db_client.table("standings").upsert(standings, on_conflict="id").execute()
            logger.info(f"[{comp_code}] Upserted {len(standings)} standings rows.")
    except Exception as e:
        logger.debug(f"[{comp_code}] Standings fetch skipped: {e}")

    logger.info(f"[{comp_code}] Successfully upserted {upserted_count} fixtures.")
    return upserted_count


async def search_match_highlight(
    client: httpx.AsyncClient,
    match: dict[str, Any],
) -> str | None:
    """Find and verify official highlight video for a historical match (0 quota units)."""
    home = match.get("home_team_name", "")
    away = match.get("away_team_name", "")
    h_score = match.get("home_score")
    a_score = match.get("away_score")
    utc_date = match.get("utc_date", "")
    m_year = utc_date[:4] if len(utc_date) >= 4 else "2025"
    comp_id = match.get("competition_id", "")

    if comp_id == "IDN":
        query = f"{home} vs {away} {m_year} highlight vidio liga 1"
    else:
        query = f"{home} vs {away} {m_year} highlights ({home} OR {away}) official"

    try:
        candidates = await youtube_rss_client.search_public_highlights_async(query, client=client)
        if not candidates:
            return None

        h_aliases = highlight_service.get_team_aliases(home)
        a_aliases = highlight_service.get_team_aliases(away)
        scored: list[tuple[float, str, str, str]] = []

        for c in candidates:
            vid = c.get("id", {}).get("videoId")
            if not vid:
                continue
            title = c.get("snippet", {}).get("title", "")
            dur = c.get("duration", 0)
            ch = c.get("channel", "") or c.get("snippet", {}).get("channelTitle", "")

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
                channel_handle=ch,
            )

            # Indonesian broadcaster Vidio is legitimate for IDN matches
            if comp_id != "IDN" and ch and any(r in ch.lower() for r in ["indonesia", "asia", "uk", "sky sports", "bein"]):
                score -= 30

            if score > 0:
                scored.append((score, vid, title, ch))

        if not scored:
            return None

        scored.sort(key=lambda x: x[0], reverse=True)

        for score, vid, title, ch in scored:
            if score < 25:
                break
            is_playable, _, count, reason = youtube_rss_client.check_video_global_availability(vid, min_countries=50)
            if is_playable:
                return vid
    except Exception as e:
        logger.debug(f"[BigSweep] Search error {home} vs {away}: {e}")

    return None


async def sweep_unlinked_matches(
    client: httpx.AsyncClient,
    season: int = 2025,
    max_matches: int = 200,
    concurrency: int = 4,
) -> int:
    """Sweep through unlinked finished matches in season archive and link highlights."""
    res = (
        db_client.table("matches")
        .select("id, competition_id, home_team_name, away_team_name, home_score, away_score, utc_date")
        .eq("season", season)
        .eq("status", "FINISHED")
        .is_("highlight_video_id", "null")
        .order("utc_date", desc=True)
        .limit(max_matches)
        .execute()
    )

    unlinked = res.data or []
    logger.info(f"[BigSweep] Found {len(unlinked)} unlinked finished matches in Season {season}.")
    if not unlinked:
        return 0

    sem = asyncio.Semaphore(concurrency)
    linked_count = 0

    async def process_match(m: dict[str, Any]):
        nonlocal linked_count
        async with sem:
            vid = await search_match_highlight(client, m)
            if vid:
                now_iso = datetime.now(UTC).isoformat()
                try:
                    db_client.table("matches").update({
                        "highlight_video_id": vid,
                        "updated_at": now_iso,
                        "last_youtube_check": now_iso,
                    }).eq("id", m["id"]).execute()
                    linked_count += 1
                    logger.info(f"[LINKED #{linked_count}] [{m['competition_id']}] {m['home_team_name']} vs {m['away_team_name']} -> https://youtu.be/{vid}")
                except Exception as e:
                    logger.error(f"[BigSweep] DB update error {m['id']}: {e}")

    tasks = [process_match(m) for m in unlinked]
    await asyncio.gather(*tasks, return_exceptions=True)
    logger.info(f"[BigSweep] Finished sweep: successfully linked {linked_count}/{len(unlinked)} matches.")
    return linked_count


async def run_big_sweep(
    ingest_fixtures: bool = True,
    sweep_highlights: bool = True,
    season: int = 2025,
    max_sweep: int = 200,
    concurrency: int = 4,
):
    """Execute complete Season 2025 parity ingestion and highlight sweep."""
    logger.info("=" * 70)
    logger.info("SEASON 2025 BIG SWEEP & 1:1 PARITY RESTORATION ENGINE")
    logger.info(f"Season: {season} | Ingest Fixtures: {ingest_fixtures} | Sweep Highlights: {sweep_highlights}")
    logger.info("=" * 70)

    async with httpx.AsyncClient(timeout=20.0, headers=espn_client.headers) as client:
        if ingest_fixtures:
            logger.info(f"Ingesting missing season {season} fixtures for: {', '.join(MISSING_2025_COMPS)}")
            total_fixtures = 0
            for comp in MISSING_2025_COMPS:
                cnt = await fetch_and_upsert_fixtures(client, comp, season=season)
                total_fixtures += cnt
            logger.info(f"Total fixtures ingested across missing competitions: {total_fixtures}")

        if sweep_highlights:
            logger.info(f"Running 0-quota highlight sweep across Season {season} unlinked fixtures...")
            await sweep_unlinked_matches(client, season=season, max_matches=max_sweep, concurrency=concurrency)


def main():
    parser = argparse.ArgumentParser(description="Season 2025 Big Sweep & Ingestion Engine")
    parser.add_argument("--season", type=int, default=2025, help="Target season year (default: 2025)")
    parser.add_argument("--skip-ingest", action="store_true", help="Skip fixture ingestion")
    parser.add_argument("--skip-sweep", action="store_true", help="Skip highlight sweep")
    parser.add_argument("--max-sweep", type=int, default=200, help="Max unlinked matches to sweep")
    parser.add_argument("--concurrency", type=int, default=4, help="Concurrency for highlight searches")
    args = parser.parse_args()

    asyncio.run(
        run_big_sweep(
            ingest_fixtures=not args.skip_ingest,
            sweep_highlights=not args.skip_sweep,
            season=args.season,
            max_sweep=args.max_sweep,
            concurrency=args.concurrency,
        ),
    )


if __name__ == "__main__":
    main()
