import threading
import httpx
from loguru import logger


class EdgeCacheClient:
    """Non-blocking background client to purge Cloudflare Edge Cache upon match state changes."""

    PROXY_PURGE_URL = "https://edge.fc.dirzaaulia.com/api/purge-cache"

    @classmethod
    def purge_matches_cache(cls):
        """Dispatches an asynchronous thread-safe cache purge notification to Cloudflare Workers."""
        def _do_purge():
            try:
                with httpx.Client(timeout=3.0) as client:
                    res = client.post(cls.PROXY_PURGE_URL, json={})
                    if res.status_code in [200, 202]:
                        logger.info("[EdgeCache] Cloudflare edge cache purged successfully.")
            except Exception as e:
                logger.debug(f"[EdgeCache] Non-blocking cache purge notice: {e}")

        threading.Thread(target=_do_purge, daemon=True).start()


edge_cache_client = EdgeCacheClient()
