"""Archive and recent fixture matching helpers for WebSub highlight pushes."""

from datetime import datetime, timedelta
import html
import re
from typing import Any

from loguru import logger


def search_historical_archive(
    active_sb: Any,
    home_aliases: list[str],
    away_aliases: list[str],
    title: str,
) -> dict[str, Any] | None:
    """Cross-references older highlight uploads into supported PostgreSQL archive matches."""
    try:
        t_clean = html.unescape(title).lower()
        res = (
            active_sb.table("matches")
            .select(
                "id, home_team_name, away_team_name, competition_id, competition_name, "
                "utc_date, highlight_video_id, youtube_region_info, home_score, away_score, status",
            )
            .eq("status", "FINISHED")
            .order("utc_date", desc=True)
            .limit(100)
            .execute()
        )
        for m in res.data or []:
            h_hits = any(re.search(rf"\b{re.escape(al)}\b", t_clean) for al in home_aliases)
            a_hits = any(re.search(rf"\b{re.escape(al)}\b", t_clean) for al in away_aliases)
            if h_hits and a_hits:
                return m
    except Exception as e:
        logger.debug(f"[WebSub Archive] Archive search error: {e}")
    return None


def find_matching_fixture_for_websub(
    active_sb: Any,
    highlight_service: Any,
    title: str,
    now_utc: datetime,
) -> tuple[dict[str, Any] | None, str | None]:
    """Finds the best matching recent (72h) or archive finished fixture for a WebSub video title."""
    cutoff_72h = (now_utc - timedelta(hours=72)).isoformat()
    try:
        res = (
            active_sb.table("matches")
            .select(
                "id, home_team_name, away_team_name, competition_id, competition_name, "
                "utc_date, highlight_video_id, youtube_region_info, home_score, away_score, status",
            )
            .eq("status", "FINISHED")
            .gte("utc_date", cutoff_72h)
            .order("utc_date", desc=True)
            .limit(50)
            .execute()
        )
        candidates = res.data or []
    except Exception as e:
        logger.error(f"[WebSub Unified] DB error fetching candidates: {e}")
        return None, str(e)

    t_clean = html.unescape(title).lower()
    for m in candidates:
        home = m.get("home_team_name", "")
        away = m.get("away_team_name", "")
        h_aliases = highlight_service.get_team_aliases(home)
        a_aliases = highlight_service.get_team_aliases(away)
        home_hit = any(re.search(rf"\b{re.escape(a)}\b", t_clean) for a in h_aliases)
        away_hit = any(re.search(rf"\b{re.escape(a)}\b", t_clean) for a in a_aliases)
        if home_hit and away_hit:
            return m, None

    for m in candidates:
        h_aliases = highlight_service.get_team_aliases(m.get("home_team_name", ""))
        a_aliases = highlight_service.get_team_aliases(m.get("away_team_name", ""))
        archived = search_historical_archive(active_sb, h_aliases, a_aliases, title)
        if archived:
            return archived, None

    return None, None
