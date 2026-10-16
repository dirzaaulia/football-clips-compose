import html
import re
import time
import xml.etree.ElementTree as ET
from typing import Any

import httpx
from loguru import logger


class EditorialRssService:
    """Service to fetch accredited journalistic football articles and previews from public RSS feeds.

    Zero cost ($0.00), zero authentication, multi-source coverage (BBC Sport, The Guardian).
    """

    FEEDS = [
        ("BBC Sport", "https://feeds.bbci.co.uk/sport/football/rss.xml"),
        ("The Guardian", "https://www.theguardian.com/football/rss"),
    ]

    def __init__(self):
        self._cached_articles: list[dict[str, str]] = []
        self._last_fetch_time: float = 0.0
        self._cache_ttl_seconds: float = 900.0  # 15 minutes

    @staticmethod
    def _clean_text(raw: str | None) -> str:
        if not raw:
            return ""
        text = html.unescape(raw)
        text = re.sub(r"<[^>]+>", " ", text)
        return " ".join(text.split()).strip()

    async def get_articles(self, client: httpx.AsyncClient | None = None) -> list[dict[str, str]]:
        now = time.time()
        if self._cached_articles and (now - self._last_fetch_time) < self._cache_ttl_seconds:
            return self._cached_articles

        should_close = False
        if client is None:
            client = httpx.AsyncClient(timeout=12.0, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
            should_close = True

        all_articles = []
        try:
            for source_name, feed_url in self.FEEDS:
                try:
                    resp = await client.get(feed_url)
                    if resp.status_code != 200:
                        continue
                    root = ET.fromstring(resp.text)
                    for item in root.findall(".//item"):
                        title = self._clean_text(item.find("title").text if item.find("title") is not None else "")
                        desc = self._clean_text(item.find("description").text if item.find("description") is not None else "")
                        link = item.find("link").text if item.find("link") is not None else ""
                        if title and len(title) > 10:
                            all_articles.append({
                                "title": title,
                                "summary": desc,
                                "source": source_name,
                                "link": link
                            })
                except Exception as ex:
                    logger.debug(f"[RSS Editorial] Could not fetch feed {source_name}: {ex}")

            if all_articles:
                self._cached_articles = all_articles
                self._last_fetch_time = now
                logger.info(f"[RSS Editorial] Cached {len(all_articles)} accredited journalistic stories from multi-source RSS.")
        finally:
            if should_close:
                await client.aclose()

        return self._cached_articles

    def match_article_for_fixture(
        self,
        home_team: str,
        away_team: str,
        articles: list[dict[str, str]]
    ) -> dict[str, str] | None:
        """Find an article matching both clubs or a high-confidence match analysis/preview."""
        if not home_team or not away_team or not articles:
            return None

        h_clean = home_team.lower().replace("fc", "").replace("afc", "").strip()
        a_clean = away_team.lower().replace("fc", "").replace("afc", "").strip()

        # Pass 1: Both teams mentioned in title or summary (Direct Match Report / Preview)
        for art in articles:
            blob = f"{art['title']} {art['summary']}".lower()
            if h_clean in blob and a_clean in blob:
                return {
                    "headline": art["title"],
                    "story": art["summary"],
                    "source": art["source"]
                }

        return None


editorial_rss_service = EditorialRssService()
