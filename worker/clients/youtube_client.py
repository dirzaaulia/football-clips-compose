import json
import os
import re
from datetime import datetime

import httpx
from googleapiclient.discovery import build
from loguru import logger
from worker.config.constants import PERMANENT_CHANNEL_IDS
from worker.config.settings import settings


class YouTubeClient:
    QUOTA_FILE = "youtube_quota.json"
    DISK_CHANNEL_CACHE_FILE = "channel_cache.json"

    def __init__(self):
        self.api_key = settings.youtube_api_key
        self.youtube = build("youtube", "v3", developerKey=self.api_key) if self.api_key else None
        self.disk_channel_cache: dict[str, str] = self._load_disk_channel_cache()

    def _load_disk_channel_cache(self) -> dict[str, str]:
        if os.path.exists(self.DISK_CHANNEL_CACHE_FILE):
            try:
                with open(self.DISK_CHANNEL_CACHE_FILE) as f:
                    return json.load(f)
            except Exception:
                return {}
        return {}

    def _save_disk_channel_cache(self):
        try:
            with open(self.DISK_CHANNEL_CACHE_FILE, "w") as f:
                json.dump(self.disk_channel_cache, f)
        except Exception as e:
            logger.warning(f"Gagal save disk channel cache: {e}")

    def get_remaining_quota(self) -> int:
        hits = self._check_and_reset_quota()
        return max(0, settings.max_quota_per_day - hits)

    def _check_and_reset_quota(self) -> int:
        now_pt = datetime.now(settings.pacific_tz)
        today_pt = now_pt.strftime("%Y-%m-%d")
        quota_data = {"pt_date": today_pt, "hits": 0}

        if os.path.exists(self.QUOTA_FILE):
            try:
                with open(self.QUOTA_FILE) as f:
                    saved = json.load(f)
                    if saved.get("pt_date") == today_pt:
                        return saved.get("hits", 0)
            except Exception:
                pass

        with open(self.QUOTA_FILE, "w") as f:
            json.dump(quota_data, f)
        return 0

    def increment_quota(self, cost: int = 1):
        now_pt = datetime.now(settings.pacific_tz)
        today_pt = now_pt.strftime("%Y-%m-%d")
        hits = self._check_and_reset_quota() + cost
        with open(self.QUOTA_FILE, "w") as f:
            json.dump({"pt_date": today_pt, "hits": hits}, f)

    def resolve_channel_id(self, handle: str) -> str | None:
        if handle in PERMANENT_CHANNEL_IDS:
            return PERMANENT_CHANNEL_IDS[handle]
        if handle in self.disk_channel_cache:
            return self.disk_channel_cache[handle]

        url = "https://www.googleapis.com/youtube/v3/channels"
        params = {"part": "id", "forHandle": handle, "key": self.api_key}
        try:
            with httpx.Client(timeout=10.0) as client:
                res = client.get(url, params=params)
                if res.status_code == 200 and res.json().get("items"):
                    channel_id = res.json()["items"][0]["id"]
                    self.disk_channel_cache[handle] = channel_id
                    self._save_disk_channel_cache()
                    self.increment_quota(1)
                    return channel_id
        except Exception as e:
            logger.warning(f"Gagal resolve handle {handle}: {e}")
        return None

    def get_playlist_recent_videos(self, channel_id: str, max_results: int = 30) -> list[dict]:
        if not channel_id or not channel_id.startswith("UC"):
            return []
        uploads_playlist_id = "UU" + channel_id[2:]
        try:
            req = self.youtube.playlistItems().list(
                playlistId=uploads_playlist_id,
                part="snippet",
                maxResults=max_results
            )
            res = req.execute()
            self.increment_quota(1)
            return res.get("items", [])
        except Exception as e:
            logger.debug(f"Playlist scan error for {uploads_playlist_id}: {e}")
            return []

    def search_videos(self, query: str, channel_id: str, pub_after: str, pub_before: str) -> list[dict]:
        try:
            req = self.youtube.search().list(
                q=query,
                channelId=channel_id,
                part="snippet",
                type="video",
                publishedAfter=pub_after,
                publishedBefore=pub_before,
                order="relevance",
                maxResults=5
            )
            res = req.execute()
            self.increment_quota(100)
            return res.get("items", [])
        except Exception as e:
            logger.error(f"Search API error for '{query}': {e}")
            return []

    def get_video_details(self, video_ids: list[str]) -> tuple[dict[str, int], dict[str, dict], dict[str, bool]]:
        if not video_ids:
            return {}, {}, {}
        try:
            dur_req = self.youtube.videos().list(part="contentDetails,status", id=",".join(video_ids))
            dur_res = dur_req.execute()
            self.increment_quota(1)
            durations, regions, embeddables = {}, {}, {}
            for item in dur_res.get("items", []):
                vid = item["id"]
                dur_sec = self._parse_iso_duration(item.get("contentDetails", {}).get("duration", ""))
                durations[vid] = dur_sec
                regions[vid] = item.get("contentDetails", {}).get("regionRestriction")
                embeddables[vid] = item.get("status", {}).get("embeddable", True)
            return durations, regions, embeddables
        except Exception as e:
            logger.error(f"Gagal get_video_details: {e}")
            return {}, {}, {}

    @staticmethod
    def _parse_iso_duration(duration_str: str) -> int:
        match = re.match(r'PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?', duration_str or "")
        if not match:
            return 0
        h = int(match.group(1) or 0)
        m = int(match.group(2) or 0)
        s = int(match.group(3) or 0)
        return h * 3600 + m * 60 + s

youtube_client = YouTubeClient()
