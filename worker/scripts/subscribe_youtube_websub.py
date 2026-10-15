import asyncio
import os
import sys
import urllib.parse
from typing import Any

import httpx
from loguru import logger

# Add root directory to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "../..")))

from worker.clients.youtube_client import youtube_client
from worker.config.constants import EUROPEAN_CLUB_HANDLES, OFFICIAL_LEAGUE_CHANNELS


class WebSubSubscriber:
    """Subscribes all official football club & league channels to Google WebSub Hub."""

    GOOGLE_HUB_URL = "https://pubsubhubbub.appspot.com/subscribe"
    DEFAULT_CALLBACK = "https://fc-worker-519310318914.us-central1.run.app/websub"
    LEASE_SECONDS = 864000  # 10 days

    def __init__(self, callback_url: str = DEFAULT_CALLBACK):
        self.callback_url = callback_url

    async def subscribe_channel(self, client: httpx.AsyncClient, channel_id: str, label: str) -> bool:
        topic_url = f"https://www.youtube.com/xml/feeds/videos.xml?channel_id={channel_id}"
        payload = {
            "hub.callback": self.callback_url,
            "hub.mode": "subscribe",
            "hub.topic": topic_url,
            "hub.lease_seconds": str(self.LEASE_SECONDS),
        }
        try:
            resp = await client.post(
                self.GOOGLE_HUB_URL,
                data=payload,
                headers={"Content-Type": "application/x-www-form-urlencoded"},
                timeout=10.0,
            )
            if resp.status_code in [202, 204]:
                logger.info(f"Subscribed {label} ({channel_id}) -> HTTP {resp.status_code}")
                return True
            else:
                logger.warning(f"Failed {label} ({channel_id}) -> HTTP {resp.status_code}: {resp.text[:60]}")
                return False
        except Exception as e:
            logger.error(f"Error subscribing {label} ({channel_id}): {e}")
            return False

    async def subscribe_all(self):
        logger.info(f"Starting batch WebSub subscription to callback: {self.callback_url}")

        channel_map: dict[str, str] = {}

        # 1. League channels
        for comp, handle in OFFICIAL_LEAGUE_CHANNELS.items():
            c_id = youtube_client.resolve_channel_id(handle)
            if c_id and c_id not in channel_map:
                channel_map[c_id] = f"League: {handle}"

        # 2. Club channels
        for club, handle in EUROPEAN_CLUB_HANDLES.items():
            c_id = youtube_client.resolve_channel_id(handle)
            if c_id and c_id not in channel_map:
                channel_map[c_id] = f"Club: {club} ({handle})"

        logger.info(f"Found {len(channel_map)} unique YouTube channels to subscribe.")

        success_count = 0
        async with httpx.AsyncClient() as client:
            tasks = [
                self.subscribe_channel(client, c_id, label)
                for c_id, label in channel_map.items()
            ]
            results = await asyncio.gather(*tasks, return_exceptions=True)
            for r in results:
                if r is True:
                    success_count += 1

        logger.info(f"Batch subscription complete: {success_count}/{len(channel_map)} channels successfully subscribed!")


websub_subscriber = WebSubSubscriber()


if __name__ == "__main__":
    asyncio.run(websub_subscriber.subscribe_all())
