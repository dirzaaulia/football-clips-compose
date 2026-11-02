import json
from collections import Counter
from datetime import UTC, datetime, timedelta
from typing import Any

from loguru import logger

from worker.clients.database_client import db_client
from worker.clients.edge_cache_client import edge_cache_client
from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings
from worker.services.highlight.cooldown import should_check_youtube_for_match, sort_unlinked_matches_by_priority
from worker.services.highlight.filters import get_worldwide_watchability_score
from worker.services.notification_service import notification_service
from worker.utils.telemetry_logger import append_event


class BatchCoordinator:
    def __init__(self, search_pipeline, quota_maximizer):
        self.search_pipeline = search_pipeline
        self.quota_maximizer = quota_maximizer

    @staticmethod
    def is_terminal(m: dict[str, Any]) -> bool:
        info = m.get("youtube_region_info")
        if isinstance(info, dict):
            return bool(info.get("terminal"))
        if isinstance(info, str) and info.strip():
            try:
                parsed = json.loads(info)
                if isinstance(parsed, dict):
                    return bool(parsed.get("terminal"))
            except Exception:
                pass
        return False

    def mark_match_unavailable(
        self,
        match: dict[str, Any],
        reason: str = "NO_OFFICIAL_BROADCASTER",
        log_events: list[dict[str, Any]] | None = None,
    ) -> None:
        now_iso = datetime.now(UTC).isoformat()
        terminal_info = {"terminal": True, "status": "UNAVAILABLE", "reason": reason, "checked_at": now_iso}
        try:
            db_client.table("matches").update({
                "youtube_region_info": terminal_info,
                "last_youtube_check": now_iso,
            }).eq("id", match["id"]).execute()
            append_event(
                log_events,
                f"   [TERMINAL UNAVAILABLE] Marked {match.get('home_team_name')} vs {match.get('away_team_name')} as UNAVAILABLE ({reason}).",
                "INFO",
                "YOUTUBE",
                "TERMINAL_UNAVAILABLE",
                match_id=match["id"],
            )
        except Exception as e:
            logger.warning(f"Error marking match {match.get('id')} unavailable: {e}")

    def sync_unlinked_highlights(
        self,
        max_batch: int = 25,
        log_events: list[dict[str, Any]] | None = None,
    ) -> int:
        if log_events is None:
            log_events = []

        now_utc = datetime.now(UTC)
        now_iso = now_utc.isoformat()
        thirty_days_ago = (now_utc - timedelta(days=30)).isoformat()

        # 1. Matches without highlights (last 30 days)
        res_unlinked = (
            db_client.table("matches")
            .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, last_youtube_check, highlight_video_id, youtube_region_info, home_score, away_score")
            .eq("status", "FINISHED")
            .is_("highlight_video_id", "null")
            .gte("utc_date", thirty_days_ago)
            .order("utc_date", desc=True)
            .limit(80)
            .execute()
        )
        unlinked = [m for m in (res_unlinked.data or []) if not self.is_terminal(m)]

        # 2. Restricted matches seeking upgrades
        res_restricted = (
            db_client.table("matches")
            .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, last_youtube_check, highlight_video_id, youtube_region_info, home_score, away_score")
            .eq("status", "FINISHED")
            .not_.is_("highlight_video_id", "null")
            .not_.is_("youtube_region_info", "null")
            .gte("utc_date", thirty_days_ago)
            .order("utc_date", desc=True)
            .limit(40)
            .execute()
        )
        upgrade_candidates = [
            m for m in (res_restricted.data or [])
            if not self.is_terminal(m) and get_worldwide_watchability_score(m.get("youtube_region_info")) < 99.0
        ]

        all_candidates = unlinked + upgrade_candidates
        if not all_candidates:
            append_event(log_events, "All FINISHED fixtures within 30 days have 99%+ worldwide official highlights.", "INFO", "YOUTUBE", "ALL_LINKED")
            return 0

        eligible_matches = []
        cooldown_skipped = []
        for m in all_candidates:
            ok, reason = should_check_youtube_for_match(m, now_utc)
            if ok:
                eligible_matches.append(m)
            else:
                cooldown_skipped.append((m, reason))

        if cooldown_skipped:
            tier_counter = Counter()
            for m, r in cooldown_skipped:
                t_match = r.split(" active")[0].strip() if " active" in r else "Active"
                tier_counter[t_match] += 1
            tier_summary = ", ".join(f"{cnt} in {t}" for t, cnt in tier_counter.items())
            append_event(log_events, f"Cooldown guardrail: {len(cooldown_skipped)} fixture(s) protected ({tier_summary}).", "INFO", "YOUTUBE", "COOLDOWN_SUMMARY")

        # Archive fallback if eligible is empty
        if not eligible_matches:
            try:
                res_archive = (
                    db_client.table("matches")
                    .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, last_youtube_check, highlight_video_id, youtube_region_info, home_score, away_score")
                    .eq("status", "FINISHED")
                    .lt("season", "2026")
                    .is_("highlight_video_id", "null")
                    .order("season", desc=True)
                    .order("utc_date", desc=True)
                    .limit(40)
                    .execute()
                )
                for am in (res_archive.data or []):
                    if not self.is_terminal(am):
                        ok, _ = should_check_youtube_for_match(am, now_utc)
                        if ok:
                            eligible_matches.append(am)
            except Exception as e:
                logger.warning(f"Error querying archive matches: {e}")

        # Claimed video IDs
        try:
            res_claimed = db_client.table("matches").select("highlight_video_id").not_.is_("highlight_video_id", "null").execute()
            claimed_video_ids = {r["highlight_video_id"] for r in (res_claimed.data or []) if r.get("highlight_video_id")}
        except Exception:
            claimed_video_ids = set()

        sorted_matches = sort_unlinked_matches_by_priority(eligible_matches, now_utc) if eligible_matches else []
        filtered_batch = sorted_matches[:max_batch]
        linked_count = 0

        for match in filtered_batch:
            try:
                vid, reg, backup_vid, backup_reg, highlight_videos = self.search_pipeline.search_highlight_for_match(match, log_events, claimed_video_ids)
                if vid:
                    if vid in claimed_video_ids and vid != match.get("highlight_video_id"):
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

                    try:
                        db_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                        claimed_video_ids.add(vid)
                        linked_count += 1
                        append_event(log_events, f"   [HIGHLIGHT LINKED] {match.get('home_team_name')} vs {match.get('away_team_name')} -> https://youtu.be/{vid}", "SUCCESS", "YOUTUBE", "DB_LINKED", match_id=match["id"])
                        notification_service.notify_highlight_ready(match, vid, "Official Highlights", log_events)
                    except Exception as e:
                        db_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
                else:
                    is_terminal_cand = False
                    try:
                        m_time = datetime.fromisoformat(str(match.get("utc_date", "")).replace("Z", "+00:00"))
                        if (now_utc - m_time).total_seconds() >= 7 * 86400:
                            is_terminal_cand = True
                    except Exception:
                        pass

                    if is_terminal_cand:
                        self.mark_match_unavailable(match, reason="NO_OFFICIAL_BROADCASTER", log_events=log_events)
                    else:
                        db_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
            except Exception as e:
                db_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()

        # Quota maximizer
        maximizer_linked = 0
        try:
            maximizer_linked = self.quota_maximizer.maximize_daily_quota_usage(max_batch=10, log_events=log_events, claimed_video_ids=claimed_video_ids)
        except Exception as e:
            logger.debug(f"[BatchCoordinator] Quota maximizer notice: {e}")

        total_linked = linked_count + maximizer_linked
        if total_linked > 0:
            edge_cache_client.purge_matches_cache()

        return total_linked
