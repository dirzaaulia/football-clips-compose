import json
from collections import Counter
from datetime import UTC, datetime, timedelta
from typing import Any

from loguru import logger

from worker.clients.database_client import db_client
from worker.clients.edge_cache_client import edge_cache_client
from worker.clients.youtube_client import youtube_client
from worker.config.settings import settings
from worker.services.highlight.cooldown import (
    is_backlog_safe_exhausted,
    should_check_youtube_for_match,
    sort_unlinked_matches_by_priority,
)
from worker.services.highlight.filters import (
    get_worldwide_watchability_score,
    is_target_market_catered,
)
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
                attributes={
                    "step": "OUTCOME",
                    "status": "TERMINAL_UNAVAILABLE",
                    "reason": reason,
                    "competition_id": match.get("competition_id"),
                    "competition_name": match.get("competition_name"),
                    "utc_date": match.get("utc_date"),
                    "home_team": match.get("home_team_name"),
                    "away_team": match.get("away_team_name"),
                },
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

        # 1. Priority 1: Unlinked matches (last 30 days)
        res_unlinked = (
            db_client.table("matches")
            .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, last_youtube_check, highlight_video_id, youtube_region_info, home_score, away_score")
            .eq("status", "FINISHED")
            .is_("highlight_video_id", "null")
            .gte("utc_date", thirty_days_ago)
            .order("utc_date", desc=True)
            .limit(150)
            .execute()
        )
        unlinked_p1 = [m for m in (res_unlinked.data or []) if not self.is_terminal(m)]

        # 2. Priority 2: Current month matches seeking Tier 1 / 99%+ upgrades
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
        upgrades_p2 = [
            m for m in (res_restricted.data or [])
            if not self.is_terminal(m) and get_worldwide_watchability_score(m.get("youtube_region_info")) < 99.0
        ]

        all_candidates = unlinked_p1 + upgrades_p2
        if not all_candidates:
            append_event(log_events, "All FINISHED fixtures within 30 days have 99%+ worldwide official highlights.", "INFO", "YOUTUBE", "ALL_LINKED")
            return 0

        eligible_matches = []
        cooldown_skipped = []

        def collect_eligible(candidates_list: list[dict[str, Any]]):
            sorted_tier = sort_unlinked_matches_by_priority(candidates_list, now_utc)
            for m in sorted_tier:
                if len(eligible_matches) >= max_batch:
                    break
                ok, reason = should_check_youtube_for_match(m, now_utc)
                if ok:
                    eligible_matches.append(m)
                else:
                    cooldown_skipped.append((m, reason))

        # Fill batch strictly by Priority 1 -> Priority 2
        collect_eligible(unlinked_p1)
        if len(eligible_matches) < max_batch:
            collect_eligible(upgrades_p2)

        # Dynamic Cooldown Bypass: if batch has capacity and quota is abundant (>= 500)
        try:
            safe_quota = youtube_client.get_quota_status().get("remaining", 5000)
        except Exception:
            safe_quota = 5000
        if len(eligible_matches) < max_batch and safe_quota >= 500 and cooldown_skipped:
            bypassed = []
            for m, _ in cooldown_skipped:
                if m not in eligible_matches:
                    eligible_matches.append(m)
                    bypassed.append(m)
                    if len(eligible_matches) >= max_batch:
                        break
            if bypassed:
                append_event(
                    log_events,
                    f"Quota abundant ({safe_quota} units). Bypassed cooldown for {len(bypassed)} fixture(s).",
                    "INFO",
                    "YOUTUBE",
                    "COOLDOWN_BYPASSED",
                )

        if cooldown_skipped and not (len(eligible_matches) >= max_batch and safe_quota >= 500):
            tier_counter = Counter()
            for m, r in cooldown_skipped:
                t_match = r.split(" active")[0].strip() if " active" in r else "Active"
                tier_counter[t_match] += 1
            tier_summary = ", ".join(f"{cnt} in {t}" for t, cnt in tier_counter.items())
            append_event(log_events, f"Cooldown guardrail: {len(cooldown_skipped)} fixture(s) protected ({tier_summary}).", "INFO", "YOUTUBE", "COOLDOWN_SUMMARY")

        # Claimed video IDs
        try:
            res_claimed = db_client.table("matches").select("highlight_video_id").not_.is_("highlight_video_id", "null").execute()
            claimed_video_ids = {r["highlight_video_id"] for r in (res_claimed.data or []) if r.get("highlight_video_id")}
        except Exception:
            claimed_video_ids = set()

        filtered_batch = eligible_matches[:max_batch]
        linked_count = 0

        for match in filtered_batch:
            try:
                vid, reg, backup_vid, backup_reg, highlight_videos = self.search_pipeline.search_highlight_for_match(match, log_events, claimed_video_ids)
                if vid:
                    if vid in claimed_video_ids and vid != match.get("highlight_video_id"):
                        db_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
                        continue

                    current_reg = match.get("youtube_region_info") or {}
                    if isinstance(current_reg, str):
                        try:
                            current_reg = json.loads(current_reg)
                        except Exception:
                            current_reg = {}
                    sweep_count = (current_reg.get("sweep_count") or 0) + 1

                    if reg is None:
                        reg = {}
                    reg["sweep_count"] = sweep_count

                    watchability = get_worldwide_watchability_score(reg)
                    target_catered = is_target_market_catered(reg)

                    if not target_catered or watchability < 99.0:
                        exhausted, exhaust_reason = is_backlog_safe_exhausted(match, now_utc, sweep_count=sweep_count)
                        if exhausted:
                            reg["target_market_exhausted"] = True
                            reg["terminal"] = True
                            reg["exhausted_reason"] = exhaust_reason
                            append_event(
                                log_events,
                                f"   [MARKET EXHAUSTED] {match.get('home_team_name')} vs {match.get('away_team_name')} marked exhausted ({exhaust_reason}).",
                                "INFO",
                                "YOUTUBE",
                                "TARGET_MARKET_EXHAUSTED",
                                match_id=match["id"],
                            )

                    update_data = {"highlight_video_id": vid, "last_youtube_check": now_iso, "youtube_region_info": reg}
                    if backup_vid:
                        update_data["backup_highlight_video_id"] = backup_vid
                    if backup_reg is not None:
                        update_data["backup_youtube_region_info"] = backup_reg
                    if highlight_videos:
                        update_data["highlight_videos"] = highlight_videos

                    try:
                        db_client.table("matches").update(update_data).eq("id", match["id"]).execute()
                        claimed_video_ids.add(vid)
                        linked_count += 1
                        append_event(
                            log_events,
                            f"   [HIGHLIGHT LINKED] {match.get('home_team_name')} vs {match.get('away_team_name')} -> https://youtu.be/{vid}",
                            "SUCCESS",
                            "YOUTUBE",
                            "DB_LINKED",
                            match_id=match["id"],
                            attributes={
                                "step": "OUTCOME",
                                "status": "LINKED",
                                "video_id": vid,
                                "competition_id": match.get("competition_id"),
                                "competition_name": match.get("competition_name"),
                                "utc_date": match.get("utc_date"),
                                "home_team": match.get("home_team_name"),
                                "away_team": match.get("away_team_name"),
                            },
                        )
                        notification_service.notify_highlight_ready(match, vid, "Official Highlights", log_events)
                    except Exception as e:
                        db_client.table("matches").update({"last_youtube_check": now_iso}).eq("id", match["id"]).execute()
                else:
                    current_reg = match.get("youtube_region_info") or {}
                    if isinstance(current_reg, str):
                        try:
                            current_reg = json.loads(current_reg)
                        except Exception:
                            current_reg = {}
                    sweep_count = (current_reg.get("sweep_count") or 0) + 1

                    exhausted, exhaust_reason = is_backlog_safe_exhausted(match, now_utc, sweep_count=sweep_count)
                    if exhausted:
                        self.mark_match_unavailable(match, reason=f"NO_OFFICIAL_BROADCASTER ({exhaust_reason})", log_events=log_events)
                    else:
                        current_reg["sweep_count"] = sweep_count
                        db_client.table("matches").update({
                            "last_youtube_check": now_iso,
                            "youtube_region_info": current_reg,
                        }).eq("id", match["id"]).execute()
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
