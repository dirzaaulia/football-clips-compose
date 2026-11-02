import asyncio
from datetime import UTC, datetime, timedelta
from typing import Any

import httpx
from loguru import logger

from worker.clients.espn_parser import format_round_slug, normalize_status, parse_event, parse_match_summary, parse_standings


class ESPNClient:
    """Enterprise-grade, zero-quota ESPN Soccer API client."""

    BASE_URL = "https://site.api.espn.com/apis/site/v2/sports/soccer"

    COMPETITION_MAP: dict[str, dict[str, str]] = {
        "PL": {"slug": "eng.1", "name": "Premier League", "type": "league"},
        "PD": {"slug": "esp.1", "name": "LaLiga", "type": "league"},
        "SA": {"slug": "ita.1", "name": "Serie A", "type": "league"},
        "BL1": {"slug": "ger.1", "name": "Bundesliga", "type": "league"},
        "FL1": {"slug": "fra.1", "name": "Ligue 1", "type": "league"},
        "CL": {"slug": "uefa.champions", "name": "UEFA Champions League", "type": "cup"},
        "EL": {"slug": "uefa.europa", "name": "UEFA Europa League", "type": "cup"},
        "ECL": {"slug": "uefa.europa.conf", "name": "UEFA Conference League", "type": "cup"},
        "FAC": {"slug": "eng.fa", "name": "FA Cup", "type": "cup"},
        "EFL": {"slug": "eng.league_cup", "name": "Carabao Cup", "type": "cup"},
        "CDR": {"slug": "esp.copa_del_rey", "name": "Copa del Rey", "type": "cup"},
        "DFB": {"slug": "ger.dfb_pokal", "name": "DFB-Pokal", "type": "cup"},
        "CIT": {"slug": "ita.coppa_italia", "name": "Coppa Italia", "type": "cup"},
        "SPL": {"slug": "ksa.1", "name": "Saudi Pro League", "type": "league"},
        "MLS": {"slug": "usa.1", "name": "Major League Soccer", "type": "league"},
        "DED": {"slug": "ned.1", "name": "Eredivisie", "type": "league"},
        "PPL": {"slug": "por.1", "name": "Liga Portugal", "type": "league"},
        "IDN": {"slug": "idn.1", "name": "Liga 1 Indonesia", "type": "league"},
        "ACL": {"slug": "afc.champions", "name": "AFC Champions League Elite", "type": "cup"},
        "LIB": {"slug": "conmebol.libertadores", "name": "CONMEBOL Libertadores", "type": "cup"},
        "SUD": {"slug": "conmebol.sudamericana", "name": "CONMEBOL Sudamericana", "type": "cup"},
        "REC": {"slug": "conmebol.recopa", "name": "CONMEBOL Recopa", "type": "cup"},
        "BRA": {"slug": "bra.1", "name": "Brasileirão Série A", "type": "league"},
        "ARG": {"slug": "arg.1", "name": "Argentine Liga Profesional", "type": "league"},
    }

    SLUG_TO_CODE: dict[str, str] = {v["slug"]: k for k, v in COMPETITION_MAP.items()}

    def __init__(self):
        self.backoff_until = None
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            "Accept": "application/json",
        }

    normalize_status = staticmethod(normalize_status)
    format_round_slug = staticmethod(format_round_slug)
    parse_event = staticmethod(parse_event)

    async def fetch_scoreboard_day(self, client: httpx.AsyncClient, league_slug: str, yyyymmdd: str | None = None) -> dict[str, Any]:
        if self.backoff_until and self.backoff_until > datetime.now(UTC):
            return {}
        url = f"{self.BASE_URL}/{league_slug}/scoreboard"
        params = {"dates": yyyymmdd} if yyyymmdd else {}
        try:
            resp = await client.get(url, params=params, headers=self.headers, timeout=15.0)
            if resp.status_code in (429, 503):
                self.backoff_until = datetime.now(UTC) + timedelta(minutes=5)
            return resp.json() if resp.status_code == 200 else {}
        except Exception as e:
            logger.debug(f"[ESPN] Fetch failed for {league_slug} ({yyyymmdd}): {e}")
            return {}

    async def get_matches(self, competitions: list[str], date_from: str, date_to: str) -> list[dict[str, Any]]:
        try:
            d_start = datetime.strptime(date_from, "%Y-%m-%d").date()
            d_end = datetime.strptime(date_to, "%Y-%m-%d").date()
        except Exception:
            d_start = datetime.now(UTC).date()
            d_end = d_start

        days_to_fetch, curr = [], d_start
        while curr <= d_end:
            days_to_fetch.append(curr.strftime("%Y%m%d"))
            curr += timedelta(days=1)

        target_leagues = [{"code": c, **self.COMPETITION_MAP[c]} for c in competitions if c in self.COMPETITION_MAP]
        if not target_leagues:
            return []

        all_matches, seen_ids = [], set()
        sem = asyncio.Semaphore(4)

        async with httpx.AsyncClient(timeout=25.0) as client:
            async def bounded_fetch(slug: str, day: str):
                async with sem:
                    return await self.fetch_scoreboard_day(client, slug, day)

            tasks = [bounded_fetch(lg["slug"], day) for lg in target_leagues for day in days_to_fetch]
            metadata = [lg for lg in target_leagues for _ in days_to_fetch]
            results = await asyncio.gather(*tasks, return_exceptions=True)

            for result, lg_info in zip(results, metadata, strict=False):
                if isinstance(result, Exception) or not result:
                    continue
                calendar = result.get("leagues", [{}])[0].get("calendar", [])
                for ev in result.get("events", []):
                    parsed = parse_event(ev, lg_info, calendar)
                    if parsed and parsed["id"] not in seen_ids:
                        seen_ids.add(parsed["id"])
                        all_matches.append(parsed)

        logger.info(f"[ESPN] Ingested {len(all_matches)} fixtures across {len(target_leagues)} competitions ({date_from} to {date_to})")
        return all_matches

    async def get_standings(self, competition_code: str, season: int | None = None, client: httpx.AsyncClient | None = None) -> list[dict[str, Any]]:
        if self.backoff_until and self.backoff_until > datetime.now(UTC):
            return []
        info = self.COMPETITION_MAP.get(competition_code)
        if not info:
            return []

        url = f"https://site.api.espn.com/apis/v2/sports/soccer/{info['slug']}/standings"
        if season:
            url += f"?season={season}"

        should_close = False
        if client is None:
            client = httpx.AsyncClient(timeout=15.0, headers=self.headers)
            should_close = True

        try:
            resp = await client.get(url)
            if resp.status_code in (429, 503):
                self.backoff_until = datetime.now(UTC) + timedelta(minutes=5)
            if resp.status_code != 200:
                return []
            return parse_standings(resp.json(), competition_code, season)
        except Exception as e:
            logger.debug(f"[ESPN Standings] Failed for {competition_code}: {e}")
            return []
        finally:
            if should_close:
                await client.aclose()

    async def get_match_summary(self, event_id: int, competition_code: str, client: httpx.AsyncClient | None = None) -> dict[str, Any]:
        if self.backoff_until and self.backoff_until > datetime.now(UTC):
            return {}
        info = self.COMPETITION_MAP.get(competition_code)
        slug = info["slug"] if info else "eng.1"
        url = f"https://site.api.espn.com/apis/site/v2/sports/soccer/{slug}/summary?event={event_id}"

        should_close = False
        if client is None:
            client = httpx.AsyncClient(timeout=15.0, headers=self.headers)
            should_close = True

        try:
            resp = await client.get(url)
            if resp.status_code in (429, 503):
                self.backoff_until = datetime.now(UTC) + timedelta(minutes=5)
            if resp.status_code != 200:
                return {}
            return parse_match_summary(resp.json())
        except Exception as e:
            logger.debug(f"[ESPN Summary] Failed for {event_id}: {e}")
            return {}
        finally:
            if should_close:
                await client.aclose()


espn_client = ESPNClient()
