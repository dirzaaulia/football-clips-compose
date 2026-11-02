import os
from collections import Counter
from datetime import UTC, datetime, timedelta

from loguru import logger
from worker.clients.espn_client import espn_client
from worker.clients.database_client import db_client as supabase_client
from worker.config.settings import settings
from worker.services.club_service import club_service
from worker.services.notification_service import notification_service
from worker.services.tactical_enricher import enrich_editorial_articles, enrich_tactical_data
from worker.utils.telemetry_logger import append_event


class MatchService:

    def __init__(self):
        self.newly_finished_count: int = 0

    def consume_newly_finished(self) -> int:
        count = self.newly_finished_count
        self.newly_finished_count = 0
        return count

    @staticmethod
    def safe_parse_iso(date_str: str | None) -> datetime:
        if not date_str:
            return datetime.now(UTC)
        try:
            cleaned = str(date_str).replace('Z', '+00:00')
            if '+' in cleaned:
                cleaned = cleaned.split('+')[0]
            if '.' in cleaned:
                cleaned = cleaned.split('.')[0]
            return datetime.fromisoformat(cleaned).replace(tzinfo=UTC)
        except Exception:
            return datetime.now(UTC)

    async def sync_live_scores(self, log_events: list | None = None) -> list[dict]:
        """Rapid 60-second live score ticker: fetches today's live/upcoming matches and updates scores/clocks."""
        if log_events is None:
            log_events = []

        now_utc = datetime.now(UTC)
        today_str = now_utc.strftime("%Y-%m-%d")
        provider_name = "ESPN Live Engine" if settings.match_data_provider == "espn" else settings.match_data_provider.upper()

        append_event(log_events, f"LIVE SCORE TICKER [{provider_name}] ({today_str})", "INFO", "MATCH_SYNC", "MODE")
        matches_raw = await espn_client.get_matches(
            competitions=settings.target_competitions,
            date_from=today_str,
            date_to=today_str,
        )

        if not matches_raw:
            append_event(log_events, f"{provider_name}: No active matches today for live score tick.", "INFO", "MATCH_SYNC", "EMPTY_API")
            return []

        return await self._process_and_save_matches(matches_raw, log_events, is_live_ticker=True)

    async def sync_matches(self, is_full_sync: bool = False, log_events: list | None = None) -> list[dict]:
        """Scheduled fixture sync (every 12 hours): fetches scheduled matches and auto-syncs supported clubs."""
        if log_events is None:
            log_events = []

        now_utc = datetime.now(UTC)
        provider_name = "ESPN Live Engine" if settings.match_data_provider == "espn" else settings.match_data_provider.upper()

        mode_text = (
            f"FULL FIXTURE SYNC [{provider_name}] (H-2 to H+{settings.upcoming_fixture_days})"
            if is_full_sync
            else f"FIXTURE SCHEDULE SYNC [{provider_name}] (H-1 to H+{settings.upcoming_fixture_days})"
        )
        logger.info(f"Sync Mode: {mode_text}")
        append_event(log_events, f"Sync Mode: {mode_text}", "INFO", "MATCH_SYNC", "MODE")

        date_from = (now_utc - timedelta(days=2 if is_full_sync else 1)).strftime("%Y-%m-%d")
        date_to = (now_utc + timedelta(days=settings.upcoming_fixture_days)).strftime("%Y-%m-%d")

        matches_raw = await espn_client.get_matches(
            competitions=settings.target_competitions,
            date_from=date_from,
            date_to=date_to,
        )

        if not matches_raw:
            append_event(log_events, f"{provider_name}: No matches received for sync window.", "WARN", "MATCH_SYNC", "EMPTY_API")
            return []

        # Auto-sync supported clubs on fixture sync
        club_service.sync_clubs_from_matches(matches_raw, log_events)

        comp_counts = Counter(m.get("competition_id") or m.get("competition", {}).get("code") or "OTHER" for m in matches_raw)
        breakdown_str = ", ".join(f"{k}: {v}" for k, v in sorted(comp_counts.items()))
        append_event(
            log_events,
            f"[FIXTURES SYNCED] Ingested upcoming fixtures across leagues: {breakdown_str}",
            "INFO",
            "MATCH_SYNC",
            "BREAKDOWN",
            attributes={
                "fixtures": [
                    {
                        "id": str(m.get("id")),
                        "home": m.get("home_team_name") or "Home",
                        "away": m.get("away_team_name") or "Away",
                        "competition": m.get("competition_name") or m.get("competition_id") or "League",
                        "utc_date": m.get("utc_date"),
                        "status": m.get("status", "SCHEDULED"),
                        "round": m.get("matchday") or m.get("round") or "",
                    }
                    for m in matches_raw
                ]
            }
        )

        return await self._process_and_save_matches(matches_raw, log_events, is_live_ticker=False)

    async def _process_and_save_matches(self, matches_raw: list[dict], log_events: list, is_live_ticker: bool = False) -> list[dict]:
        now_utc = datetime.now(UTC)
        durable = os.getenv("FC_FIXTURE_OUTBOX") == "true"

        payload_batch = []
        api_match_ids = []
        for m in matches_raw:
            m["updated_at"] = now_utc.isoformat()
            payload_batch.append(m)
            api_match_ids.append(m["id"])

        unique_payloads = list({p['id']: p for p in payload_batch}.values())
        if not unique_payloads:
            return []

        try:
            old_data_map = {}
            select_cols = "id, status, home_score, away_score, home_penalties, away_penalties, formations, statistics, editorial_headline, events"
            for i in range(0, len(api_match_ids), 50):
                try:
                    res_db = supabase_client.table("matches").select(select_cols).in_("id", api_match_ids[i:i+50]).execute()
                except Exception as sel_err:
                    if "events" in str(sel_err).lower():
                        select_cols = "id, status, home_score, away_score, home_penalties, away_penalties, formations, statistics, editorial_headline"
                        res_db = supabase_client.table("matches").select(select_cols).in_("id", api_match_ids[i:i+50]).execute()
                    else:
                        raise sel_err
                for row in res_db.data:
                    old_data_map[row["id"]] = row

            changes_found = False
            finished_match_dispatches: list[dict[str, Any]] = []
            for p in unique_payloads:
                old = old_data_map.get(p["id"])
                h_team, a_team = p.get("home_team_name", "Home"), p.get("away_team_name", "Away")
                comp_name = p.get("competition_name") or p.get("competition_id") or "League"
                m_date = str(p.get("utc_date", ""))[:10]
                status = p.get("status", "SCHEDULED")
                h_score, a_score = p.get("home_score"), p.get("away_score")
                clock_str = p.get("display_clock") or ""

                if status in ["IN_PLAY", "PAUSED", "LIVE"]:
                    fixture_msg = f"Live fixture active: [{comp_name}] {h_team} {h_score if h_score is not None else 0} - {a_score if a_score is not None else 0} {a_team} (Clock: {clock_str or 'Live'})"
                    append_event(
                        log_events,
                        fixture_msg,
                        "INFO",
                        "MATCH_SYNC",
                        "FIXTURE_LIVE",
                        match_id=p['id'],
                        attributes={
                            'status': status,
                            'home': h_team,
                            'away': a_team,
                            'home_score': h_score,
                            'away_score': a_score,
                            'display_clock': clock_str,
                            'competition': comp_name,
                            'utc_date': p.get('utc_date'),
                        },
                    )

                if old:
                    for field in ["formations", "statistics", "editorial_headline", "events"]:
                        if not p.get(field) and old.get(field):
                            p[field] = old[field]

                    is_regression = (
                        (old["status"] in ["FINISHED", "AWARDED"] and p["status"] not in ["FINISHED", "AWARDED"]) or
                        (old["status"] in ["IN_PLAY", "PAUSED"] and p["status"] in ["TIMED", "SCHEDULED"])
                    )

                    match_attrs = {
                        "competition": comp_name,
                        "home": h_team,
                        "away": a_team,
                        "status": p.get("status"),
                        "home_score": p.get("home_score"),
                        "away_score": p.get("away_score"),
                    }

                    if is_regression:
                        append_event(log_events, f"[STATUS PROTECTED] [{comp_name}] {h_team} vs {a_team} status preserved as {old['status']}", "WARN", "MATCH_SYNC", "REGRESSION", match_id=p["id"], attributes=match_attrs)
                        p["status"] = old["status"]
                        for score_field in ["home_score", "away_score", "home_penalties", "away_penalties"]:
                            if p.get(score_field) is None:
                                p[score_field] = old.get(score_field)
                    elif old["status"] != p["status"]:
                        if p["status"] in ["FINISHED", "AWARDED"]:
                            self.newly_finished_count += 1
                            pen_str = f" ({p.get('home_penalties')}-{p.get('away_penalties')} pen)" if p.get("home_penalties") is not None else ""
                            append_event(log_events, f"[FULL TIME] [{comp_name}] ({m_date}) {h_team} vs {a_team} finished: {p.get('home_score') or 0} - {p.get('away_score') or 0}{pen_str}", "INFO", "MATCH_SYNC", "FULL_TIME", match_id=p["id"], attributes=match_attrs)
                            if not durable:
                                notification_service.notify_match_event(p, "fulltime", log_events)
                            finished_match_dispatches.append({
                                "match_id": p["id"],
                                "home": h_team,
                                "away": a_team,
                                "competition": comp_name,
                            })
                        elif p["status"] == "IN_PLAY":
                            if old["status"] == "PAUSED":
                                append_event(log_events, f"[SECOND HALF] [{comp_name}] ({m_date}) 2nd half underway for {h_team} vs {a_team}", "INFO", "MATCH_SYNC", "SECOND_HALF", match_id=p["id"], attributes=match_attrs)
                            else:
                                append_event(log_events, f"[KICKOFF] [{comp_name}] ({m_date}) {h_team} vs {a_team} kicked off (Status: IN_PLAY)", "INFO", "MATCH_SYNC", "KICKOFF", match_id=p["id"], attributes=match_attrs)
                                if not durable:
                                    notification_service.notify_match_event(p, "kickoff", log_events)
                        elif p["status"] == "PAUSED":
                            append_event(log_events, f"[HALF TIME] [{comp_name}] ({m_date}) Half time break: {h_team} {p.get('home_score') or 0} - {p.get('away_score') or 0} {a_team}", "INFO", "MATCH_SYNC", "HALF_TIME", match_id=p["id"], attributes=match_attrs)
                        else:
                            append_event(log_events, f"[STATUS UPDATE] [{comp_name}] ({m_date}) {h_team} vs {a_team} status changed: {old['status']} -> {p['status']}", "INFO", "MATCH_SYNC", "STATUS_CHANGE", match_id=p["id"], attributes=match_attrs)
                        changes_found = True

                    if p["status"] in ["IN_PLAY", "PAUSED", "FINISHED"]:
                        h_old, a_old = old.get("home_score") or 0, old.get("away_score") or 0
                        h_new, a_new = p.get("home_score") or 0, p.get("away_score") or 0
                        if h_old != h_new or a_old != a_new:
                            append_event(log_events, f"[LIVE SCORE] [{comp_name}] ({m_date}) Score updated for {h_team} vs {a_team}: {h_new} - {a_new}", "SUCCESS", "MATCH_SYNC", "GOAL_SCORE", match_id=p["id"], attributes=match_attrs)
                            if p["status"] in ["IN_PLAY", "PAUSED"] and not durable:
                                notification_service.notify_match_event(p, "score", log_events)
                            changes_found = True
                else:
                    changes_found = True
                    kickoff_time = p.get("utc_date", "")[11:16] + " UTC" if len(p.get("utc_date", "")) >= 16 else "TBD"
                    if p["status"] in ["TIMED", "SCHEDULED"]:
                        append_event(
                            log_events,
                            f"[NEW FIXTURE] [{comp_name}] ({m_date}) Inserted upcoming fixture: {h_team} vs {a_team} (Kickoff: {kickoff_time})",
                            "INFO",
                            "MATCH_SYNC",
                            "NEW_MATCH",
                            match_id=p["id"],
                            attributes={
                                "competition": comp_name,
                                "home": h_team,
                                "away": a_team,
                                "utc_date": p.get("utc_date"),
                                "status": p.get("status"),
                                "round": p.get("matchday") or p.get("round") or "",
                            },
                        )

            enriched_count = await enrich_tactical_data(unique_payloads, old_data_map, log_events)
            if not is_live_ticker:
                await enrich_editorial_articles(unique_payloads, log_events)

            if not changes_found and enriched_count == 0:
                append_event(log_events, "No fixture score or status changes detected.", "INFO", "MATCH_SYNC", "NO_CHANGE")

            if not durable:
                notification_service.check_upcoming_fixtures(unique_payloads, log_events)

            try:
                supabase_client.table("matches").upsert(unique_payloads, on_conflict="id").execute()
            except Exception as upsert_err:
                err_str = str(upsert_err).lower()
                if "events" in err_str and ("column" in err_str or "exist" in err_str or "schema" in err_str or "pgrst204" in err_str):
                    stripped = [{k: v for k, v in p.items() if k != "events"} for p in unique_payloads]
                    supabase_client.table("matches").upsert(stripped, on_conflict="id").execute()
                else:
                    raise upsert_err

            if finished_match_dispatches:
                try:
                    from worker.clients.cloud_tasks_client import cloud_tasks_client
                    for f_match in finished_match_dispatches:
                        cloud_tasks_client.dispatch_highlight_task(
                            match_id=f_match["match_id"],
                            home=f_match["home"],
                            away=f_match["away"],
                            competition=f_match["competition"],
                            schedule_delay_seconds=10,
                        )
                        cloud_tasks_client.dispatch_highlight_task(
                            match_id=f_match["match_id"],
                            home=f_match["home"],
                            away=f_match["away"],
                            competition=f_match["competition"],
                            schedule_delay_seconds=5400,
                        )
                except Exception as task_err:
                    logger.debug(f"Cloud Task dispatch notice: {task_err}")

            msg_upsert = f"Successfully synced {len(unique_payloads)} matches to Supabase (Enriched {enriched_count} tactical profiles)."
            logger.info(msg_upsert)
            append_event(log_events, msg_upsert, "SUCCESS", "MATCH_SYNC", "DB_UPSERT")
            return unique_payloads
        except Exception as e:
            append_event(log_events, f"Database error during match upsert: {e}", "ERROR", "MATCH_SYNC", "ERROR")
            logger.error(f"Database error during match upsert: {e}")
            return []


match_service = MatchService()
