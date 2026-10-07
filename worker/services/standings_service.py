from datetime import UTC, datetime

from loguru import logger
from worker.clients.football_data_client import football_data_client
from worker.clients.supabase_client import supabase_client
from worker.config.settings import settings


class StandingsService:
    # Leagues supported on Football-Data.org Free Tier
    STANDINGS_COMPETITIONS = ["PL", "PD", "SA", "BL1", "FL1", "CL"]

    def __init__(self):
        self.last_sync_timestamp: float = 0.0

    async def sync_standings(self, force: bool = False, log_events: list | None = None) -> list[dict]:
        """
        Synchronize standings table from Football-Data.org to Supabase.
        Ensures strict row count cap (~120 rows) with zero cost risk to Supabase Free Tier.
        """
        if log_events is None:
            log_events = []

        now_utc = datetime.now(UTC)
        log_events.append("📊 [STANDINGS] Memulai sinkronisasi klasemen liga...")
        logger.info("[StandingsService] Memulai sinkronisasi klasemen...")

        all_standings_rows = []

        for comp_code in self.STANDINGS_COMPETITIONS:
            try:
                data = await football_data_client.get_standings(comp_code)
                if not data:
                    log_events.append(f"⚠️ [STANDINGS] Tidak ada data klasemen untuk {comp_code}")
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
                    log_events.append(f"⚪ [STANDINGS] Grup klasemen kosong untuk {comp_code}")
                    continue

                # Filter TOTAL table (or take all if tournament group stages like UCL)
                comp_rows_count = 0
                for group in standings_groups:
                    table_type = group.get("type", "TOTAL")
                    # In league formats, ignore HOME/AWAY sub-tables to keep storage ultra-low
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

                log_events.append(f"✅ [STANDINGS] {comp_name} ({comp_code}): {comp_rows_count} klub diproses.")
            except Exception as e:
                err_msg = f"🚨 [STANDINGS] Gagal memproses {comp_code}: {e}"
                log_events.append(err_msg)
                logger.error(err_msg)

        if not all_standings_rows:
            log_events.append("⚪ [STANDINGS] Tidak ada baris klasemen yang siap di-upsert.")
            return []

        # Deduplicate by row id
        unique_standings = list({row["id"]: row for row in all_standings_rows}.values())

        try:
            # Batch upsert with on_conflict="id" ensures fixed database footprint (<30 KB)
            supabase_client.table("standings").upsert(unique_standings, on_conflict="id").execute()
            self.last_sync_timestamp = now_utc.timestamp()
            msg = f"🏆 [STANDINGS] Sukses upsert {len(unique_standings)} baris klasemen ke Supabase."
            log_events.append(msg)
            logger.info(msg)
            return unique_standings
        except Exception as e:
            err_msg = f"🚨 [STANDINGS] Database error saat upsert standings: {e}"
            log_events.append(err_msg)
            logger.error(err_msg)
            return []


standings_service = StandingsService()
