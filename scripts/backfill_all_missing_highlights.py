import os
import sys
from datetime import datetime, UTC

# Ensure worker modules are discoverable
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "worker"))

from clients.supabase_client import supabase_client
from services.highlight_service import HighlightService, COMPETITION_PRIORITY


def backfill_all_missing_highlights(max_matches: int = 250):
    print("=" * 70)
    print("STARTING FULL HIGHLIGHT BACKFILL SWEEP ACROSS MISSING FIXTURES")
    print("=" * 70)

    hs = HighlightService()
    quota = hs.get_safe_quota_remaining()
    print(f"Current YouTube API safe quota remaining: {quota} units")

    # Fetch all finished matches in current season without highlights
    res = (
        supabase_client.table("matches")
        .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, last_youtube_check, highlight_video_id, youtube_region_info, home_score, away_score, season")
        .eq("status", "FINISHED")
        .is_("highlight_video_id", "null")
        .gte("season", 2026)
        .order("utc_date", desc=True)
        .limit(max_matches)
        .execute()
    )

    unlinked_matches = res.data or []
    print(f"Found {len(unlinked_matches)} unlinked finished matches in season >= 2026.")

    if not unlinked_matches:
        print("No unlinked finished matches found. Exiting.")
        return

    # Sort matches by competition priority
    def sort_key(m):
        c_code = m.get("competition_id") or ""
        prio = COMPETITION_PRIORITY.get(c_code, 99)
        utc = m.get("utc_date") or ""
        return (prio, utc)

    unlinked_matches.sort(key=sort_key)

    linked_count = 0
    not_found_count = 0
    claimed_ids = set()

    for idx, match in enumerate(unlinked_matches, 1):
        m_id = match["id"]
        home = match.get("home_team_name")
        away = match.get("away_team_name")
        comp = match.get("competition_name") or match.get("competition_id")
        utc = match.get("utc_date", "")[:10]

        print(f"\n[{idx}/{len(unlinked_matches)}] Checking ({comp}) {home} vs {away} [{utc}] (ID: {m_id})...")

        logs = []
        try:
            vid, channel, reg, score, cands = hs.search_highlight_for_match(
                match,
                log_events=logs,
                claimed_video_ids=claimed_ids,
                allow_search_api=True,
            )

            now_iso = datetime.now(UTC).isoformat()
            if vid:
                claimed_ids.add(vid)
                update_data = {
                    "highlight_video_id": vid,
                    "last_youtube_check": now_iso,
                    "updated_at": now_iso,
                }
                if reg:
                    update_data["youtube_region_info"] = reg
                supabase_client.table("matches").update(update_data).eq("id", m_id).execute()
                print(f"  -> SUCCESS: Linked {vid} from {channel} (Score: {score})")
                linked_count += 1
            else:
                supabase_client.table("matches").update({
                    "last_youtube_check": now_iso,
                }).eq("id", m_id).execute()
                print("  -> NOT FOUND: No official video available yet.")
                not_found_count += 1
        except Exception as e:
            print(f"  -> ERROR during search: {e}")

    print("\n" + "=" * 70)
    print(f"BACKFILL SWEEP COMPLETED: {linked_count} Newly Linked | {not_found_count} Not Found Yet")
    print("=" * 70)


if __name__ == "__main__":
    backfill_all_missing_highlights()
