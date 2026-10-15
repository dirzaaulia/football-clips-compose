import html
import re
import urllib.parse
import xml.etree.ElementTree as ET
from datetime import UTC, datetime
from typing import Any

import httpx
from loguru import logger


class YouTubeRSSClient:
    """Zero-quota YouTube video discovery client using official RSS Atom feeds.

    Consumes 0 YouTube Data API quota units.
    Fetches the 15 most recent uploads from any YouTube channel instantly.
    """

    RSS_BASE_URL = "https://www.youtube.com/feeds/videos.xml"
    OEMBED_BASE_URL = "https://www.youtube.com/oembed"

    ATOM_NS = {
        "atom": "http://www.w3.org/2005/Atom",
        "yt": "http://www.youtube.com/xml/schemas/2015",
        "media": "http://search.yahoo.com/mrss/",
    }

    def __init__(self):
        self.headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept": "application/atom+xml,application/xml,text/xml",
        }

    def fetch_channel_recent_videos(self, channel_id: str, timeout_seconds: float = 10.0) -> list[dict[str, Any]]:
        """Fetch the 15 most recent videos from a channel via RSS.

        Returns items formatted identically to YouTube Data API playlistItems,
        allowing seamless drop-in replacement with 0 quota cost.
        """
        if not channel_id:
            return []

        url = f"{self.RSS_BASE_URL}?channel_id={channel_id}"
        try:
            with httpx.Client(timeout=timeout_seconds, headers=self.headers, follow_redirects=True) as client:
                resp = client.get(url)
                if resp.status_code != 200:
                    logger.warning(f"[YouTube RSS] Failed to fetch feed for {channel_id}: HTTP {resp.status_code}")
                    return []

                return self._parse_feed_xml(resp.content, channel_id)
        except Exception as e:
            logger.error(f"[YouTube RSS] Error fetching RSS feed for {channel_id}: {e}")
            return []

    async def fetch_channel_recent_videos_async(
        self,
        channel_id: str,
        client: httpx.AsyncClient | None = None,
    ) -> list[dict[str, Any]]:
        """Asynchronously fetch the 15 most recent videos from a channel via RSS."""
        if not channel_id:
            return []

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

            return self._parse_feed_xml(resp.content, channel_id)
        except Exception as e:
            logger.error(f"[YouTube RSS] Async error for {channel_id}: {e}")
            return []

    def _parse_feed_xml(self, xml_bytes: bytes, fallback_channel_id: str) -> list[dict[str, Any]]:
        """Parse Atom XML feed into standard YouTube Data API candidate structures."""
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

                if video_id_el is None or not video_id_el.text:
                    continue

                vid = video_id_el.text.strip()
                title = html.unescape(title_el.text) if title_el is not None and title_el.text else ""
                published_at = published_el.text.strip() if published_el is not None and published_el.text else ""
                channel_name = author_name_el.text.strip() if author_name_el is not None and author_name_el.text else ""
                c_id = channel_id_el.text.strip() if channel_id_el is not None and channel_id_el.text else fallback_channel_id

                # Match format of playlistItems.list snippet for 100% pipeline compatibility
                results.append({
                    "id": {"videoId": vid},
                    "snippet": {
                        "title": title,
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
        """Check if video is publicly embeddable via YouTube oEmbed API (0 quota units)."""
        url = f"{self.OEMBED_BASE_URL}?url=https://www.youtube.com/watch?v={video_id}&format=json"
        try:
            with httpx.Client(timeout=5.0, headers=self.headers) as client:
                resp = client.get(url)
                # 200 = Publicly embeddable, 401/403/404 = Not embeddable or restricted
                return resp.status_code == 200
        except Exception:
            return True

    def check_video_global_availability(self, video_id: str, min_countries: int = 100) -> tuple[bool, bool, int, str]:
        """
        Verify YouTube playability, embeddability, and global country availability with 0 YouTube Data API quota.
        Does NOT block non-embeddable videos so that the app's 'Watch on YouTube' fallback works seamlessly.
        Returns: (is_playable, is_embeddable, allowed_country_count, status_message)
        - is_playable: True if video is public & playable on YouTube (status == 'OK')
        - is_embeddable: True if 3rd-party website iframe embedding is enabled (oEmbed HTTP 200)
        - allowed_country_count: number of allowed countries (249 if worldwide)
        """
        import json

        # 1. oEmbed check (identifies if iframe embed is allowed or requires YouTube app fallback)
        is_embeddable = False
        oembed_url = f"{self.OEMBED_BASE_URL}?url=https://www.youtube.com/watch?v={video_id}&format=json"
        try:
            with httpx.Client(timeout=5.0, headers=self.headers) as client:
                resp = client.get(oembed_url)
                is_embeddable = (resp.status_code == 200)
        except Exception as e:
            logger.debug(f"[Global Availability] oEmbed check error for {video_id}: {e}")

        # 2. Player microformat playability & geoblock inspection
        watch_url = f"https://www.youtube.com/watch?v={video_id}"
        try:
            with httpx.Client(timeout=6.0, headers=self.headers, follow_redirects=True) as client:
                rw = client.get(watch_url)
                m = re.search(r"ytInitialPlayerResponse\s*=\s*({.+?});", rw.text)
                if m:
                    data = json.loads(m.group(1))
                    status = data.get("playabilityStatus", {}).get("status")
                    if status != "OK":
                        return False, False, 0, f"Unplayable: {status}"
                    countries = data.get("microformat", {}).get("playerMicroformatRenderer", {}).get("availableCountries", [])
                    count = len(countries)
                    is_global = (count == 0 or count >= min_countries)
                    return True, is_embeddable, count if count > 0 else 249, "Global OK" if is_global else f"Regional only ({count} countries)"
        except Exception as e:
            logger.debug(f"[Global Availability] Microformat check error for {video_id}: {e}")

        return True, is_embeddable, 249, "Global OK"

    async def search_public_highlights_async(
        self,
        query: str,
        client: httpx.AsyncClient | None = None,
        timeout_seconds: float = 10.0,
    ) -> list[dict[str, Any]]:
        """
        Search YouTube public web search and parse ytInitialData.
        Returns candidate highlight videos with 0 YouTube Data API quota units and 0 API keys.
        Ideal for historical season backfill and quota-free highlight recovery.
        """
        import json
        import urllib.parse

        url = f"https://www.youtube.com/results?search_query={urllib.parse.quote(query)}"
        should_close = False
        if client is None:
            client = httpx.AsyncClient(timeout=timeout_seconds, headers=self.headers, follow_redirects=True)
            should_close = True

        try:
            resp = await client.get(url)
            if resp.status_code != 200:
                logger.warning(f"[YouTube Web Search] HTTP {resp.status_code} for query: {query}")
                return []

            m = re.search(r"var ytInitialData\s*=\s*({.+?});</script>", resp.text)
            if not m:
                m = re.search(r"window\[\"ytInitialData\"\]\s*=\s*({.+?});</script>", resp.text)
                if not m:
                    return []

            data = json.loads(m.group(1))
            contents = (
                data.get("contents", {})
                .get("twoColumnSearchResultsRenderer", {})
                .get("primaryContents", {})
                .get("sectionListRenderer", {})
                .get("contents", [])
            )
            items = contents[0].get("itemSectionRenderer", {}).get("contents", []) if contents else []

            results = []
            for it in items:
                v = it.get("videoRenderer")
                if not v:
                    continue
                vid = v.get("videoId")
                if not vid:
                    continue

                title = v.get("title", {}).get("runs", [{}])[0].get("text", "")
                owner = v.get("ownerText", {}).get("runs", [{}])[0].get("text", "")
                len_text = v.get("lengthText", {}).get("simpleText", "0:00")
                parts = len_text.split(":")
                dur = 0
                if len(parts) == 2:
                    dur = int(parts[0]) * 60 + int(parts[1])
                elif len(parts) == 3:
                    dur = int(parts[0]) * 3600 + int(parts[1]) * 60 + int(parts[2])

                results.append({
                    "id": {"videoId": vid},
                    "snippet": {
                        "title": title,
                        "channelTitle": owner,
                        "resourceId": {"videoId": vid},
                    },
                    "duration": dur,
                    "channel": owner,
                    "source": "web_scrape_zero_quota",
                })

            return results
        except Exception as e:
            logger.error(f"[YouTube Web Search] Failed to search for '{query}': {e}")
            return []
        finally:
            if should_close:
                await client.aclose()


youtube_rss_client = YouTubeRSSClient()

