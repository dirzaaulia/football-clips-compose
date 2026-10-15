from datetime import UTC, datetime, timedelta

from loguru import logger
from worker.clients.supabase_client import supabase_client
from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings
from worker.utils.telemetry_logger import append_event


class MaintenanceService:

    def cleanup_old_data(self, log_events: list | None = None) -> int:
        if log_events is None:
            log_events = []

        retention = settings.retention_days
        cutoff_date = (datetime.now(UTC) - timedelta(days=retention)).isoformat()
        try:
            res = (
                supabase_client.table("matches")
                .delete()
                .lt("utc_date", cutoff_date)
                .execute()
            )
            deleted_count = len(res.data) if res.data else 0
            if deleted_count > 0:
                msg = f"[PURGE] Cleaned up {deleted_count} stale fixtures older than {retention}-day retention window (cutoff: {cutoff_date[:10]})."
            else:
                msg = f"[PURGE] Retention clean. All fixtures are within active {retention}-day retention window (cutoff: {cutoff_date[:10]})."

            append_event(log_events, msg, "INFO", "MAINTENANCE", "CLEANUP_DONE")
            logger.info(msg)

            # Auto-purge any vertical YouTube Shorts so they can be re-scraped as 16:9 official highlights
            self.purge_invalid_shorts(log_events=log_events)

            return deleted_count
        except Exception as e:
            err = f"Cleanup database error: {e}"
            append_event(log_events, err, "ERROR", "MAINTENANCE", "CLEANUP_ERROR")
            logger.error(err)
            return 0

    def purge_invalid_shorts(self, log_events: list | None = None) -> int:
        if log_events is None:
            log_events = []
        try:
            res = (
                supabase_client.table("matches")
                .select("id, home_team_name, away_team_name, highlight_video_id")
                .not_.is_("highlight_video_id", "null")
                .execute()
            )
            matches = res.data or []
            purged = 0
            for m in matches:
                vid = m.get("highlight_video_id")
                if not vid:
                    continue
                if youtube_client.is_youtube_short(vid):
                    supabase_client.table("matches").update({
                        "highlight_video_id": None,
                        "last_youtube_check": None
                    }).eq("id", m["id"]).execute()
                    purged += 1
                    msg = f"[PURGE SHORT] Unlinked vertical YouTube Short ({vid}) from {m.get('home_team_name')} vs {m.get('away_team_name')}."
                    append_event(log_events, msg, "WARN", "MAINTENANCE", "PURGE_SHORT")
                    logger.warning(msg)
            return purged
        except Exception as e:
            logger.debug(f"Audit shorts skipped: {e}")
            return 0


maintenance_service = MaintenanceService()
