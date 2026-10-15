import os
import sys
from datetime import datetime, UTC
from pathlib import Path

sys.path.insert(0, str(Path.cwd()))
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

from worker.clients.supabase_client import supabase_client

CORRECTIONS = {
    564702: {
        "name": "Real Madrid vs Villarreal",
        "video_id": "_sZK1UhrlFI",
        "region_restriction": None,
        "youtube_region_info": None,
    },
    560595: {
        "name": "Chelsea vs Bournemouth",
        "video_id": "06dvvp2ORLM",
        "region_restriction": None,
        "youtube_region_info": None,
    },
    560594: {
        "name": "Hull City vs Everton",
        "video_id": "nSkWXRgkjCI",
        "region_restriction": None,
        "youtube_region_info": None,
    },
    559670: {
        "name": "OGC Nice vs Strasbourg",
        "video_id": "V1lQ8tGKw5c",
        "region_restriction": {"blocked": ["FR"]},
        "youtube_region_info": {"blocked": ["FR"]},
    },
    560592: {
        "name": "Sunderland vs Brighton",
        "video_id": "B6PiVZOGOXc",
        "region_restriction": None,
        "youtube_region_info": None,
    },
    558579: {
        "name": "Atalanta vs Venezia",
        "video_id": "8hoi54Z4R-k",
        "region_restriction": {"allowed": ["IT"]},
        "youtube_region_info": {"allowed": ["IT"]},
    },
    560593: {
        "name": "Arsenal vs Leeds United",
        "video_id": "p2kZ0O4IJg4",
        "region_restriction": None,
        "youtube_region_info": None,
    },
    559669: {
        "name": "RC Lens vs Lyon",
        "video_id": "As854ShtlKg",
        "region_restriction": {"blocked": ["FR"]},
        "youtube_region_info": {"blocked": ["FR"]},
    },
    558582: {
        "name": "Inter vs Parma",
        "video_id": "3HMXVBRxnXw",
        "region_restriction": {"blocked": ["IT"]},
        "youtube_region_info": {"blocked": ["IT"]},
    },
}

def apply_corrections():
    now_iso = datetime.now(UTC).isoformat()
    for match_id, data in CORRECTIONS.items():
        payload = {
            "highlight_video_id": data["video_id"],
            "last_youtube_check": now_iso,
            "region_restriction": data["region_restriction"],
            "youtube_region_info": data["youtube_region_info"],
            "updated_at": now_iso,
        }
        res = (
            supabase_client.table("matches")
            .update(payload)
            .eq("id", match_id)
            .execute()
        )
        print(f"Updated {match_id} ({data['name']}) -> {data['video_id']}")

if __name__ == "__main__":
    apply_corrections()
