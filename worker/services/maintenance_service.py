from datetime import UTC, datetime, timedelta

from loguru import logger
from worker.clients.supabase_client import supabase_client
from worker.clients.supabase_cloud_client import supabase_cloud_client
from worker.clients.youtube_client import youtube_client
from worker.clients.youtube_rss_client import youtube_rss_client
from worker.config.broadcaster_registry import GLOBAL_BROADCASTER_REGISTRY
from worker.config.settings import settings
from worker.utils.telemetry_logger import append_event


class MaintenanceService:

    def cleanup_old_data(self, log_events: list | None = None) -> int:
        # Canonical history is retained. Metadata retention is handled by fc_ops.
        append_event(log_events if log_events is not None else [],
                     "Canonical fixture history retained; no age-based deletion or speculative full-time recovery.",
                     "INFO", "MAINTENANCE", "HISTORY_RETAINED")
        return 0


    def heal_stuck_matches(self, log_events: list | None = None) -> int:
        """Retained compatibility hook; canonical changes require provider verification."""
        return 0

    def purge_invalid_shorts(self, log_events: list | None = None) -> int:
        """Retained compatibility hook; canonical changes require provider verification."""
        return 0

    def deduplicate_matches(self, log_events: list | None = None) -> int:
        """Retained compatibility hook; canonical changes require provider verification."""
        return 0

    def verify_channel_licensees(self, log_events: list | None = None) -> dict[str, str]:
        """Probe registered broadcaster YouTube channels for rights validity and active season freshness.

        Caters to season-breaks (e.g. summer intermission or winter pause) so channels are
        never falsely marked inactive if the league itself had no finished matches.
        """
        if log_events is None:
            log_events = []

        now_utc = datetime.now(UTC)
        current_year = now_utc.year
        thirty_days_ago = (now_utc - timedelta(days=30)).isoformat()
        results = {}

        for b in GLOBAL_BROADCASTER_REGISTRY:
            b_name = b.get("name", "Unknown Broadcaster")
            channel_id = b.get("channel_id")
            end_year = b.get("contract_end_year", 2099)

            # Contract expiry check
            if current_year > end_year:
                b["status"] = "EXPIRED"
                msg = f"[LICENSE EXPIRED] {b_name} contract ended in {end_year}. Tier 4 Global Club fallback active."
                append_event(log_events, msg, "WARN", "LICENSE_PROBE", "CONTRACT_EXPIRED")
                logger.warning(msg)
                results[b["broadcaster_id"]] = "EXPIRED"
                continue

            # Fetch recent videos (0 quota)
            try:
                recent_vids = youtube_rss_client.fetch_channel_recent_videos(channel_id, timeout_seconds=8.0)
                recent_titles = [v.get("snippet", {}).get("title", "").lower() for v in recent_vids]
            except Exception as e:
                logger.debug(f"[License Probe] Could not fetch RSS for {b_name}: {e}")
                results[b["broadcaster_id"]] = b.get("status", "ACTIVE")
                continue

            for comp_id in b.get("competitions", []):
                # Edge case check: Did the competition have finished matches in the last 30 days?
                try:
                    res_matches = (
                        supabase_client.table("matches")
                        .select("id")
                        .eq("competition_id", comp_id)
                        .eq("status", "FINISHED")
                        .gte("utc_date", thirty_days_ago)
                        .limit(5)
                        .execute()
                    )
                    match_count = len(res_matches.data or [])
                except Exception:
                    match_count = 0

                if match_count < 2:
                    # Off-season or intermission: not enough matches to evaluate; retain ACTIVE
                    msg = f"[LICENSE PROBE] {b_name} ({comp_id}): Season break or intermission ({match_count} matches in 30d). Retaining ACTIVE status."
                    append_event(log_events, msg, "INFO", "LICENSE_PROBE", "SEASON_PAUSE")
                    continue

                # In-season check: Did the channel publish any highlights?
                comp_keywords = [comp_id.lower()]
                if comp_id == "PL":
                    comp_keywords.extend(["premier league", "epl"])
                elif comp_id == "CL":
                    comp_keywords.extend(["champions league", "ucl"])
                elif comp_id == "EL":
                    comp_keywords.extend(["europa league", "uel"])
                elif comp_id == "PD":
                    comp_keywords.extend(["laliga", "la liga"])
                elif comp_id == "SA":
                    comp_keywords.extend(["serie a"])
                elif comp_id == "BL1":
                    comp_keywords.extend(["bundesliga"])
                elif comp_id == "FL1":
                    comp_keywords.extend(["ligue 1"])

                has_published = any(any(kw in t for kw in comp_keywords) for t in recent_titles)
                if has_published:
                    msg = f"[LICENSE VERIFIED] {b_name} active for {comp_id} (recent uploads confirmed)."
                    append_event(log_events, msg, "SUCCESS", "LICENSE_PROBE", "ACTIVE")
                else:
                    msg = f"[LICENSE INACTIVE] {b_name} has 0 recent highlights for {comp_id} despite {match_count} active fixtures. Flagged for review."
                    append_event(log_events, msg, "WARN", "LICENSE_PROBE", "INACTIVE_SUSPECT")
                    logger.warning(msg)

            results[b["broadcaster_id"]] = b.get("status", "ACTIVE")

        return results


maintenance_service = MaintenanceService()
