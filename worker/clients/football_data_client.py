import asyncio
import time

import httpx
from loguru import logger
from worker.config.settings import settings


class FootballDataClient:
    BASE_URL = "https://api.football-data.org/v4"

    def __init__(self):
        self.headers = {"X-Auth-Token": settings.football_data_api_key}
        self.last_call_timestamp: float = 0.0
        self.min_interval_seconds: float = 6.0  # Safe guard for 10 req/minute free tier

    async def _throttle(self):
        elapsed = time.time() - self.last_call_timestamp
        if elapsed < self.min_interval_seconds:
            wait_time = self.min_interval_seconds - elapsed
            logger.debug(f"[FootballData] Rate limit throttle: sleeping {wait_time:.2f}s")
            await asyncio.sleep(wait_time)
        self.last_call_timestamp = time.time()

    async def get_matches(self, competitions: list[str], date_from: str, date_to: str) -> list[dict]:
        from datetime import datetime, timedelta
        try:
            d_start = datetime.strptime(date_from, "%Y-%m-%d")
            d_end = datetime.strptime(date_to, "%Y-%m-%d")
        except Exception:
            d_start = datetime.now()
            d_end = d_start

        all_matches = []
        curr_start = d_start
        while curr_start <= d_end:
            curr_end = min(curr_start + timedelta(days=9), d_end)
            await self._throttle()
            url = f"{self.BASE_URL}/matches"
            params = {
                "competitions": ",".join(competitions),
                "dateFrom": curr_start.strftime("%Y-%m-%d"),
                "dateTo": curr_end.strftime("%Y-%m-%d")
            }
            async with httpx.AsyncClient(timeout=15.0) as client:
                try:
                    response = await client.get(url, headers=self.headers, params=params)
                    if response.status_code == 200:
                        data = response.json()
                        matches = data.get("matches", [])
                        all_matches.extend(matches)
                    elif response.status_code == 429:
                        logger.warning("[FootballData] 🚨 429 Rate Limit encountered! Sleeping 60s...")
                        await asyncio.sleep(60.0)
                    else:
                        logger.error(f"[FootballData] Gagal fetch matches ({params['dateFrom']} - {params['dateTo']}): HTTP {response.status_code} - {response.text[:100]}")
                except Exception as e:
                    logger.error(f"[FootballData] Exception saat get_matches: {e}")
            curr_start = curr_end + timedelta(days=1)

        # Deduplicate matches by id
        return list({m['id']: m for m in all_matches}.values())

    async def get_competition_teams(self, competition_code: str) -> list[dict]:
        await self._throttle()
        url = f"{self.BASE_URL}/competitions/{competition_code}/teams"
        async with httpx.AsyncClient(timeout=15.0) as client:
            try:
                response = await client.get(url, headers=self.headers)
                if response.status_code == 200:
                    return response.json().get("teams", [])
                else:
                    logger.warning(f"[FootballData] Gagal fetch teams for {competition_code}: {response.status_code}")
                    return []
            except Exception as e:
                logger.error(f"[FootballData] Exception get_competition_teams: {e}")
                return []

    async def get_standings(self, competition_code: str) -> dict:
        await self._throttle()
        url = f"{self.BASE_URL}/competitions/{competition_code}/standings"
        async with httpx.AsyncClient(timeout=15.0) as client:
            try:
                response = await client.get(url, headers=self.headers)
                if response.status_code == 200:
                    return response.json()
                elif response.status_code == 429:
                    logger.warning(f"[FootballData] 🚨 429 Rate Limit on standings {competition_code}! Sleeping 60s...")
                    await asyncio.sleep(60.0)
                    return {}
                else:
                    logger.warning(f"[FootballData] Gagal fetch standings for {competition_code}: {response.status_code}")
                    return {}
            except Exception as e:
                logger.error(f"[FootballData] Exception get_standings: {e}")
                return {}

football_data_client = FootballDataClient()
