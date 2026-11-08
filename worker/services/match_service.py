"""Match fixture and live score synchronization orchestrator."""

from collections import Counter
from datetime import UTC, datetime, timedelta
import os
from typing import Any

from loguru import logger

from worker.clients.database_client import db_client as supabase_client
from worker.clients.espn_client import espn_client
from worker.config.settings import settings
from worker.services.club_service import club_service
from worker.services.match_state_machine import (
    PERSISTED_MATCH_COLUMNS,
    evaluate_match_transitions,
)
from worker.services.notification_service import notification_service
from worker.services.tactical_enricher import (
    enrich_editorial_articles,
    enrich_tactical_data,
)
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
            cleaned = str(date_str).replace("Z", "+00:00")
            if "+" in cleaned:
                cleaned = cleaned.split("+")[0]
            if "." in cleaned:
                cleaned = cleaned.split(".")[0]
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
                ],
            },
        )

        return await self._process_and_save_matches(matches_raw, log_events, is_live_ticker=False)

    async def _process_and_save_matches(
        self,
        matches_raw: list[dict],
        log_events: list,
        is_live_ticker: bool = False,
    ) -> list[dict]:
        now_utc = datetime.now(UTC)
        durable = os.getenv("FC_FIXTURE_OUTBOX") == "true"

        payload_batch = []
        api_match_ids = []
        for m in matches_raw:
            m["updated_at"] = now_utc.isoformat()
            payload_batch.append(m)
            api_match_ids.append(m["id"])

        unique_payloads = list({p["id"]: p for p in payload_batch}.values())
        if not unique_payloads:
            return []

        try:
            old_data_map: dict[Any, dict[str, Any]] = {}
            select_cols = "id, status, home_score, away_score, home_penalties, away_penalties"
            for i in range(0, len(api_match_ids), 50):
                res_db = supabase_client.table("matches").select(select_cols).in_("id", api_match_ids[i : i + 50]).execute()
                for row in res_db.data:
                    old_data_map[row["id"]] = row

            transition = evaluate_match_transitions(unique_payloads, old_data_map, log_events, durable=durable)
            self.newly_finished_count += transition.newly_finished_delta

            enriched_count = await enrich_tactical_data(unique_payloads, old_data_map, log_events)
            if not is_live_ticker:
                await enrich_editorial_articles(unique_payloads, log_events)

            if not transition.changes_found and enriched_count == 0:
                append_event(log_events, "No fixture score or status changes detected.", "INFO", "MATCH_SYNC", "NO_CHANGE")

            if not durable:
                notification_service.check_upcoming_fixtures(unique_payloads, log_events)

            db_payloads = [
                {k: v for k, v in p.items() if k in PERSISTED_MATCH_COLUMNS}
                for p in unique_payloads
            ]
            supabase_client.table("matches").upsert(db_payloads, on_conflict="id").execute()

            if transition.finished_dispatches:
                try:
                    from worker.clients.cloud_tasks_client import cloud_tasks_client

                    for f_match in transition.finished_dispatches:
                        for delay in (10, 5400):
                            cloud_tasks_client.dispatch_highlight_task(
                                match_id=f_match["match_id"],
                                home=f_match["home"],
                                away=f_match["away"],
                                competition=f_match["competition"],
                                schedule_delay_seconds=delay,
                            )
                except Exception as task_err:
                    logger.debug(f"Cloud Task dispatch notice: {task_err}")

            msg_upsert = f"Successfully synced {len(unique_payloads)} matches to Oracle VM PostgreSQL (Enriched {enriched_count} tactical profiles)."
            logger.info(msg_upsert)
            append_event(log_events, msg_upsert, "SUCCESS", "MATCH_SYNC", "DB_UPSERT")
            return unique_payloads
        except Exception as e:
            append_event(log_events, f"Database error during match upsert: {e}", "ERROR", "MATCH_SYNC", "ERROR")
            logger.error(f"Database error during match upsert: {e}")
            return []


match_service = MatchService()
