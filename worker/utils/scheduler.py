from datetime import UTC, datetime, timedelta
from typing import Any

from worker.clients.database_client import db_client
from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings
from worker.services.match_service import match_service


class CycleScheduler:
    @staticmethod
    async def calculate_highlight_dynamic_sleep() -> tuple[int, str]:
        # 1. Check daily quota status
        quota_status = youtube_client.get_quota_status()
        hits_today = quota_status.get("hits_today", 0)
        remaining = quota_status.get("remaining", 0)
        target = getattr(settings, "youtube_target_daily_quota", 8500)

        if quota_status.get("is_exhausted", False) or hits_today >= target:
            hours_left = max(0.5, float(quota_status.get("hours_until_reset", 4.0)))
            sleep_sec = int(hours_left * 3600)
            return sleep_sec, f"{hours_left:.1f}h (Daily Quota Target Met [{hits_today}/{target}] - Sleeping until PT Midnight Reset)"

        # 2. Check unlinked finished matches in the last 30 days
        now_utc = datetime.now(UTC)
        thirty_days_ago = (now_utc - timedelta(days=30)).isoformat()
        res = (
            db_client.table("matches")
            .select("id, utc_date, last_youtube_check")
            .eq("status", "FINISHED")
            .is_("highlight_video_id", "null")
            .gte("utc_date", thirty_days_ago)
            .order("utc_date", desc=True)
            .limit(100)
            .execute()
        )
        unlinked = res.data or []

        # 3. Check earliest cooldown expiration across all unlinked fixtures
        has_fresh_matches = False
        earliest_wait_min = 1440.0
        for m in unlinked:
            raw_date = m.get("utc_date")
            if not raw_date:
                continue
            m_time = match_service.safe_parse_iso(raw_date)
            age_hours = (now_utc - m_time).total_seconds() / 3600.0
            last_check_str = m.get("last_youtube_check")
            if not last_check_str:
                earliest_wait_min = 0.0
                break
            last_check = match_service.safe_parse_iso(last_check_str)
            mins_since = (now_utc - last_check).total_seconds() / 60.0

            if age_hours < 6:
                has_fresh_matches = True
                req_cd = 30.0
            elif age_hours < 24:
                req_cd = 120.0
            elif age_hours < 72:
                req_cd = 360.0
            else:
                req_cd = 1440.0

            wait_remaining = max(0.0, req_cd - mins_since)
            if wait_remaining < earliest_wait_min:
                earliest_wait_min = wait_remaining

        # 4. If fresh matches are ready, pace sweeps at 5 minutes (300s)
        if earliest_wait_min <= 1.0:
            return 300, "5 Minutes (Eligible Fresh Fixtures Ready for YouTube Sweep)"

        # 5. If all fresh matches are in cooldown, sleep dynamically (15m-60m)
        if hits_today < target and remaining >= 500:
            sleep_min = min(60, max(15, int(earliest_wait_min) + 1))
            return sleep_min * 60, f"{sleep_min}m (Dynamic Cooldown Pacing - {hits_today}/{target} units consumed)"

        if has_fresh_matches:
            sleep_min = min(30, max(5, int(earliest_wait_min) + 1))
            return sleep_min * 60, f"{sleep_min}m (Tier 1 Fresh Match Embargo Cooldown)"

        sleep_min = min(120, max(15, int(earliest_wait_min) + 1))
        return sleep_min * 60, f"{sleep_min}m (Tier Cooldown Guardrail Pacing)"

    @staticmethod
    async def calculate_dynamic_sleep() -> tuple[int, str]:
        now_utc = datetime.now(UTC)
        now_iso = now_utc.isoformat()

        # Check if there are active LIVE matches
        live_res = (
            db_client.table("matches")
            .select("id")
            .in_("status", ["IN_PLAY", "PAUSED"])
            .limit(1)
            .execute()
        )
        if live_res.data:
            return settings.sleep_live_match, "60s (Near-Live Match In Progress)"

        # Check if matches kick off within the next 45 minutes
        future_res = (
            db_client.table("matches")
            .select("utc_date")
            .eq("status", "TIMED")
            .gte("utc_date", now_iso)
            .order("utc_date", desc=False)
            .limit(1)
            .execute()
        )
        if future_res.data:
            next_start = match_service.safe_parse_iso(future_res.data[0]["utc_date"])
            diff_min = (next_start - now_utc).total_seconds() / 60
            if 0 <= diff_min <= 45:
                return settings.sleep_near_match, f"15 Minutes (Upcoming kickoff in {int(diff_min)}m)"

        return settings.sleep_normal, "2 Hours (Standard Idle Interval)"


cycle_scheduler = CycleScheduler()
