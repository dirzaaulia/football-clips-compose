import pytest
from unittest.mock import MagicMock, patch
from worker.clients.cloud_tasks_client import CloudTasksClient


def test_cloud_tasks_client_initialization():
    client = CloudTasksClient()
    assert client.queue_id == "fc-highlight-queue"
    assert client.location == "us-central1"
    assert client.project_id == "highlightdata"


def test_dispatch_highlight_task_offline_fallback():
    client = CloudTasksClient()
    with patch.object(CloudTasksClient, "client", new=None):
        # Should gracefully return False without raising an unhandled exception
        result = client.dispatch_highlight_task(
            match_id=12345,
            home="Arsenal",
            away="Chelsea",
            competition="Premier League",
        )
        assert result is False


def test_dispatch_highlight_task_success():
    client = CloudTasksClient()
    mock_sdk_client = MagicMock()
    mock_sdk_client.queue_path.return_value = "projects/highlightdata/locations/us-central1/queues/fc-highlight-queue"
    mock_sdk_client.task_path.return_value = "projects/highlightdata/locations/us-central1/queues/fc-highlight-queue/tasks/match-12345"
    mock_sdk_client.create_task.return_value = MagicMock(name="task-object-12345")

    with patch.object(CloudTasksClient, "client", new=mock_sdk_client):
        result = client.dispatch_highlight_task(
            match_id=12345,
            home="Arsenal",
            away="Chelsea",
            competition="Premier League",
        )
        assert result is True
        mock_sdk_client.create_task.assert_called_once()
        call_args = mock_sdk_client.create_task.call_args[1]["request"]
        assert call_args["parent"] == "projects/highlightdata/locations/us-central1/queues/fc-highlight-queue"
        task_dict = call_args["task"]
        assert "http_request" in task_dict
        assert task_dict["http_request"]["url"].endswith("/tasks/process-highlight")
