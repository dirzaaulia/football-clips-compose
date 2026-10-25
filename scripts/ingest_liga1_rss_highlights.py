import os
import sys
import re
import httpx
import xml.etree.ElementTree as ET
from datetime import datetime, UTC
from pathlib import Path

sys.path.insert(0, str(Path.cwd()))

from worker.clients.supabase_client import supabase_client

def parse_teams_from_title(title: str) -> tuple[str, str]:
    # e.g. "Mini Match | Madura United FC vs PERSIB Bandung | Pekan 6"
    clean = re.sub(r"^Mini [Mm]atch\s*\|\s*", "", title, flags=re.IGNORECASE)
    clean = re.sub(r"\s*\|\s*Pekan\s*\d+.*$", "", clean, flags=re.IGNORECASE)
    parts = re.split(r"\s+vs\.?\s+", clean, flags=re.IGNORECASE)
    if len(parts) >= 2:
        return parts[0].strip(), parts[1].strip()
    return title, ""

def ingest_liga1_highlights():
    print("=== 🇮🇩 INGESTING REAL LIGA 1 INDONESIA MATCH HIGHLIGHTS ===")
    rss_url = "https://www.youtube.com/feeds/videos.xml?channel_id=UCyJejTAHiCrIRqWmFrbgQfQ"
    r = httpx.get(rss_url, timeout=10.0)
    if r.status_code != 200:
        print(f"❌ Failed to fetch RSS feed: {r.status_code}")
        return

    root = ET.fromstring(r.text)
    ns = {'atom': 'http://www.w3.org/2005/Atom', 'yt': 'http://www.youtube.com/xml/schemas/2015'}
    entries = root.findall('atom:entry', ns)
    print(f"Found {len(entries)} videos from @liga1match RSS feed.")

    ingested_count = 0
    for idx, entry in enumerate(entries, 1):
        title = entry.find('atom:title', ns).text
        video_id = entry.find('yt:videoId', ns).text
        published = entry.find('atom:published', ns).text

        home_team, away_team = parse_teams_from_title(title)
        match_id = 900000000 + idx  # Synthetic ID offset for RSS matches

        payload = {
            "id": match_id,
            "competition_id": "IDN",
            "competition_name": "Liga 1 Indonesia",
            "utc_date": published,
            "status": "FINISHED",
            "home_team_name": home_team,
            "away_team_name": away_team,
            "home_team_crest": "https://a.espncdn.com/i/leaguelogos/soccer/500/2309.png",
            "away_team_crest": "https://a.espncdn.com/i/leaguelogos/soccer/500/2309.png",
            "highlight_video_id": video_id,
            "season": 2026,
            "updated_at": datetime.now(UTC).isoformat()
        }

        try:
            res = supabase_client.table("matches").upsert(payload).execute()
            if res:
                ingested_count += 1
                print(f"  ✅ Saved [{video_id}] {home_team} vs {away_team}")
        except Exception as e:
            print(f"  ⚠️ Error saving match {video_id}: {e}")

    print(f"\n🎉 Successfully ingested {ingested_count} real Liga 1 Indonesia video highlights into PostgreSQL!")

if __name__ == "__main__":
    ingest_liga1_highlights()
