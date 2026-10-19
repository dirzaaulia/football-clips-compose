from datetime import UTC, datetime, timedelta

from loguru import logger
from worker.clients.espn_client import espn_client
from worker.clients.football_data_client import football_data_client
from worker.clients.supabase_client import supabase_client
from worker.clients.supabase_cloud_client import supabase_cloud_client
from worker.config.settings import settings
from worker.services.club_service import club_service
from worker.services.editorial_rss_service import editorial_rss_service
from worker.services.notification_service import notification_service
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

    async def sync_matches(self, is_full_sync: bool = False, log_events: list | None = None) -> list[dict]:
        if log_events is None:
            log_events = []

        now_utc = datetime.now(UTC)
        provider = (settings.match_data_provider or "espn").lower()
        provider_name = "ESPN Live Engine" if provider == "espn" else "Football-Data API"

        mode_text = (
            f"FULL SYNC [{provider_name}] (H-{settings.retention_days} s/d H+{settings.upcoming_fixture_days})"
            if is_full_sync
            else f"LIVE SYNC [{provider_name}] (H-1 to H+7)"
        )
        logger.info(f"Sync Mode: {mode_text}")
        append_event(log_events, f"Sync Mode: {mode_text}", "INFO", "MATCH_SYNC", "MODE")

        if is_full_sync:
            date_from = (now_utc - timedelta(days=settings.retention_days)).strftime("%Y-%m-%d")
            date_to = (now_utc + timedelta(days=settings.upcoming_fixture_days)).strftime("%Y-%m-%d")
        else:
            date_from = (now_utc - timedelta(days=1)).strftime("%Y-%m-%d")
            date_to = (now_utc + timedelta(days=7)).strftime("%Y-%m-%d")

        if provider == "espn":
            matches_raw = await espn_client.get_matches(
                competitions=settings.target_competitions,
                date_from=date_from,
                date_to=date_to,
            )
        else:
            fd_comps = [c for c in settings.target_competitions if c in ["PL", "PD", "SA", "BL1", "FL1", "CL", "EL"]]
            matches_raw = await football_data_client.get_matches(
                competitions=fd_comps,
                date_from=date_from,
                date_to=date_to,
            )

        if not matches_raw:
            append_event(log_events, f"{provider_name}: No matches received for sync window.", "WARN", "MATCH_SYNC", "EMPTY_API")
            return []

        msg_fetch = f"Ingested {len(matches_raw)} fixtures from {provider_name} ({date_from} to {date_to})"
        logger.info(msg_fetch)
        append_event(log_events, msg_fetch, "INFO", "MATCH_SYNC", "API_FETCH")

        # Granular Competition Breakdown
        from collections import Counter
        comp_counts = Counter(m.get("competition_id") or m.get("competition", {}).get("code") or "OTHER" for m in matches_raw)
        breakdown_str = ", ".join(f"{k}: {v}" for k, v in sorted(comp_counts.items()))
        append_event(log_events, f"[INGEST BREAKDOWN] {breakdown_str}", "INFO", "MATCH_SYNC", "BREAKDOWN")

        # Proximity alert for upcoming kickoffs within 45 mins
        upcoming_near = []
        for m in matches_raw:
            m_date = self.safe_parse_iso(m.get("utc_date") or m.get("utcDate"))
            diff_min = (m_date - now_utc).total_seconds() / 60
            if 0 <= diff_min <= 45 and m.get("status") in ["TIMED", "SCHEDULED"]:
                h_name = m.get("home_team_name") or m.get("homeTeam", {}).get("name", "Home")
                a_name = m.get("away_team_name") or m.get("awayTeam", {}).get("name", "Away")
                upcoming_near.append(f"{h_name} vs {a_name} ({int(diff_min)}m)")
        if upcoming_near:
            append_event(log_events, f"[KICKOFF UPCOMING] Starting soon: {', '.join(upcoming_near)}", "INFO", "MATCH_SYNC", "KICKOFF_NEAR")

        # 1. Auto-sync supported clubs dari 5 liga besar (Zero Maintenance across seasons)
        club_service.sync_clubs_from_matches(matches_raw, log_events)

        payload_batch = []
        api_match_ids = []

        for m in matches_raw:
            if provider == "espn":
                m["updated_at"] = now_utc.isoformat()
                payload_batch.append(m)
                api_match_ids.append(m["id"])
            else:
                comp_name = m.get("competition", {}).get("name", "Unknown")
                if comp_name == "Primera Division":
                    comp_name = "LaLiga"

                match_start = self.safe_parse_iso(m.get("utcDate"))
                elapsed_minutes = (now_utc - match_start).total_seconds() / 60
                api_status = m.get("status", "SCHEDULED")

                # Fallback otomatis jika status IN_PLAY tapi sudah lewat 140 menit
                if api_status in ["IN_PLAY", "PAUSED"] and elapsed_minutes > 140:
                    api_status = "FINISHED"

                matchday = m.get("matchday")
                payload = {
                    "id": m["id"],
                    "competition_id": m.get("competition", {}).get("code", "OTHER"),
                    "competition_name": comp_name,
                    "utc_date": m.get("utcDate"),
                    "status": api_status,
                    "matchday": matchday,
                    "round_name": f"Matchweek {matchday}" if matchday else None,
                    "display_clock": None,
                    "home_team_id": m.get("homeTeam", {}).get("id"),
                    "home_team_name": m.get("homeTeam", {}).get("name", ""),
                    "home_team_crest": m.get("homeTeam", {}).get("crest"),
                    "away_team_id": m.get("awayTeam", {}).get("id"),
                    "away_team_name": m.get("awayTeam", {}).get("name", ""),
                    "away_team_crest": m.get("awayTeam", {}).get("crest"),
                    "home_score": m.get("score", {}).get("fullTime", {}).get("home"),
                    "away_score": m.get("score", {}).get("fullTime", {}).get("away"),
                    "provider": "football_data",
                    "updated_at": now_utc.isoformat()
                }
                payload_batch.append(payload)
                api_match_ids.append(m["id"])

        unique_payloads = list({p['id']: p for p in payload_batch}.values())
        if not unique_payloads:
            return []

        # 2. Status & Score Regression Protection
        try:
            old_data_map = {}
            select_cols = "id, status, home_score, away_score, formations, statistics, editorial_headline, events"
            for i in range(0, len(api_match_ids), 50):
                try:
                    res_db = supabase_client.table("matches").select(select_cols).in_("id", api_match_ids[i:i+50]).execute()
                except Exception as sel_err:
                    if "events" in str(sel_err).lower():
                        select_cols = "id, status, home_score, away_score, formations, statistics, editorial_headline"
                        res_db = supabase_client.table("matches").select(select_cols).in_("id", api_match_ids[i:i+50]).execute()
                    else:
                        raise sel_err
                for row in res_db.data:
                    old_data_map[row["id"]] = row

            changes_found = False
            for p in unique_payloads:
                old = old_data_map.get(p["id"])
                h_team, a_team = p["home_team_name"], p["away_team_name"]

                comp_name = p.get("competition_name") or p.get("competition_id") or "League"
                m_date = str(p.get("utc_date", ""))[:10]

                if old:
                    # Retain existing tactical data if already present and not overwritten
                    if not p.get("formations") and old.get("formations"):
                        p["formations"] = old["formations"]
                    if not p.get("statistics") and old.get("statistics"):
                        p["statistics"] = old["statistics"]
                    if not p.get("editorial_headline") and old.get("editorial_headline"):
                        p["editorial_headline"] = old["editorial_headline"]
                    if not p.get("events") and old.get("events"):
                        p["events"] = old["events"]

                    # Protection against regression from FINISHED back to IN_PLAY/TIMED
                    is_regression = False
                    if old["status"] in ["FINISHED", "AWARDED"] and p["status"] not in ["FINISHED", "AWARDED"] or old["status"] in ["IN_PLAY", "PAUSED"] and p["status"] in ["TIMED", "SCHEDULED"]:
                        is_regression = True

                    if is_regression:
                        append_event(log_events, f"[REGRESSION_BLOCKED] [{comp_name}] ({m_date}) {h_team} vs {a_team} status retained as {old['status']}", "WARN", "MATCH_SYNC", "REGRESSION")
                        p["status"] = old["status"]
                        if p.get("home_score") is None:
                            p["home_score"] = old.get("home_score")
                        if p.get("away_score") is None:
                            p["away_score"] = old.get("away_score")
                    elif old["status"] != p["status"]:
                        if p["status"] in ["FINISHED", "AWARDED"]:
                            self.newly_finished_count += 1
                            append_event(log_events, f"[FULL TIME] [{comp_name}] ({m_date}) {h_team} vs {a_team} finished: {p.get('home_score') or 0} - {p.get('away_score') or 0}", "INFO", "MATCH_SYNC", "FULL_TIME")
                            notification_service.notify_match_event(p, "fulltime", log_events)
                        elif p["status"] == "IN_PLAY":
                            if old["status"] == "PAUSED":
                                append_event(log_events, f"[SECOND HALF] [{comp_name}] ({m_date}) 2nd half underway for {h_team} vs {a_team}", "INFO", "MATCH_SYNC", "SECOND_HALF")
                            else:
                                append_event(log_events, f"[KICKOFF] [{comp_name}] ({m_date}) {h_team} vs {a_team} kicked off (Status: IN_PLAY)", "INFO", "MATCH_SYNC", "KICKOFF")
                                notification_service.notify_match_event(p, "kickoff", log_events)
                        elif p["status"] == "PAUSED":
                            append_event(log_events, f"[HALF TIME] [{comp_name}] ({m_date}) Half time break: {h_team} {p.get('home_score') or 0} - {p.get('away_score') or 0} {a_team}", "INFO", "MATCH_SYNC", "HALF_TIME")
                        else:
                            append_event(log_events, f"[STATUS UPDATE] [{comp_name}] ({m_date}) {h_team} vs {a_team} status changed: {old['status']} -> {p['status']}", "INFO", "MATCH_SYNC", "STATUS_CHANGE")
                        changes_found = True

                    if p["status"] in ["IN_PLAY", "PAUSED", "FINISHED"]:
                        h_score_old, a_score_old = old.get("home_score") or 0, old.get("away_score") or 0
                        h_score_new, a_score_new = p.get("home_score") or 0, p.get("away_score") or 0
                        if h_score_old != h_score_new or a_score_old != a_score_new:
                            append_event(log_events, f"[LIVE SCORE] [{comp_name}] ({m_date}) Score updated for {h_team} vs {a_team}: {h_score_new} - {a_score_new}", "SUCCESS", "MATCH_SYNC", "GOAL_SCORE")
                            if p["status"] in ["IN_PLAY", "PAUSED"]:
                                notification_service.notify_match_event(p, "score", log_events)
                            changes_found = True
                else:
                    changes_found = True
                    kickoff_time = p.get("utc_date", "")[11:16] + " UTC" if len(p.get("utc_date", "")) >= 16 else "TBD"
                    if p["status"] in ["TIMED", "SCHEDULED"]:
                        append_event(log_events, f"[NEW FIXTURE] [{comp_name}] ({m_date}) Inserted upcoming fixture: {h_team} vs {a_team} (Kickoff: {kickoff_time})", "INFO", "MATCH_SYNC", "NEW_MATCH")
                    else:
                        append_event(log_events, f"[NEW FIXTURE] [{comp_name}] ({m_date}) Inserted fixture: {h_team} vs {a_team} (Status: {p['status']})", "INFO", "MATCH_SYNC", "NEW_MATCH")

            # 3. Enrich live and newly finished matches with real tactical data (Formations & Stats)
            enriched_count = 0
            max_enrich_per_cycle = 5
            for p in unique_payloads:
                p.setdefault("season", 2026)
                if provider != "espn":
                    continue
                is_live = p.get("status") in ["IN_PLAY", "PAUSED"]
                old = old_data_map.get(p["id"])
                is_newly_finished = old and old.get("status") != p.get("status") and p.get("status") in ["FINISHED", "AWARDED"]
                needs_stats = p.get("status") in ["FINISHED", "AWARDED"] and (not old or not old.get("statistics") or not old.get("formations"))

                if is_live or is_newly_finished or (needs_stats and enriched_count < max_enrich_per_cycle):
                    try:
                        summary = await espn_client.get_match_summary(p["id"], p.get("competition_id", "PL"))
                        if summary:
                            if summary.get("formations"):
                                p["formations"] = summary["formations"]
                            if summary.get("statistics"):
                                p["statistics"] = summary["statistics"]
                            if summary.get("editorial_headline"):
                                p["editorial_headline"] = summary["editorial_headline"]
                            if summary.get("editorial_story"):
                                p["editorial_story"] = summary["editorial_story"]
                            if summary.get("editorial_source"):
                                p["editorial_source"] = summary["editorial_source"]
                            if summary.get("events"):
                                p["events"] = summary["events"]
                            enriched_count += 1
                            f_home = summary.get("formations", {}).get("home", "")
                            f_away = summary.get("formations", {}).get("away", "")
                            form_str = f" ({f_home} vs {f_away})" if f_home and f_away else ""
                            append_event(log_events, f"[TACTICAL DATA] [{comp_name}] Enriched formations{form_str} & boxscore stats for {h_team} vs {a_team}", "INFO", "MATCH_SYNC", "TACTICAL_SYNC")
                    except Exception as ex:
                        logger.debug(f"[TACTICAL DATA] Could not fetch summary for {p['id']}: {ex}")

            # 4. Multi-source journalistic enrichment via BBC Sport & The Guardian RSS for match analysis & previews
            try:
                rss_articles = await editorial_rss_service.get_articles()
                if rss_articles:
                    for p in unique_payloads:
                        if not p.get("editorial_story"):
                            h_team = p.get("home_team_name", "")
                            a_team = p.get("away_team_name", "")
                            art = editorial_rss_service.match_article_for_fixture(h_team, a_team, rss_articles)
                            if art:
                                p["editorial_headline"] = art["headline"]
                                p["editorial_story"] = art["story"]
                                p["editorial_source"] = art["source"]
                                comp_name = p.get("competition_name") or p.get("competition_id") or "League"
                                append_event(log_events, f"[EDITORIAL RSS] [{comp_name}] Linked {art['source']} journalistic story for {h_team} vs {a_team}", "INFO", "MATCH_SYNC", "EDITORIAL_SYNC")
            except Exception as e:
                logger.debug(f"[EDITORIAL RSS] Multi-source enrichment skipped: {e}")

            if not changes_found and enriched_count == 0:

                append_event(log_events, "No fixture score or status changes detected.", "INFO", "MATCH_SYNC", "NO_CHANGE")

            notification_service.check_upcoming_fixtures(unique_payloads, log_events)

            try:
                supabase_client.table("matches").upsert(unique_payloads, on_conflict="id").execute()
            except Exception as upsert_err:
                err_str = str(upsert_err).lower()
                if "events" in err_str and ("column" in err_str or "exist" in err_str or "schema" in err_str or "pgrst204" in err_str):
                    logger.warning("[MATCH SYNC] matches.events column not present on target schema; retrying upsert without events payload...")
                    stripped = [{k: v for k, v in p.items() if k != "events"} for p in unique_payloads]
                    supabase_client.table("matches").upsert(stripped, on_conflict="id").execute()
                else:
                    raise upsert_err

            msg_upsert = f"Successfully synced {len(unique_payloads)} fixtures to Supabase database (Enriched {enriched_count} tactical profiles)."
            logger.info(msg_upsert)
            append_event(log_events, msg_upsert, "SUCCESS", "MATCH_SYNC", "DB_UPSERT")

            # Dual-write to Legacy Supabase Cloud for backward compatibility
            supabase_cloud_client.sync_matches(unique_payloads)

            return unique_payloads
        except Exception as e:
            append_event(log_events, f"Database error during fixture upsert: {e}", "ERROR", "MATCH_SYNC", "ERROR")
            logger.error(f"Database error during match upsert: {e}")
            return []

match_service = MatchService()
