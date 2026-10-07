import asyncio
import signal
import sys
from datetime import UTC, datetime
from pathlib import Path

# Ensure root workspace is in sys.path when invoked directly
_pkg_root = str(Path(__file__).resolve().parent.parent)
if _pkg_root not in sys.path:
    sys.path.insert(0, _pkg_root)

from loguru import logger

from worker.clients.supabase_client import supabase_client
from worker.config.settings import settings
from worker.services.highlight_service import highlight_service
from worker.services.maintenance_service import maintenance_service
from worker.services.match_service import match_service
from worker.services.standings_service import standings_service
from worker.services.telemetry_service import telemetry_service


class WorkerOrchestrator:

    def __init__(self):
        self.is_running = True
        self.cycle_index = 0
        self._setup_signals()

    def _setup_signals(self):
        def handle_signal(sig, frame):
            logger.info("Signal shutdown diterima. Menyelesaikan siklus dan berhenti bersih...")
            self.is_running = False
        signal.signal(signal.SIGINT, handle_signal)
        signal.signal(signal.SIGTERM, handle_signal)

    async def calculate_dynamic_sleep(self) -> tuple[int, str]:
        now_utc = datetime.now(UTC)
        now_iso = now_utc.isoformat()

        # Cek apakah ada laga LIVE saat ini
        live_res = (
            supabase_client.table("matches")
            .select("id")
            .in_("status", ["IN_PLAY", "PAUSED"])
            .limit(1)
            .execute()
        )
        if live_res.data:
            return settings.sleep_live_match, "10 Menit (Ada Pertandingan LIVE)"

        # Cek apakah ada laga yang akan kick-off dalam 45 menit ke depan
        future_res = (
            supabase_client.table("matches")
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
                return settings.sleep_near_match, f"30 Menit (Kickoff laga berikutnya dalam {int(diff_min)}m)"

        return settings.sleep_normal, "2 Jam (Jeda normal antarlaga)"

    async def run_single_cycle(self, is_full_sync: bool = False):
        self.cycle_index += 1
        logger.info(f"=== Memulai Siklus #{self.cycle_index} ===")
        steps_log = []

        # STEP 1: Sinkronisasi Jadwal Pertandingan & Auto-Sync Supported Clubs
        step1_events = []
        try:
            await match_service.sync_matches(is_full_sync=is_full_sync, log_events=step1_events)
            step1_status = "OK"
        except Exception as e:
            step1_events.append(f"🚨 Error Step 1: {e}")
            step1_status = "ERROR"
        steps_log.append({"title": "Step 1: Sinkronisasi Jadwal & Tim", "status": step1_status, "events": step1_events})

        # STEP 2: Sinkronisasi Video Highlight Resmi YouTube
        step2_events = []
        is_backfill_mode = is_full_sync or ("--force" in sys.argv) or ("--backfill" in sys.argv)
        batch_limit = 100 if is_backfill_mode else 25
        try:
            highlight_service.sync_unlinked_highlights(max_batch=batch_limit, log_events=step2_events)
            step2_status = "OK"
        except Exception as e:
            step2_events.append(f"🚨 Error Step 2: {e}")
            step2_status = "ERROR"
        steps_log.append({"title": "Step 2: Sinkronisasi Highlight", "status": step2_status, "events": step2_events})

        # STEP 3: Sinkronisasi Klasemen Liga (Standings)
        step3_events = []
        # Sync on full sync, startup, or if last sync > 6 hours ago
        hours_since_last_standings = (datetime.now(UTC).timestamp() - standings_service.last_sync_timestamp) / 3600.0
        should_sync_standings = is_full_sync or (standings_service.last_sync_timestamp == 0.0) or (hours_since_last_standings >= 6.0)
        if should_sync_standings:
            try:
                await standings_service.sync_standings(force=is_full_sync, log_events=step3_events)
                step3_status = "OK"
            except Exception as e:
                step3_events.append(f"🚨 Error Step 3: {e}")
                step3_status = "ERROR"
        else:
            step3_events.append(f"⚪ Klasemen masih up-to-date (sinkron terakhir {hours_since_last_standings:.1f} jam lalu). Skip.")
            step3_status = "SKIPPED"
        steps_log.append({"title": "Step 3: Sinkronisasi Klasemen", "status": step3_status, "events": step3_events})

        # STEP 4: Maintenance & Housekeeping (Hapus laga >30 hari / 4 minggu)
        step4_events = []
        try:
            maintenance_service.cleanup_old_data(log_events=step4_events)
            step4_status = "OK"
        except Exception as e:
            step4_events.append(f"🚨 Error Step 4: {e}")
            step4_status = "ERROR"
        steps_log.append({"title": "Step 4: Maintenance", "status": step4_status, "events": step4_events})

        # Hitung jeda tidur cerdas berikutnya
        sleep_sec, sleep_desc = await self.calculate_dynamic_sleep()
        telemetry_service.record_cycle(
            mode="FULL SYNC" if is_full_sync else "REGULAR SYNC",
            next_sleep_str=sleep_desc,
            cycle_index=self.cycle_index,
            steps_log=steps_log
        )
        return sleep_sec, sleep_desc

    async def start(self):
        is_once = "--once" in sys.argv
        is_force = "--force" in sys.argv

        logger.info("🚀 Football Clips Worker v2.0 (Async Engine) dimulai.")
        while self.is_running:
            is_full = is_force or (self.cycle_index == 0) or (self.cycle_index % 12 == 0)
            sleep_sec, sleep_desc = await self.run_single_cycle(is_full_sync=is_full)

            if is_once:
                logger.info("Mode --once selesai. Keluar.")
                break

            logger.info(f"😴 Siklus #{self.cycle_index} selesai. Tidur {sleep_desc} ({sleep_sec}s)...")
            try:
                await asyncio.sleep(sleep_sec)
            except asyncio.CancelledError:
                break

if __name__ == "__main__":
    orchestrator = WorkerOrchestrator()
    asyncio.run(orchestrator.start())
