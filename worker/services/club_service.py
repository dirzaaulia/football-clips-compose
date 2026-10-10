import re
from datetime import UTC, datetime

from loguru import logger
from worker.clients.supabase_client import supabase_client
from worker.config.settings import settings
from worker.utils.telemetry_logger import append_event


class ClubService:

    @staticmethod
    def sync_clubs_from_matches(matches_data: list[dict], log_events: list | None = None) -> int:
        """
        Mengekstrak seluruh tim dari 5 liga besar yang ada di jadwal pertandingan
        dan melakukan auto-upsert ke tabel supported_clubs di Supabase.
        """
        if log_events is None:
            log_events = []

        now_utc = datetime.now(UTC).isoformat()
        clubs_map = {}

        for m in matches_data:
            comp_code = m.get("competition", {}).get("code")
            if comp_code in settings.big_five_competitions:
                comp_name = m.get("competition", {}).get("name", "Unknown")
                if comp_name == "Primera Division":
                    comp_name = "LaLiga"

                for side in ["homeTeam", "awayTeam"]:
                    team = m.get(side, {})
                    tid = team.get("id")
                    tname = team.get("name")
                    if tid and tname:
                        slug = re.sub(r'[^a-z0-9]+', '_', tname.lower()).strip('_')
                        short_name = (
                            tname.replace(" FC", "")
                            .replace(" AFC", "")
                            .replace(" CF", "")
                            .replace(" 1909", "")
                            .replace(" 1913", "")[:15]
                        )
                        crest = team.get("crest") or f"https://crests.football-data.org/{tid}.png"

                        clubs_map[slug] = {
                            "id": slug,
                            "team_id": tid,
                            "name": tname,
                            "short_name": short_name,
                            "competition_code": comp_code,
                            "competition_name": comp_name,
                            "crest_url": crest,
                            "is_active": True,
                            "updated_at": now_utc
                        }

        if clubs_map:
            try:
                payloads = list(clubs_map.values())
                supabase_client.table("supported_clubs").upsert(payloads, on_conflict="id").execute()
                append_event(log_events, f"[SUPPORTED_CLUBS] Auto-synced {len(payloads)} top 5 European league clubs to Supabase.", "INFO", "MATCH_SYNC", "SUPPORTED_CLUBS")
                logger.info(f"Auto-synced {len(payloads)} supported clubs to Supabase.")
                return len(payloads)
            except Exception as e:
                append_event(log_events, f"[SUPPORTED_CLUBS] Auto-sync clubs warning: {e}", "WARN", "MATCH_SYNC", "SUPPORTED_CLUBS_ERROR")
                logger.warning(f"Failed to auto-sync supported clubs: {e}")
        return 0

club_service = ClubService()
