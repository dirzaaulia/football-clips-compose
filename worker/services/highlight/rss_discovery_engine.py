"""Channel RSS, Playlist API, and Search API discovery helpers with parallel tier prefetching."""

import html
from typing import Any

from worker.clients.youtube_client import youtube_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.utils.telemetry_logger import append_event


def build_scanned_summary(raw_items: list[dict[str, Any]] | None, source: str) -> list[dict[str, Any]]:
    summary = []
    for it in raw_items or []:
        vid = (
            it.get("snippet", {}).get("resourceId", {}).get("videoId")
            or (
                it.get("id", {}).get("videoId")
                if isinstance(it.get("id"), dict)
                else it.get("id")
                if isinstance(it.get("id"), str)
                else ""
            )
        )
        if vid:
            summary.append({
                "id": vid,
                "title": html.unescape(it.get("snippet", {}).get("title", "")),
                "publishedAt": it.get("snippet", {}).get("publishedAt"),
                "source": source,
            })
    return summary


def prefetch_tier_rss_feeds(handles: list[str]):
    """Resolves channel IDs for a tier and prefetches their RSS feeds concurrently into the 180s TTL cache."""
    cids = []
    for h in handles:
        cid = youtube_client.resolve_channel_id(h)
        if cid:
            cids.append(cid)
    if len(cids) > 1:
        youtube_rss_client.fetch_channels_batch(cids)


def fetch_channel_uploads(
    handle: str,
    c_id: str,
    scan_limit: int,
    safe_quota: int,
    match_id: Any,
    log_events: list[dict[str, Any]],
) -> tuple[list[dict[str, Any]], str]:
    """Fetches recent uploads from a channel via cached RSS first, falling back to Playlist API."""
    source_mode = "rss"
    raw_items = youtube_rss_client.fetch_channel_recent_videos(c_id)
    scanned_summary = build_scanned_summary(raw_items, "rss")

    if raw_items:
        append_event(
            log_events,
            f"   [RSS DISCOVERY] Scanned {len(raw_items)} uploads from channel {handle} (0 quota units)",
            "INFO",
            "YOUTUBE",
            "RSS_DISCOVERY",
            match_id=match_id,
            attributes={"scanned_videos": scanned_summary, "channel": handle},
        )
        return raw_items, source_mode

    if safe_quota >= 10:
        source_mode = "api_playlist"
        raw_items = youtube_client.get_playlist_recent_videos(c_id, max_results=scan_limit) or []
        scanned_summary = build_scanned_summary(raw_items, "api_playlist")
        if raw_items:
            append_event(
                log_events,
                f"   [API PLAYLIST] Fetched {len(raw_items)} uploads from {handle} via YouTube Data API (Cost: 1 quota)",
                "INFO",
                "YOUTUBE",
                "API_CHANNEL_FALLBACK",
                match_id=match_id,
                attributes={"scanned_videos": scanned_summary, "channel": handle},
            )
            return raw_items, source_mode

    append_event(
        log_events,
        f"   [CHANNEL EMPTY] 0 public uploads available on channel {handle}",
        "INFO",
        "YOUTUBE",
        "CHANNEL_EMPTY",
        match_id=match_id,
        attributes={"scanned_videos": [], "channel": handle},
    )
    return [], source_mode


def run_api_search_fallback(
    query: str,
    handle: str,
    c_id: str,
    pub_after: str,
    pub_before: str,
    match_id: Any,
    log_events: list[dict[str, Any]],
) -> list[dict[str, Any]]:
    """Executes targeted YouTube Data API search fallback when RSS discovery yields 0 matches."""
    api_search_items = youtube_client.search_videos(query, c_id, pub_after, pub_before) or []
    api_scanned_summary = [
        {
            "id": it.get("id", {}).get("videoId"),
            "title": html.unescape(it.get("snippet", {}).get("title", "")),
            "source": "api",
            "publishedAt": it.get("snippet", {}).get("publishedAt"),
        }
        for it in api_search_items
        if it.get("id", {}).get("videoId")
    ]
    append_event(
        log_events,
        f"   [API SEARCH] Querying YouTube Data API: '{query}' for channel {handle} (Cost: 100 quota)",
        "INFO",
        "YOUTUBE",
        "API_GENERAL_SEARCH",
        match_id=match_id,
        attributes={"channel": f"{handle} [API]", "query": query, "scanned_videos": api_scanned_summary},
    )
    matched: list[dict[str, Any]] = []
    if api_search_items:
        append_event(
            log_events,
            f"   [API RESULT] Discovered {len(api_search_items)} search results from {handle}",
            "INFO",
            "YOUTUBE",
            "API_GENERAL_SEARCH",
            match_id=match_id,
            attributes={"channel": f"{handle} [API]", "query": query, "scanned_videos": api_scanned_summary},
        )
        for it in api_search_items:
            it["source"] = "api_search"
            matched.append(it)
            s_vid = it.get("id", {}).get("videoId")
            s_title = html.unescape(it.get("snippet", {}).get("title", ""))
            if s_vid:
                append_event(
                    log_events,
                    f"   [API RESULT] Discovered from {handle} [API]: '{s_title}' -> https://youtu.be/{s_vid}",
                    "INFO",
                    "YOUTUBE",
                    "CANDIDATE_ITEM",
                    match_id=match_id,
                    attributes={
                        "candidate_id": s_vid,
                        "title": s_title,
                        "channel": handle,
                        "source": "API",
                        "url": f"https://youtu.be/{s_vid}",
                    },
                )
    return matched
