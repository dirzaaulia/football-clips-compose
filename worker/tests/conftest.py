"""Global pytest fixtures preventing unit tests from mutating production telemetry_state."""

from unittest.mock import patch
import pytest


@pytest.fixture(autouse=True)
def prevent_production_telemetry_writes(request):
    """Prevents unit tests from writing phantom test events (e.g. match_id=5006) into production telemetry_state."""
    # Allow explicit telemetry persistence unit tests to mock/test telemetry_service directly if needed
    if "test_telemetry" in request.node.nodeid:
        yield
        return

    with patch("worker.services.websub_processor._persist_websub_events", return_value=None):
        yield
