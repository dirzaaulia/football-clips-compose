"""Zero-quota YouTube video discovery client using official RSS Atom feeds.

Includes a persistent HTTP connection pool, 180s in-memory RSS feed TTL cache,
and concurrent batch channel fetching for 5x-10x faster sweeps at $0 cost.
"""

from concurrent.futures import ThreadPoolExecutor
import html
import time
import xml.etree.ElementTree as ET
from typing import Any

import httpx
from loguru import logger

from worker.clients.youtube_web_scraper import (
    inspect_video_global_availability,
    search_public_highlights_web_async,
)


class YouTubeRSSClient:
    """Zero-quota YouTube video discovery client using official RSS Atom feeds."""

    RSS_BASE_URL = "https://www.youtube.com/feeds/videos.xml"
    OEMBED_BASE_URL = "https://www.youtube.com/oembed"
    RSS_CACHE_TTL_SECONDS = 180.0

    ATOM_NS = {
        "atom": "http://www.w3.org/2005/Atom",
        "yt": "http://www.youtube.com/xml/schemas/2015",
        "media": "http://search.yahoo.com/mrss/",
    }

    DEAD_CHANNEL_TTL_SECONDS = 7 * 86400.0

    def __init__(self):
        self.headers = {"Accept": "*/*"}
        self._rss_cache: dict[str, tuple[float, list[dict[str, Any]]]] = {}
        self._dead_channels: dict[str, float] = {}
        self._http_client = httpx.Client(
            timeout=10.0,
            headers=self.headers,
            follow_redirects=True,
            limits=httpx.Limits(max_keepalive_connections=15, max_connections=30),
        )

    def is_channel_dead(self, channel_id: str) -> bool:
        """Returns True if the channel recently returned HTTP 404 or 410."""
        if not channel_id:
            return False
        now = time.time()
        expiry = self._dead_channels.get(channel_id)
        if expiry and now < expiry:
            return True
        if expiry:
            del self._dead_channels[channel_id]
        return False

    def clear_cache(self):
        self._rss_cache.clear()

    def fetch_channel_recent_videos(
        self,
        channel_id: str,
        timeout_seconds: float = 10.0,
        use_cache: bool = True,
    ) -> list[dict[str, Any]]:
        """Fetch the 15 most recent videos from a channel via RSS with 180s TTL caching."""
        if not channel_id or self.is_channel_dead(channel_id):
            return []

        now = time.time()
        if use_cache and channel_id in self._rss_cache:
            ts, cached_items = self._rss_cache[channel_id]
            if (now - ts) < self.RSS_CACHE_TTL_SECONDS:
                return list(cached_items)

        url = f"{self.RSS_BASE_URL}?channel_id={channel_id}"
        try:
            resp = self._http_client.get(url, timeout=timeout_seconds)
            if resp.status_code in (404, 410):
                self._dead_channels[channel_id] = now + self.DEAD_CHANNEL_TTL_SECONDS
                logger.warning(f"[YouTube RSS] Channel {channel_id} returned HTTP {resp.status_code}. Blacklisting for 7 days.")
                return []
            if resp.status_code != 200:
                logger.warning(f"[YouTube RSS] Failed to fetch feed for {channel_id}: HTTP {resp.status_code}")
                return []

            parsed = self._parse_feed_xml(resp.content, channel_id)
            if parsed:
                self._rss_cache[channel_id] = (now, parsed)
            return list(parsed)
        except Exception as e:
            logger.error(f"[YouTube RSS] Error fetching RSS feed for {channel_id}: {e}")
            return []

    def fetch_channels_batch(self, channel_ids: list[str], max_workers: int = 5) -> dict[str, list[dict[str, Any]]]:
        """Concurrently prefetches multiple channel RSS feeds into the 180s TTL cache."""
        valid_ids = [cid for cid in dict.fromkeys(channel_ids) if cid]
        if not valid_ids:
            return {}
        results: dict[str, list[dict[str, Any]]] = {}
        with ThreadPoolExecutor(max_workers=min(max_workers, len(valid_ids))) as pool:
            future_map = {pool.submit(self.fetch_channel_recent_videos, cid): cid for cid in valid_ids}
            for fut, cid in future_map.items():
                try:
                    results[cid] = fut.result()
                except Exception:
                    results[cid] = []
        return results

    async def fetch_channel_recent_videos_async(
        self,
        channel_id: str,
        client: httpx.AsyncClient | None = None,
    ) -> list[dict[str, Any]]:
        """Asynchronously fetch the 15 most recent videos from a channel via RSS."""
        if not channel_id:
            return []

        now = time.time()
        if channel_id in self._rss_cache:
            ts, cached_items = self._rss_cache[channel_id]
            if (now - ts) < self.RSS_CACHE_TTL_SECONDS:
                return list(cached_items)

        url = f"{self.RSS_BASE_URL}?channel_id={channel_id}"
        try:
            if client:
                resp = await client.get(url, headers=self.headers)
            else:
                async with httpx.AsyncClient(timeout=10.0, headers=self.headers, follow_redirects=True) as local_client:
                    resp = await local_client.get(url)

            if resp.status_code != 200:
                logger.warning(f"[YouTube RSS] Async fetch failed for {channel_id}: HTTP {resp.status_code}")
                return []

            parsed = self._parse_feed_xml(resp.content, channel_id)
            if parsed:
                self._rss_cache[channel_id] = (now, parsed)
            return list(parsed)
        except Exception as e:
            logger.error(f"[YouTube RSS] Async error for {channel_id}: {e}")
            return []

    def _parse_feed_xml(self, xml_bytes: bytes, fallback_channel_id: str) -> list[dict[str, Any]]:
        try:
            root = ET.fromstring(xml_bytes)
            entries = root.findall("atom:entry", self.ATOM_NS)
            results = []
            for entry in entries:
                video_id_el = entry.find("yt:videoId", self.ATOM_NS)
                title_el = entry.find("atom:title", self.ATOM_NS)
                published_el = entry.find("atom:published", self.ATOM_NS)
                author_name_el = entry.find("atom:author/atom:name", self.ATOM_NS)
                channel_id_el = entry.find("yt:channelId", self.ATOM_NS)
                desc_el = entry.find("media:group/media:description", self.ATOM_NS)

                if video_id_el is None or not video_id_el.text:
                    continue

                vid = video_id_el.text.strip()
                title = html.unescape(title_el.text) if title_el is not None and title_el.text else ""
                published_at = published_el.text.strip() if published_el is not None and published_el.text else ""
                channel_name = author_name_el.text.strip() if author_name_el is not None and author_name_el.text else ""
                c_id = channel_id_el.text.strip() if channel_id_el is not None and channel_id_el.text else fallback_channel_id
                description = html.unescape(desc_el.text).strip()[:300] if desc_el is not None and desc_el.text else ""

                results.append({
                    "id": {"videoId": vid},
                    "snippet": {
                        "title": title,
                        "description": description,
                        "publishedAt": published_at,
                        "channelId": c_id,
                        "channelTitle": channel_name,
                        "resourceId": {"videoId": vid},
                    },
                    "source": "rss",
                })
            return results
        except Exception as e:
            logger.error(f"[YouTube RSS] Failed to parse XML: {e}")
            return []

    def check_oembed_embeddable(self, video_id: str) -> bool:
        url = f"{self.OEMBED_BASE_URL}?url=https://www.youtube.com/watch?v={video_id}&format=json"
        try:
            resp = self._http_client.get(url, timeout=5.0)
            return resp.status_code == 200
        except Exception:
            return True

    def check_video_global_availability(self, video_id: str, min_countries: int = 100) -> tuple[bool, bool, int, str]:
        return inspect_video_global_availability(video_id, self.headers, min_countries)

    async def search_public_highlights_async(
        self,
        query: str,
        client: httpx.AsyncClient | None = None,
        timeout_seconds: float = 10.0,
    ) -> list[dict[str, Any]]:
        return await search_public_highlights_web_async(query, self.headers, client, timeout_seconds)


youtube_rss_client = YouTubeRSSClient()
