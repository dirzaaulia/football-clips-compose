from datetime import UTC, datetime
from typing import Any

from loguru import logger

from worker.clients.database_client import db_client
from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings
from worker.repositories.channel_repository import channel_repository
from worker.services.highlight.cooldown import COMPETITION_PRIORITY
from worker.services.highlight.normalizer import get_club_handle
from worker.utils.telemetry_logger import append_event


class QuotaMaximizer:
    def __init__(self, search_pipeline, unavailable_marker):
        self.search_pipeline = search_pipeline
        self.mark_unavailable = unavailable_marker

    def maximize_daily_quota_usage(
        self,
        max_batch: int = 10,
        log_events: list[dict[str, Any]] | None = None,
        claimed_video_ids: set[str] | None = None,
    ) -> int:
        if log_events is None:
            log_events = []

        quota_status = youtube_client.get_quota_status()
        hits_today = quota_status.get("hits_today", 0)
        remaining = quota_status.get("remaining", 0)
        target = getattr(settings, "youtube_target_daily_quota", 8500)

        if hits_today >= target or remaining < 500:
            return 0

        deficit = target - hits_today
        budget_calls = min(max_batch, deficit // 100, remaining // 100)
        if budget_calls <= 0:
            return 0

        now_utc = datetime.now(UTC)
        now_iso = now_utc.isoformat()

        thirty_days_ago = (now_utc - timedelta(days=30)).isoformat()
        try:
            res = (
                db_client.table("matches")
                .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, last_youtube_check, season, home_score, away_score, youtube_region_info")
                .eq("status", "FINISHED")
                .is_("highlight_video_id", "null")
                .gte("utc_date", thirty_days_ago)
                .order("utc_date", desc=True)
                .limit(200)
                .execute()
            )
            candidates = [m for m in (res.data or []) if not (m.get("youtube_region_info") or {}).get("terminal")]
        except Exception as e:
            logger.debug(f"[QuotaMaximizer] Failed to query candidates: {e}")
            return 0

        if not candidates:
            return 0

        def has_official_targets(m: dict) -> bool:
            c_id = (m.get("competition_id") or "").upper()
            c_name = (m.get("competition_name") or "").upper()
            league_h, _ = channel_repository.get_competition_channel(c_id)
            if not league_h and c_name:
                league_h, _ = channel_repository.get_competition_channel(c_name)
            if league_h:
                return True
            return bool(get_club_handle(m.get("home_team_name", "")) or get_club_handle(m.get("away_team_name", "")))

        big_clubs = {
            "real madrid", "barcelona", "atletico madrid", "manchester city", "arsenal", "liverpool",
            "manchester united", "chelsea", "tottenham", "bayern", "dortmund", "juventus", "inter", "milan", "psg"
        }
        def is_big(name: str) -> bool:
            t = (name or "").lower()
            return any(b in t for b in big_clubs)

        def enrichment_priority(m: dict) -> tuple:
            h = m.get("home_team_name", "")
            a = m.get("away_team_name", "")
            tot_goals = (m.get("home_score") or 0) + (m.get("away_score") or 0)
            h_b = is_big(h)
            a_b = is_big(a)
            tier = 0 if ((h_b and a_b) or (h_b and tot_goals >= 4)) else (1 if (h_b or a_b or tot_goals >= 3) else 2)
            c_id = (m.get("competition_id") or "").upper()
            prio = COMPETITION_PRIORITY.get(c_id, 99)
            return (tier, prio, -tot_goals)

        valid_targets = [m for m in candidates if has_official_targets(m)]
        if not valid_targets:
            return 0

        valid_targets.sort(key=enrichment_priority)
        eligible = valid_targets[:budget_calls]

        if claimed_video_ids is None:
            try:
                res_c = db_client.table("matches").select("highlight_video_id").not_.is_("highlight_video_id", "null").execute()
                claimed_video_ids = {r["highlight_video_id"] for r in (res_c.data or []) if r.get("highlight_video_id")}
            except Exception:
                claimed_video_ids = set()

        linked_count = 0
        for match in eligible:
            try:
                vid, reg, backup_vid, backup_reg, highlight_videos = self.search_pipeline.search_highlight_for_match(
                    match, log_events, claimed_video_ids, allow_search_api=True
                )
                if vid:
                    if vid in claimed_video_ids:
                        db_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
                        continue

                    update_data = {"highlight_video_id": vid, "last_youtube_check": now_iso}
                    if backup_vid:
                        update_data["backup_highlight_video_id"] = backup_vid
                    if reg is not None:
                        update_data["youtube_region_info"] = reg
                    if backup_reg is not None:
                        update_data["backup_youtube_region_info"] = backup_reg
                    if highlight_videos:
                        update_data["highlight_videos"] = highlight_videos

                    db_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                    claimed_video_ids.add(vid)
                    linked_count += 1
                else:
                    db_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
            except Exception as e:
                db_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()

        return linked_count
