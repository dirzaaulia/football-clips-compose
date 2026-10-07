from datetime import UTC, datetime, timedelta

from loguru import logger
from worker.clients.supabase_client import supabase_client


from worker.config.settings import settings


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
                msg = f"🧹 Menghapus {deleted_count} pertandingan kadaluarsa (>{retention} hari)."
            else:
                msg = f"⚪ Database bersih (Tidak ada data >{retention} hari)."

            log_events.append(msg)
            logger.info(msg)
            return deleted_count
        except Exception as e:
            err = f"🚨 [ERROR CLEANUP] {e}"
            log_events.append(err)
            logger.error(err)
            return 0


maintenance_service = MaintenanceService()
