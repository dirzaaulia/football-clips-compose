"""Helper functions and season calendar seeds for historical season backfill."""

import asyncio
from typing import Any

import httpx
from loguru import logger

from worker.clients.espn_client import espn_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.services.highlight_service import highlight_service

SEASON_SEED_DATES = {
    2024: "20240915",
    2025: "20250915",
    2026: "20260915",
}

UCL_SEASON_DATES = {
    2024: [
        "20240917", "20240918", "20240919", "20241001", "20241002", "20241022", "20241023",
        "20241105", "20241106", "20241126", "20241127", "20241210", "20241211", "20250121",
        "20250122", "20250129", "20250211", "20250212", "20250218", "20250219", "20250304",
        "20250305", "20250311", "20250312", "20250408", "20250409", "20250415", "20250416",
        "20250429", "20250430", "20250506", "20250507", "20250531",
    ],
    2025: [
        "20250916", "20250917", "20250918", "20250930", "20251001", "20251021", "20251022",
        "20251104", "20251105", "20251125", "20251126", "20251209", "20251210", "20260120",
        "20260121", "20260128", "20260217", "20260218", "20260224", "20260225", "20260310",
        "20260311", "20260317", "20260318", "20260407", "20260408", "20260414", "20260415",
        "20260428", "20260429", "20260505", "20260506", "20260530",
    ],
}


async def find_historical_highlight(client: httpx.AsyncClient, match: dict) -> str | None:
    """Find and verify official highlight for a historical match with 0 YouTube Data API quota."""
    home = match.get("home_team_name", "")
    away = match.get("away_team_name", "")
    h_score = match.get("home_score")
    a_score = match.get("away_score")
    utc_date = match.get("utc_date", "")
    m_year = utc_date[:4] if len(utc_date) >= 4 else "2024"

    query = f"{home} vs {away} {m_year} highlights ({home} OR {away}) official"
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
            ch = c.get("channel", "")

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

            if ch and any(
                r in ch.lower()
                for r in ["indonesia", "asia", "uk", "sky sports", "bein", "vidio", "canal+", "dazn de"]
            ):
                score -= 30

            if score > 0:
                scored_candidates.append((score, vid, title, ch))

        if not scored_candidates:
            return None

        scored_candidates.sort(key=lambda x: x[0], reverse=True)
        best_regional_fallback = None

        for score, vid, title, ch in scored_candidates:
            if score < 25:
                break
            is_playable, _, country_count, _ = youtube_rss_client.check_video_global_availability(
                vid, min_countries=100,
            )
            if not is_playable:
                continue
            if country_count >= 100:
                return vid
            if country_count > 0 and best_regional_fallback is None:
                best_regional_fallback = (vid, ch, score, country_count, title)

        if best_regional_fallback:
            return best_regional_fallback[0]
    except Exception as e:
        logger.debug(f"[Historical Highlight] Error checking {home} vs {away}: {e}")

    return None


async def fetch_calendar_for_league(
    client: httpx.AsyncClient,
    slug: str,
    seed_date: str,
    season_year: int = 2025,
) -> list[str]:
    """Retrieve all match dates for a given league season from ESPN calendar metadata."""
    if slug == "uefa.champions" and season_year in UCL_SEASON_DATES:
        return UCL_SEASON_DATES[season_year]

    url = f"{espn_client.BASE_URL}/{slug}/scoreboard?dates={seed_date}"
    try:
        resp = await client.get(url)
        if resp.status_code != 200:
            return []
        data = resp.json()
        leagues = data.get("leagues", [])
        if not leagues:
            return []
        raw_calendar = leagues[0].get("calendar", [])
        dates = [item[:10].replace("-", "") for item in raw_calendar if isinstance(item, str) and len(item) >= 10]
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
            parsed = [espn_client.parse_event(ev, league_info, calendar) for ev in events]
            return [m for m in parsed if m]
        except Exception as e:
            logger.debug(f"[Backfill] Error fetching {slug} on {day_str}: {e}")
            return []
