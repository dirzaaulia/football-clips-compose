import urllib.request
import json

supabase_url = "https://eiomktvavndorazreyba.supabase.co"
anon_key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVpb21rdHZhdm5kb3JhenJleWJhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1OTQ4ODcsImV4cCI6MjEwMzE3MDg4N30.GhP7TJXMeAEpG8F-9pB_SLNwaXLjIrOJ2LAiPC1kCk4"
headers = {"apikey": anon_key, "Authorization": f"Bearer {anon_key}"}

print("=== CHECKING TIER 1: LEGACY SUPABASE CLOUD ===")
# 1. Highlights
try:
    req = urllib.request.Request(f"{supabase_url}/rest/v1/matches?status=eq.FINISHED&highlight_video_id=not.is.null&order=utc_date.desc&limit=5", headers=headers)
    with urllib.request.urlopen(req) as resp:
        hl_matches = json.loads(resp.read().decode())
        print(f"Supabase Cloud - Latest {len(hl_matches)} Finished Highlights:")
        for m in hl_matches:
            print(f"  [{m.get('utc_date')}] [{m.get('competition_name')}] {m.get('home_team_name')} vs {m.get('away_team_name')}: {m.get('home_score')}-{m.get('away_score')} -> Video: {m.get('highlight_video_id')}")
except Exception as e:
    print(f"Error querying Supabase Cloud highlights: {e}")

# 2. Upcoming fixtures
try:
    req2 = urllib.request.Request(f"{supabase_url}/rest/v1/matches?status=in.(TIMED,SCHEDULED)&order=utc_date.asc&limit=5", headers=headers)
    with urllib.request.urlopen(req2) as resp:
        upcoming = json.loads(resp.read().decode())
        print(f"\nSupabase Cloud - Upcoming {len(upcoming)} Fixtures:")
        for m in upcoming:
            print(f"  [{m.get('utc_date')}] [{m.get('competition_name')}] {m.get('home_team_name')} vs {m.get('away_team_name')}")
except Exception as e:
    print(f"Error querying Supabase Cloud fixtures: {e}")

# 3. Standings
try:
    req3 = urllib.request.Request(f"{supabase_url}/rest/v1/standings?competition_id=eq.PL&order=position.asc&limit=5", headers=headers)
    with urllib.request.urlopen(req3) as resp:
        standings = json.loads(resp.read().decode())
        print(f"\nSupabase Cloud - Premier League Top {len(standings)} Standings:")
        for s in standings:
            print(f"  #{s.get('position')} {s.get('team_name')}: Pld={s.get('played_games')}, Pts={s.get('points')}")
except Exception as e:
    print(f"Error querying Supabase Cloud standings: {e}")

print("\n=== CHECKING TIER 3: PRODUCTION EDGE PROXY (edge.fc.dirzaaulia.com) ===")
edge_url = "https://edge.fc.dirzaaulia.com"
# 1. Highlights
try:
    req = urllib.request.Request(f"{edge_url}/rest/v1/matches?status=eq.FINISHED&highlight_video_id=not.is.null&order=utc_date.desc&limit=5")
    with urllib.request.urlopen(req) as resp:
        hl_matches = json.loads(resp.read().decode())
        print(f"Edge Proxy - Latest {len(hl_matches)} Finished Highlights:")
        for m in hl_matches:
            print(f"  [{m.get('utc_date')}] [{m.get('competition_name')}] {m.get('home_team_name')} vs {m.get('away_team_name')}: {m.get('home_score')}-{m.get('away_score')} -> Video: {m.get('highlight_video_id')}")
except Exception as e:
    print(f"Error querying Edge Proxy highlights: {e}")
