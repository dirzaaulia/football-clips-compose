from datetime import UTC, datetime, timedelta

from loguru import logger
from worker.clients.supabase_client import supabase_client

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
                msg = f"Cleaned up {deleted_count} stale matches older than {retention} days."
            else:
                msg = f"Database clean. No stale records older than {retention} days found."

            append_event(log_events, msg, "INFO", "MAINTENANCE", "CLEANUP_DONE")
            logger.info(msg)
            return deleted_count
        except Exception as e:
            err = f"Cleanup database error: {e}"
            append_event(log_events, err, "ERROR", "MAINTENANCE", "CLEANUP_ERROR")
            logger.error(err)
            return 0


maintenance_service = MaintenanceService()
