import html
import re
import sys
from datetime import datetime, timedelta
from pathlib import Path

# Ensure root workspace is in sys.path
_pkg_root = str(Path(__file__).resolve().parent.parent)
if _pkg_root not in sys.path:
    sys.path.insert(0, _pkg_root)

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

from worker.clients.supabase_client import supabase_client
from worker.clients.youtube_client import youtube_client
from worker.config.constants import OFFICIAL_LEAGUE_CHANNELS
from worker.services.highlight_service import highlight_service

def is_valid_highlight(match: dict, video: dict) -> tuple[bool, str]:
    home = match.get("home_team_name", "")
    away = match.get("away_team_name", "")
    m_date_str = str(match.get("utc_date", ""))[:10]
    comp_id = match.get("competition_id", "")
    
    title = html.unescape(video.get("title", ""))
    t_lower = title.lower()
    chan = video.get("channel", "")
    pub_str = video.get("publishedAt", "")
    dur_sec = video.get("duration_seconds", 0)

    # 1. Title Filter: check forbidden patterns
    is_forbidden, reason = highlight_service.check_forbidden_title(title)
    if is_forbidden:
        return False, f"Forbidden keyword: {reason}"

    # Extra checks for non-match content
    extra_banned = [
        "goal of the month", "gotm", "gol del mes", "but du mois", "tor des monats",
        "training", "warm up", "inside training", "training ground",
        "press conference", "point presse", "conférence", "interview", "press briefing",
        "behind the scenes", "tunnel cam", "all the angles", "quiz", "challenge",
        "retro", "rewind", "classic match", "hat-trick 20", "gol da ricordare"
    ]
    for b in extra_banned:
        if b in t_lower:
            return False, f"Non-match pattern: '{b}'"

    # 2. Date window check: video MUST be published between match_date - 1d and match_date + 4d
    if pub_str and m_date_str:
        try:
            m_dt = datetime.fromisoformat(m_date_str)
            p_dt = datetime.fromisoformat(pub_str.replace("Z", "+00:00")).replace(tzinfo=None)
            diff_days = (p_dt.date() - m_dt.date()).days
            if diff_days < -1:
                return False, f"Published BEFORE match ({diff_days} days early: {pub_str[:10]} vs match {m_date_str})"
            if diff_days > 5:
                return False, f"Published TOO LATE ({diff_days} days late: {pub_str[:10]} vs match {m_date_str})"
        except Exception as e:
            pass

    # 3. Team name matching
    h_clean = highlight_service.clean_team_name_for_search(home)
    a_clean = highlight_service.clean_team_name_for_search(away)
    
    has_both = (h_clean in t_lower and a_clean in t_lower)
    
    # Is it from a league channel?
    is_league = chan in ["LALIGA EA SPORTS", "Ligue 1 McDonald’s", "Serie A", "Bundesliga"] or comp_id in OFFICIAL_LEAGUE_CHANNELS
    
    if is_league:
        # League channel MUST have both teams in title!
        if not has_both:
            return False, f"League channel video missing both teams (home '{h_clean}', away '{a_clean}')"
    else:
        # Club channel
        h_handle = highlight_service.get_club_handle(home)
        a_handle = highlight_service.get_club_handle(away)
        
        # If it doesn't have both teams, it must at least have the opponent AND an explicit highlight keyword
        if not has_both:
            has_opp = (h_clean in t_lower or a_clean in t_lower)
            has_hl = highlight_service.has_highlight_keyword(title) or highlight_service.has_scoreline(title)
            if not (has_opp and has_hl):
                return False, f"Club video does not mention both teams or lacks highlight keyword/scoreline"

    return True, "Valid official highlight"

def parse_iso8601_duration(dur_str: str) -> int:
    match = re.match(r"PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?", dur_str)
    if not match:
        return 0
    h = int(match.group(1) or 0)
    m = int(match.group(2) or 0)
    s = int(match.group(3) or 0)
    return h * 3600 + m * 60 + s

def main():
    print("=" * 70)
    print("AUDITING ALL LINKED HIGHLIGHTS IN SUPABASE")
    print("=" * 70)

    res = (
        supabase_client.table("matches")
        .select("id, home_team_name, away_team_name, competition_id, competition_name, utc_date, highlight_video_id")
        .not_.is_("highlight_video_id", "null")
        .execute()
    )
    matches = res.data or []
    print(f"Total linked matches in database: {len(matches)}")

    video_ids = [m["highlight_video_id"] for m in matches if m.get("highlight_video_id")]
    
    # Fetch YouTube details in chunks of 50
    details = {}
    for i in range(0, len(video_ids), 50):
        chunk = video_ids[i:i + 50]
        resp = youtube_client.youtube.videos().list(part="snippet,contentDetails", id=",".join(chunk)).execute()
        for item in resp.get("items", []):
            vid = item["id"]
            snippet = item["snippet"]
            dur_str = item["contentDetails"]["duration"]
            dur_sec = parse_iso8601_duration(dur_str)
            details[vid] = {
                "title": snippet["title"],
                "channel": snippet["channelTitle"],
                "publishedAt": snippet["publishedAt"],
                "duration_seconds": dur_sec,
            }

    valid_count = 0
    invalid_matches = []

    for m in matches:
        m_id = m["id"]
        vid = m.get("highlight_video_id")
        home = m.get("home_team_name")
        away = m.get("away_team_name")
        m_date = str(m.get("utc_date", ""))[:10]

        v_info = details.get(vid)
        if not v_info:
            invalid_matches.append((m, "Video deleted or not found on YouTube"))
            continue

        valid, reason = is_valid_highlight(m, v_info)
        if valid:
            valid_count += 1
        else:
            invalid_matches.append((m, v_info, reason))

    print(f"\nAudit Results: {valid_count} Valid, {len(invalid_matches)} Invalid / Wrong Videos")
    print("=" * 70)

    for item in invalid_matches:
        if len(item) == 3:
            m, v_info, reason = item
            print(f"❌ INVALID [ID {m['id']}] {m['home_team_name']} vs {m['away_team_name']} ({str(m['utc_date'])[:10]})")
            print(f"   Reason:   {reason}")
            print(f"   Video:    https://youtu.be/{m['highlight_video_id']}")
            print(f"   Title:    '{v_info['title']}' ({v_info['channel']})")
            print(f"   Published: {v_info['publishedAt'][:10]}")
            print("-" * 70)
        else:
            m, reason = item
            print(f"❌ DELETED [ID {m['id']}] {m['home_team_name']} vs {m['away_team_name']}: {reason}")
            print("-" * 70)

if __name__ == "__main__":
    main()
