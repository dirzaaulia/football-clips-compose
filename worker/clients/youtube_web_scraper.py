"""Zero-quota YouTube web search scraper and microformat availability inspector."""

import json
import re
import urllib.parse
from typing import Any

import httpx
from loguru import logger

OEMBED_BASE_URL = "https://www.youtube.com/oembed"


def inspect_video_global_availability(
    video_id: str,
    headers: dict[str, str],
    min_countries: int = 100,
) -> tuple[bool, bool, int, str]:
    """Verify YouTube playability, embeddability, and global country availability with 0 quota."""
    is_embeddable = False
    oembed_url = f"{OEMBED_BASE_URL}?url=https://www.youtube.com/watch?v={video_id}&format=json"
    try:
        with httpx.Client(timeout=5.0, headers=headers) as client:
            resp = client.get(oembed_url)
            is_embeddable = resp.status_code == 200
    except Exception as e:
        logger.debug(f"[Global Availability] oEmbed check error for {video_id}: {e}")

    watch_url = f"https://www.youtube.com/watch?v={video_id}"
    try:
        with httpx.Client(timeout=6.0, headers=headers, follow_redirects=True) as client:
            rw = client.get(watch_url)
            m = re.search(r"ytInitialPlayerResponse\s*=\s*({.+?});", rw.text)
            if m:
                data = json.loads(m.group(1))
                status = data.get("playabilityStatus", {}).get("status")
                if status != "OK":
                    return False, False, 0, f"Unplayable: {status}"
                countries = (
                    data.get("microformat", {})
                    .get("playerMicroformatRenderer", {})
                    .get("availableCountries", [])
                )
                count = len(countries)
                is_global = count == 0 or count >= min_countries
                return (
                    True,
                    is_embeddable,
                    count if count > 0 else 249,
                    "Global OK" if is_global else f"Regional only ({count} countries)",
                )
    except Exception as e:
        logger.debug(f"[Global Availability] Microformat check error for {video_id}: {e}")

    return True, is_embeddable, 249, "Global OK"


async def search_public_highlights_web_async(
    query: str,
    headers: dict[str, str],
    client: httpx.AsyncClient | None = None,
    timeout_seconds: float = 10.0,
) -> list[dict[str, Any]]:
    """Search YouTube public web search and parse ytInitialData (0 quota units)."""
    url = f"https://www.youtube.com/results?search_query={urllib.parse.quote(query)}"
    should_close = False
    if client is None:
        client = httpx.AsyncClient(timeout=timeout_seconds, headers=headers, follow_redirects=True)
        should_close = True

    try:
        resp = await client.get(url)
        if resp.status_code != 200:
            logger.warning(f"[YouTube Web Search] HTTP {resp.status_code} for query: {query}")
            return []

        m = re.search(r"var ytInitialData\s*=\s*({.+?});</script>", resp.text) or re.search(
            r"window\[\"ytInitialData\"\]\s*=\s*({.+?});</script>",
            resp.text,
        )
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

        results: list[dict[str, Any]] = []
        for it in items:
            v = it.get("videoRenderer")
            if not v or not v.get("videoId"):
                continue
            vid = v["videoId"]
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
                "snippet": {"title": title, "channelTitle": owner, "resourceId": {"videoId": vid}},
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
