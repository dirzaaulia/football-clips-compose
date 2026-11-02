import json
import os
import urllib.parse
import urllib.request
from datetime import datetime, timedelta, timezone
from typing import Any
import logging

logger = logging.getLogger("gcp_monitoring_client")

CREDENTIALS_CANDIDATE_PATHS = [
    "/home/ubuntu/jarvis/credentials/gcp-highlightdata.json",
    r"C:\Users\ASUS\Downloads\highlightdata-256b3bb6d645.json",
    os.getenv("GOOGLE_APPLICATION_CREDENTIALS", ""),
]


class GcpMonitoringClient:
    """Client for streaming genuine YouTube Data API quota from Google Cloud Monitoring."""

    def __init__(self):
        self.credentials_path = self._resolve_credentials_path()
        self.project_id = "highlightdata"
        self._cached_quota: dict[str, Any] | None = None
        self._last_fetch_ts: float = 0.0

    @staticmethod
    def _resolve_credentials_path() -> str | None:
        for path in CREDENTIALS_CANDIDATE_PATHS:
            if path and os.path.exists(path):
                return path
        return None

    def is_available(self) -> bool:
        return self.credentials_path is not None

    def _get_access_token(self) -> str | None:
        if not self.credentials_path:
            return None
        try:
            from google.auth.transport.requests import Request
            from google.oauth2 import service_account

            creds = service_account.Credentials.from_service_account_file(
                self.credentials_path,
                scopes=[
                    "https://www.googleapis.com/auth/cloud-platform",
                    "https://www.googleapis.com/auth/monitoring.read",
                ],
            )
            creds.refresh(Request())
            return creds.token
        except Exception as e:
            logger.warning(f"[GcpMonitoringClient] Failed to obtain access token: {e}")
            return None

    def fetch_real_quota_status(self) -> dict[str, Any] | None:
        """Fetches genuine YouTube Data API usage and limits directly from Google Cloud Monitoring."""
        now_ts = datetime.now(timezone.utc).timestamp()
        # Cache for 2 minutes to prevent extraneous GCP API calls
        if self._cached_quota and (now_ts - self._last_fetch_ts < 120.0):
            return self._cached_quota

        token = self._get_access_token()
        if not token:
            return None

        try:
            now_utc = datetime.now(timezone.utc)
            start_utc = now_utc - timedelta(hours=24)

            # 1. Fetch quota limits
            limit_val = 60000
            limit_url = (
                f"https://monitoring.googleapis.com/v3/projects/{self.project_id}/timeSeries?"
                + urllib.parse.urlencode({
                    "filter": (
                        'metric.type="serviceruntime.googleapis.com/quota/limit" '
                        'AND resource.labels.service="youtube.googleapis.com" '
                        'AND metric.labels.limit_name="defaultPerDayPerProject"'
                    ),
                    "interval.startTime": start_utc.strftime("%Y-%m-%dT%H:%M:%SZ"),
                    "interval.endTime": now_utc.strftime("%Y-%m-%dT%H:%M:%SZ"),
                })
            )
            req_limit = urllib.request.Request(limit_url, headers={"Authorization": f"Bearer {token}"})
            try:
                with urllib.request.urlopen(req_limit, timeout=10) as resp:
                    data = json.loads(resp.read().decode())
                    ts = data.get("timeSeries", [])
                    if ts and ts[0].get("points"):
                        limit_val = int(ts[0]["points"][0]["value"]["int64Value"])
            except Exception as e:
                logger.debug(f"[GcpMonitoringClient] Limit lookup error: {e}")

            # 2. Fetch usage in last 24h
            usage_url = (
                f"https://monitoring.googleapis.com/v3/projects/{self.project_id}/timeSeries?"
                + urllib.parse.urlencode({
                    "filter": (
                        'metric.type="serviceruntime.googleapis.com/quota/rate/net_usage" '
                        'AND resource.labels.service="youtube.googleapis.com"'
                    ),
                    "interval.startTime": start_utc.strftime("%Y-%m-%dT%H:%M:%SZ"),
                    "interval.endTime": now_utc.strftime("%Y-%m-%dT%H:%M:%SZ"),
                })
            )
            req_usage = urllib.request.Request(usage_url, headers={"Authorization": f"Bearer {token}"})
            total_hits = 0
            with urllib.request.urlopen(req_usage, timeout=10) as resp:
                data = json.loads(resp.read().decode())
                for s in data.get("timeSeries", []):
                    for pt in s.get("points", []):
                        val = pt.get("value", {}).get("int64Value", 0)
                        total_hits += int(val)

            try:
                from zoneinfo import ZoneInfo
                pacific_tz = ZoneInfo("America/Los_Angeles")
                now_pt = datetime.now(pacific_tz)
            except Exception:
                pacific_tz = timezone(timedelta(hours=-7))
                now_pt = datetime.now(pacific_tz)

            today_pt = now_pt.strftime("%Y-%m-%d")
            midnight_pt = (now_pt + timedelta(days=1)).replace(hour=0, minute=0, second=0, microsecond=0)
            hours_until_reset = max(0.0, (midnight_pt - now_pt).total_seconds() / 3600.0)

            remaining = max(0, limit_val - total_hits)
            status = {
                "hits_today": total_hits,
                "limit": limit_val,
                "remaining": remaining,
                "pt_date": today_pt,
                "hours_until_reset": round(hours_until_reset, 1),
                "is_exhausted": remaining <= 0,
                "source": "Google Cloud Monitoring API",
            }
            self._cached_quota = status
            self._last_fetch_ts = now_ts
            return status

        except Exception as e:
            logger.warning(f"[GcpMonitoringClient] Failed to query Cloud Monitoring timeSeries: {e}")
            return None


gcp_monitoring_client = GcpMonitoringClient()
