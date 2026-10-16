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

            # Auto-heal matches stuck in IN_PLAY or PAUSED past 180 minutes
            self.heal_stuck_matches(log_events=log_events)

            # Auto-deduplicate cross-provider fixture collisions where one is linked
            self.deduplicate_matches(log_events=log_events)

            return deleted_count
        except Exception as e:
            err = f"Cleanup database error: {e}"
            append_event(log_events, err, "ERROR", "MAINTENANCE", "CLEANUP_ERROR")
            logger.error(err)
            return 0

    def heal_stuck_matches(self, log_events: list | None = None) -> int:
        """Heal matches stuck in IN_PLAY or PAUSED where kickoff was >180 minutes ago."""
        if log_events is None:
            log_events = []
        now_utc = datetime.now(UTC)
        cutoff_stuck = (now_utc - timedelta(minutes=180)).isoformat()
        try:
            res = (
                supabase_client.table("matches")
                .select("id, competition_id, home_team_name, away_team_name, status, utc_date")
                .in_("status", ["IN_PLAY", "PAUSED"])
                .lt("utc_date", cutoff_stuck)
                .execute()
            )
            stuck_matches = res.data or []
            healed = 0
            for m in stuck_matches:
                supabase_client.table("matches").update({
                    "status": "FINISHED",
                    "display_clock": "FT",
                    "updated_at": now_utc.isoformat(),
                }).eq("id", m["id"]).execute()
                healed += 1
                msg = f"[HEAL STUCK MATCH] Transitioned {m.get('home_team_name')} vs {m.get('away_team_name')} ({m.get('competition_id')}) from {m.get('status')} -> FINISHED (Kickoff was {m.get('utc_date')})."
                append_event(log_events, msg, "WARN", "MAINTENANCE", "HEAL_STUCK")
                logger.warning(msg)
            return healed
        except Exception as e:
            logger.error(f"[Maintenance] Error healing stuck matches: {e}")
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

    def deduplicate_matches(self, log_events: list | None = None) -> int:
        """
        Detects duplicate fixtures across data providers (e.g. ESPN and Football-Data)
        sharing the same calendar date, competition, and team roots.
        If one fixture already has a linked highlight and the other does not, removes the unlinked duplicate.
        """
        if log_events is None:
            log_events = []
        try:
            res = (
                supabase_client.table("matches")
                .select("id, competition_id, home_team_name, away_team_name, utc_date, status, highlight_video_id, provider")
                .execute()
            )
            matches = res.data or []
            if not matches:
                return 0

            from collections import defaultdict

            import re

            def clean_word(name: str) -> str:
                # Strip 4-digit years (e.g. 1907, 1899), numbers, punctuation
                s = re.sub(r'\b(18|19|20)\d{2}\b', '', (name or "").lower())
                s = re.sub(r'\b(fc|cf|as|afc|vfb|vfl|sc|rb|bv|bvb)\b', '', s)
                s = re.sub(r'[^a-z]', '', s)
                return s[:4] if len(s) >= 4 else s

            groups = defaultdict(list)
            for m in matches:
                d = str(m.get("utc_date", ""))[:10]
                comp = str(m.get("competition_id", "")).upper()
                h = clean_word(m.get("home_team_name", ""))
                a = clean_word(m.get("away_team_name", ""))
                key = (d, comp, h, a)
                groups[key].append(m)

            deleted_count = 0
            for key, dupes in groups.items():
                if len(dupes) > 1:
                    with_hl = [m for m in dupes if m.get("highlight_video_id")]
                    without_hl = [m for m in dupes if not m.get("highlight_video_id")]
                    if with_hl and without_hl:
                        for to_del in without_hl:
                            supabase_client.table("matches").delete().eq("id", to_del["id"]).execute()
                            deleted_count += 1
                            msg = f"[DEDUP] Removed unlinked duplicate fixture ID {to_del['id']} ({to_del.get('home_team_name')} vs {to_del.get('away_team_name')}) in favor of linked fixture ID {with_hl[0]['id']}."
                            append_event(log_events, msg, "INFO", "MAINTENANCE", "DEDUP_FIXTURE")
                            logger.info(msg)

            return deleted_count
        except Exception as e:
            logger.error(f"[Maintenance] Error deduplicating matches: {e}")
            return 0


maintenance_service = MaintenanceService()
