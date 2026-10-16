import asyncio
from datetime import UTC, datetime

import httpx
from loguru import logger
from worker.clients.espn_client import espn_client
from worker.clients.football_data_client import football_data_client
from worker.clients.supabase_client import supabase_client
from worker.config.settings import settings
from worker.utils.telemetry_logger import append_event


class StandingsService:
    # Leagues supported for league tables (Top European Leagues + UEFA Champions League + Global Leagues)
    STANDINGS_COMPETITIONS = ["PL", "PD", "SA", "BL1", "FL1", "CL", "SPL", "MLS", "DED", "PPL"]

    def __init__(self):
        self.last_sync_timestamp: float = 0.0

    async def sync_standings(self, force: bool = False, season: int | None = None, log_events: list | None = None) -> list[dict]:
        """
        Synchronize standings table to Supabase.
        Supports both zero-quota ESPN Standings Engine and Football-Data.org.
        Supports current or historical season (e.g. season=2025).
        Ensures strict row count cap (~132 rows) with zero cost risk to Supabase Free Tier (<30 KB).
        """
        if log_events is None:
            log_events = []

        now_utc = datetime.now(UTC)
        provider = settings.standings_provider.lower()
        season_label = f" (Season {season})" if season else ""
        append_event(log_events, f"[STANDINGS] Starting league standings sync via provider: {provider.upper()}{season_label}...", "INFO", "STANDINGS", "START")
        logger.info(f"[StandingsService] Starting league standings sync via provider: {provider}{season_label}...")

        all_standings_rows = []

        if provider == "espn":
            async with httpx.AsyncClient(timeout=15.0, headers=espn_client.headers) as client:
                tasks = [espn_client.get_standings(comp_code, season=season, client=client) for comp_code in self.STANDINGS_COMPETITIONS]
                results = await asyncio.gather(*tasks, return_exceptions=True)

                for comp_code, res in zip(self.STANDINGS_COMPETITIONS, results, strict=False):
                    if isinstance(res, Exception):
                        err_msg = f"[STANDINGS] Failed to process {comp_code} from ESPN: {res}"
                        append_event(log_events, err_msg, "ERROR", "STANDINGS", "ERROR")
                        logger.error(err_msg)
                        continue

                    rows = res or []
                    all_standings_rows.extend(rows)
                    append_event(log_events, f"[STANDINGS] {comp_code}: {len(rows)} clubs processed via ESPN.", "INFO", "STANDINGS", "COMP_DONE")
        else:
            # Fallback to Football-Data.org
            for comp_code in self.STANDINGS_COMPETITIONS:
                try:
                    data = await football_data_client.get_standings(comp_code)
                    if not data:
                        append_event(log_events, f"[STANDINGS] No standings data available for {comp_code}", "WARN", "STANDINGS", "EMPTY_COMP")
                        continue

                    comp_info = data.get("competition", {})
                    comp_name = comp_info.get("name", comp_code)

                    season_info = data.get("season", {})
                    start_date = season_info.get("startDate", "")
                    try:
                        season_year = int(start_date[:4]) if start_date and len(start_date) >= 4 else now_utc.year
                    except ValueError:
                        season_year = now_utc.year

                    standings_groups = data.get("standings", [])
                    if not standings_groups:
                        append_event(log_events, f"[STANDINGS] Empty standings group for {comp_code}", "INFO", "STANDINGS", "EMPTY_GROUPS")
                        continue

                    comp_rows_count = 0
                    for group in standings_groups:
                        table_type = group.get("type", "TOTAL")
                        if table_type not in ["TOTAL", None] and len(standings_groups) > 1:
                            continue

                        table_entries = group.get("table", [])
                        for item in table_entries:
                            team = item.get("team", {})
                            team_id = team.get("id")
                            if not team_id:
                                continue

                            row_id = f"{comp_code}_{team_id}"
                            payload = {
                                "id": row_id,
                                "competition_code": comp_code,
                                "position": item.get("position", 0),
                                "team_id": team_id,
                                "team_name": team.get("name") or team.get("shortName") or "Unknown",
                                "team_short_name": team.get("shortName"),
                                "crest_url": team.get("crest"),
                                "played_games": item.get("playedGames", 0),
                                "won": item.get("won", 0),
                                "draw": item.get("draw", 0),
                                "lost": item.get("lost", 0),
                                "points": item.get("points", 0),
                                "goals_for": item.get("goalsFor", 0),
                                "goals_against": item.get("goalsAgainst", 0),
                                "goal_difference": item.get("goalDifference", 0),
                                "updated_at": now_utc.isoformat()
                            }
                            all_standings_rows.append(payload)
                            comp_rows_count += 1

                    append_event(log_events, f"[STANDINGS] {comp_name} ({comp_code}): {comp_rows_count} clubs processed.", "INFO", "STANDINGS", "COMP_DONE")
                except Exception as e:
                    err_msg = f"[STANDINGS] Failed to process {comp_code}: {e}"
                    append_event(log_events, err_msg, "ERROR", "STANDINGS", "ERROR")
                    logger.error(err_msg)

        if not all_standings_rows:
            append_event(log_events, "[STANDINGS] No standings rows available for upsert.", "INFO", "STANDINGS", "NO_ROWS")
            return []

        # Deduplicate by row id
        unique_standings = list({row["id"]: row for row in all_standings_rows}.values())

        # Enrich 5-match form guide (W,D,L,W,W) from real finished matches in database
        try:
            for comp_code in self.STANDINGS_COMPETITIONS:
                comp_rows = [r for r in unique_standings if r.get("competition_code") == comp_code]
                if not comp_rows:
                    continue

                res_m = supabase_client.table("matches").select("home_team_name, away_team_name, home_score, away_score, status, utc_date").eq("competition_id", comp_code).in_("status", ["FINISHED", "AWARDED"]).order("utc_date", desc=True).limit(200).execute()
                if not res_m.data:
                    continue

                team_results: dict[str, list[str]] = {}
                for m in res_m.data:
                    h_name = (m.get("home_team_name") or "").strip().lower()
                    a_name = (m.get("away_team_name") or "").strip().lower()
                    h_score = m.get("home_score", 0) or 0
                    a_score = m.get("away_score", 0) or 0

                    if h_score > a_score:
                        h_res, a_res = "W", "L"
                    elif a_score > h_score:
                        h_res, a_res = "L", "W"
                    else:
                        h_res, a_res = "D", "D"

                    if h_name:
                        team_results.setdefault(h_name, [])
                        if len(team_results[h_name]) < 5:
                            team_results[h_name].append(h_res)
                    if a_name:
                        team_results.setdefault(a_name, [])
                        if len(team_results[a_name]) < 5:
                            team_results[a_name].append(a_res)

                for r in comp_rows:
                    # If form is missing or is just a numeric record like '6-0-0', replace with letter form
                    curr_form = r.get("form") or ""
                    if not curr_form or not any(c in curr_form for c in "WDL"):
                        t_name = (r.get("team_name") or "").strip().lower()
                        matched_seq = team_results.get(t_name)
                        if not matched_seq:
                            for k, seq in team_results.items():
                                if k in t_name or t_name in k:
                                    matched_seq = seq
                                    break
                        if matched_seq:
                            # Oldest to newest (left to right) standard form guide
                            r["form"] = ",".join(reversed(matched_seq))
        except Exception as e:
            logger.debug(f"[STANDINGS] Form guide calculation skipped: {e}")

        try:
            # Batch upsert with on_conflict="id" ensures fixed database footprint (<30 KB)
            supabase_client.table("standings").upsert(unique_standings, on_conflict="id").execute()
            self.last_sync_timestamp = now_utc.timestamp()

            # Automatically prune any stale rows for updated competitions that were not included in this fresh sync
            for comp_code in self.STANDINGS_COMPETITIONS:
                comp_rows = [r for r in unique_standings if r.get("competition_code") == comp_code]
                if comp_rows:
                    target_season = comp_rows[0].get("season", season or 2026)
                    try:
                        supabase_client.table("standings").delete().eq("competition_code", comp_code).eq("season", target_season).lt("updated_at", now_utc.isoformat()).execute()
                    except Exception as pe:
                        logger.debug(f"[STANDINGS] Stale prune skipped for {comp_code}: {pe}")

            msg = f"[STANDINGS] Successfully upserted {len(unique_standings)} standings rows to Supabase ({provider.upper()})."
            append_event(log_events, msg, "SUCCESS", "STANDINGS", "DB_UPSERT")
            logger.info(msg)
            return unique_standings
        except Exception as e:
            err_msg = f"[STANDINGS] Database error during standings upsert: {e}"
            append_event(log_events, err_msg, "ERROR", "STANDINGS", "DB_ERROR")
            logger.error(err_msg)
            return []


standings_service = StandingsService()
