from datetime import datetime
from typing import Any

from loguru import logger


class ESPNCalendar:
    def __init__(self):
        self._calendar_cache: dict[str, list[tuple[datetime.date, datetime.date]]] = {}

    def cluster_calendar_matchweeks(self, calendar_dates: list[str]) -> list[tuple[datetime.date, datetime.date]]:
        """Cluster ESPN calendar dates into sequential Matchweeks (gaps <= 2 days)."""
        if not calendar_dates:
            return []
        try:
            parsed = sorted([
                datetime.fromisoformat(d.replace("Z", "+00:00")).date()
                for d in calendar_dates
            ])
            weeks: list[tuple[datetime.date, datetime.date]] = []
            current_start = parsed[0]
            current_end = parsed[0]

            for d in parsed[1:]:
                if (d - current_end).days <= 2:
                    current_end = d
                else:
                    weeks.append((current_start, current_end))
                    current_start = d
                    current_end = d
            weeks.append((current_start, current_end))
            return weeks
        except Exception as e:
            logger.warning(f"[ESPN] Failed to cluster calendar dates: {e}")
            return []

    def determine_matchweek(self, league_slug: str, match_date: datetime.date, calendar_dates: list[str]) -> int | None:
        """Find the 1-indexed matchweek for a given date in a league season."""
        if league_slug not in self._calendar_cache and calendar_dates:
            self._calendar_cache[league_slug] = self.cluster_calendar_matchweeks(calendar_dates)

        clusters = self._calendar_cache.get(league_slug, [])
        for i, (start, end) in enumerate(clusters, 1):
            if start <= match_date <= end:
                return i
            if abs((match_date - start).days) <= 1 or abs((match_date - end).days) <= 1:
                return i
        return None


espn_calendar = ESPNCalendar()
