import html
import re
from datetime import UTC, datetime, timedelta
from typing import Any

from loguru import logger

from worker.clients.database_client import db_client
from worker.clients.edge_cache_client import edge_cache_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.services.highlight.filters import check_forbidden_title, has_highlight_keyword, has_scoreline
from worker.services.highlight.normalizer import get_team_aliases
from worker.services.highlight.scorer import fallback_score_video
from worker.services.notification_service import notification_service
from worker.services.telemetry_service import telemetry_service
from worker.utils.telemetry_logger import append_event


class WebsubMatcher:
    @staticmethod
    def link_websub_video(
        video_id: str,
        title: str,
        channel_id: str,
        author_name: str = "",
        log_events: list[dict[str, Any]] | None = None,
    ) -> bool:
        if log_events is None:
            log_events = []

        if not video_id or not title:
            return False

        import worker.services.highlight_service as hs

        active_db = getattr(hs, "supabase_client", db_client)
        active_rss = getattr(hs, "youtube_rss_client", youtube_rss_client)
        active_notify = getattr(hs, "notification_service", notification_service)

        # 1. Title verification
        is_forbidden, reason = check_forbidden_title(title)
        if is_forbidden or not has_highlight_keyword(title):
            logger.info(f"[WebSub Direct] Video {video_id} ('{title}') rejected: {reason or 'No highlight keyword'}")
            return False

        # 2. Query recent finished unlinked matches
        now_utc = datetime.now(UTC)
        cutoff_72h = (now_utc - timedelta(hours=72)).isoformat()
        try:
            res = (
                active_db.table("matches")
                .select("*")
                .eq("status", "FINISHED")
                .is_("highlight_video_id", "null")
                .gte("utc_date", cutoff_72h)
                .order("utc_date", desc=True)
                .limit(50)
                .execute()
            )
            unlinked_matches = res.data or []
        except Exception as e:
            logger.error(f"[WebSub Direct] DB error: {e}")
            return False

        if not unlinked_matches:
            return False

        # 3. Match against team aliases
        best_match = None
        best_score = -1
        t_clean = html.unescape(title).lower()

        for m in unlinked_matches:
            home = m.get("home_team_name", "")
            away = m.get("away_team_name", "")
            h_aliases = get_team_aliases(home)
            a_aliases = get_team_aliases(away)

            home_hit = any(re.search(rf"\b{re.escape(a)}\b", t_clean) for a in h_aliases)
            away_hit = any(re.search(rf"\b{re.escape(a)}\b", t_clean) for a in a_aliases)

            if (home_hit and away_hit) or ((home_hit or away_hit) and has_scoreline(t_clean)):
                match_dt = now_utc
                try:
                    match_dt = datetime.fromisoformat(str(m.get("utc_date", "")).replace("Z", "+00:00"))
                except Exception:
                    pass

                score = fallback_score_video(
                    title=title,
                    duration_sec=300,
                    home_team=home,
                    away_team=away,
                    h_aliases=h_aliases,
                    a_aliases=a_aliases,
                    home_score=m.get("home_score"),
                    away_score=m.get("away_score"),
                    published_at=now_utc,
                    match_utc=match_dt,
                )
                if score > best_score:
                    best_score = score
                    best_match = m

        if best_match and best_score >= 50:
            is_playable, is_embeddable, _, _ = active_rss.check_video_global_availability(video_id)
            if not is_playable:
                return False

            now_iso = now_utc.isoformat()
            try:
                active_db.table("matches").update({
                    "highlight_video_id": video_id,
                    "last_youtube_check": now_iso,
                    "updated_at": now_iso,
                }).eq("id", best_match["id"]).execute()

                append_event(
                    log_events,
                    f"[WEBSUB INSTANT LINK] Successfully linked video {video_id} ('{title}') -> {best_match.get('home_team_name')} vs {best_match.get('away_team_name')}",
                    "SUCCESS",
                    "WEBSUB",
                    "INSTANT_LINK",
                    match_id=best_match.get("id"),
                )
                active_notify.notify_highlight_ready(best_match, video_id, "Official Highlights", log_events)
                edge_cache_client.purge_matches_cache()
                telemetry_service.record_cycle("WEBSUB_PUSH", "0s", 0, log_events, pipeline_type="WEBSUB_PUSH")
                return True
            except Exception as e:
                logger.error(f"[WebSub Direct] Failed to update match {best_match.get('id')}: {e}")
                return False

        return False
