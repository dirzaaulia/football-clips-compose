import time
from unittest.mock import MagicMock
from worker.clients.gemini_client import GeminiClient


def test_gemini_token_bucket_allows_under_limit():
    client = GeminiClient()
    client._request_timestamps.clear()

    # 12 requests should all succeed
    for i in range(12):
        assert client._acquire_token() is True

    # 13th request within same minute should be rejected
    assert client._acquire_token() is False


def test_gemini_token_bucket_resets_after_window():
    client = GeminiClient()
    client._request_timestamps.clear()

    # Simulate 12 requests 61 seconds ago
    old_time = time.monotonic() - 65.0
    for _ in range(12):
        client._request_timestamps.append(old_time)

    # Now a new request should be permitted as old ones expire
    assert client._acquire_token() is True
