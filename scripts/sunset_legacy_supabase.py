import os
from supabase import create_client
from dotenv import load_dotenv

load_dotenv('worker/.env')
url = os.getenv('SUPABASE_URL')  # https://eiomktvavndorazreyba.supabase.co
key = os.getenv('SUPABASE_SERVICE_ROLE_KEY')

if not url or not key:
    raise ValueError("Missing SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY")

sb = create_client(url, key)

notice_match = {
    "id": 999999999,
    "utc_date": "2099-01-01T00:00:00Z",
    "status": "FINISHED",
    "home_team_id": 999991,
    "home_team_name": "⚠️ UPDATE REQUIRED",
    "home_team_crest": "https://fc.dirzaaulia.com/favicon.ico",
    "away_team_id": 999992,
    "away_team_name": "FootballClips v3.0 Released",
    "away_team_crest": "https://fc.dirzaaulia.com/favicon.ico",
    "home_score": 3,
    "away_score": 0,
    "competition_id": "PL",
    "competition_name": "🚨 App Update Notice",
    "editorial_headline": "Your version of FootballClips is discontinued! Update now to continue.",
    "editorial_story": (
        "FootballClips has upgraded to version 3.0 with higher quality highlights, new leagues, and faster streaming.\n\n"
        "📱 Update via Google Play: https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips\n\n"
        "🌐 Or download direct APK at: https://fc.dirzaaulia.com"
    ),
    "editorial_source": "FootballClips Team",
    "season": 2026,
    "highlight_video_id": "dQw4w9WgXcQ",
}

print(f"Connecting to legacy Supabase at {url}...")
res = sb.table("matches").upsert(notice_match).execute()
print(f"Successfully pinned migration notice in matches table: {res.data}")
