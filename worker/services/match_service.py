from datetime import UTC, datetime, timedelta

from loguru import logger
from worker.clients.football_data_client import football_data_client
from worker.clients.supabase_client import supabase_client
from worker.config.settings import settings
from worker.services.club_service import club_service


class MatchService:

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
        mode_text = (
            f"FULL SYNC (H-{settings.retention_days} s/d H+{settings.upcoming_fixture_days})"
            if is_full_sync
            else "LIVE SYNC (H-1 s/d H+7)"
        )
        logger.info(f"📡 Mode Sinkronisasi: {mode_text}")
        log_events.append(f"📡 Mode Sinkronisasi: {mode_text}")

        if is_full_sync:
            date_from = (now_utc - timedelta(days=settings.retention_days)).strftime("%Y-%m-%d")
            date_to = (now_utc + timedelta(days=settings.upcoming_fixture_days)).strftime("%Y-%m-%d")
        else:
            date_from = (now_utc - timedelta(days=1)).strftime("%Y-%m-%d")
            date_to = (now_utc + timedelta(days=7)).strftime("%Y-%m-%d")

        matches_raw = await football_data_client.get_matches(
            competitions=settings.target_competitions,
            date_from=date_from,
            date_to=date_to
        )

        if not matches_raw:
            log_events.append("⚪ Football-Data: Tidak ada data pertandingan yang diterima.")
            return []

        msg_fetch = f"📥 Menerima {len(matches_raw)} pertandingan dari API Football-Data ({date_from} s/d {date_to})"
        logger.info(msg_fetch)
        log_events.append(msg_fetch)

        # 1. Auto-sync supported clubs dari 5 liga besar (Zero Maintenance across seasons)
        club_service.sync_clubs_from_matches(matches_raw, log_events)

        payload_batch = []
        api_match_ids = []

        for m in matches_raw:
            comp_name = m.get("competition", {}).get("name", "Unknown")
            if comp_name == "Primera Division":
                comp_name = "LaLiga"

            match_start = self.safe_parse_iso(m.get("utcDate"))
            elapsed_minutes = (now_utc - match_start).total_seconds() / 60
            api_status = m.get("status", "SCHEDULED")

            # Fallback otomatis jika status IN_PLAY tapi sudah lewat 140 menit
            if api_status in ["IN_PLAY", "PAUSED"] and elapsed_minutes > 140:
                api_status = "FINISHED"

            payload = {
                "id": m["id"],
                "competition_id": m.get("competition", {}).get("code", "OTHER"),
                "competition_name": comp_name,
                "utc_date": m.get("utcDate"),
                "status": api_status,
                "matchday": m.get("matchday"),
                "home_team_id": m.get("homeTeam", {}).get("id"),
                "home_team_name": m.get("homeTeam", {}).get("name", ""),
                "home_team_crest": m.get("homeTeam", {}).get("crest"),
                "away_team_id": m.get("awayTeam", {}).get("id"),
                "away_team_name": m.get("awayTeam", {}).get("name", ""),
                "away_team_crest": m.get("awayTeam", {}).get("crest"),
                "home_score": m.get("score", {}).get("fullTime", {}).get("home"),
                "away_score": m.get("score", {}).get("fullTime", {}).get("away"),
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
            for i in range(0, len(api_match_ids), 50):
                res_db = supabase_client.table("matches").select("id, status, home_score, away_score").in_("id", api_match_ids[i:i+50]).execute()
                for row in res_db.data:
                    old_data_map[row["id"]] = row

            changes_found = False
            for p in unique_payloads:
                old = old_data_map.get(p["id"])
                h_team, a_team = p["home_team_name"], p["away_team_name"]

                if old:
                    # Protection against regression from FINISHED back to IN_PLAY/TIMED
                    is_regression = False
                    if old["status"] in ["FINISHED", "AWARDED"] and p["status"] not in ["FINISHED", "AWARDED"] or old["status"] in ["IN_PLAY", "PAUSED"] and p["status"] in ["TIMED", "SCHEDULED"]:
                        is_regression = True

                    if is_regression:
                        log_events.append(f"🛡️ [BLOCKED REGRESSION] {h_team} vs {a_team} tetap {old['status']}")
                        p["status"] = old["status"]
                        if p.get("home_score") is None:
                            p["home_score"] = old.get("home_score")
                        if p.get("away_score") is None:
                            p["away_score"] = old.get("away_score")
                    elif old["status"] != p["status"]:
                        if p["status"] in ["FINISHED", "AWARDED"]:
                            log_events.append(f"🏁 [FULL TIME] {h_team} {p.get('home_score') or 0} - {p.get('away_score') or 0} {a_team}")
                        else:
                            log_events.append(f"🔄 [STATUS] {h_team} vs {a_team} | {old['status']} ➔ {p['status']}")
                        changes_found = True

                    if p["status"] in ["IN_PLAY", "PAUSED", "FINISHED"]:
                        h_score_old, a_score_old = old.get("home_score") or 0, old.get("away_score") or 0
                        h_score_new, a_score_new = p.get("home_score") or 0, p.get("away_score") or 0
                        if h_score_old != h_score_new or a_score_old != a_score_new:
                            log_events.append(f"⚽ [SKOR!] {h_team} {h_score_new} - {a_score_new} {a_team}")
                            changes_found = True
                else:
                    changes_found = True
                    log_events.append(f"✨ [NEW MATCH] Ditemukan jadwal baru: {h_team} vs {a_team}")

            if not changes_found:
                log_events.append("⚪ Tidak ada perubahan skor atau status.")

            supabase_client.table("matches").upsert(unique_payloads, on_conflict="id").execute()
            msg_upsert = f"✅ Berhasil sinkronisasi {len(unique_payloads)} jadwal ke Supabase."
            logger.info(msg_upsert)
            log_events.append(msg_upsert)
            return unique_payloads
        except Exception as e:
            log_events.append(f"🚨 [ERROR DATABASE] {e}")
            logger.error(f"Database error saat upsert matches: {e}")
            return []

match_service = MatchService()
