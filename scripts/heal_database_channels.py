"""Script to heal channel IDs and seed missing clubs in PostgreSQL."""

import os
import sys
from pathlib import Path

# Ensure project root is in sys.path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from worker.clients.database_client import db_client
from loguru import logger


def heal_database():
    logger.info("Starting PostgreSQL channel and club healing...")

    # 1. Heal MLS competition channel ID
    try:
        db_client.table("competitions").update({
            "youtube_handle": "@MLS",
            "youtube_channel_id": "UCSZbXT5TLLW_i-5W8FZpFsg",
            "backup_channel_handle": "@MLS",
        }).eq("id", "MLS").execute()
        logger.info("Successfully updated competitions[MLS] channel ID to UCSZbXT5TLLW_i-5W8FZpFsg.")
    except Exception as e:
        logger.error(f"Failed to update MLS competition: {e}")

    # 2. Clean out dummy hashes from supported_clubs
    # Dummy hashes were using patterns like 'UC5_7B1x8A0z9v7A5B1x8A0z', 'UCz8A0z7F9v1B8x8z8A0z7F9', etc.
    dummy_patterns = ["8A0z", "7F9v", "B1x8", "UC5_7", "UCw8x9"]
    try:
        clubs_res = db_client.table("supported_clubs").select("id, name, youtube_handle, youtube_channel_id").execute()
        cleaned_count = 0
        for club in (clubs_res.data or []):
            cid = club.get("youtube_channel_id") or ""
            if any(p in cid for p in dummy_patterns):
                db_client.table("supported_clubs").update({
                    "youtube_channel_id": None
                }).eq("id", club["id"]).execute()
                cleaned_count += 1
                logger.info(f"Cleaned dummy channel ID from club {club.get('name')} ({club['id']})")
        logger.info(f"Cleaned {cleaned_count} dummy channel IDs from supported_clubs.")
    except Exception as e:
        logger.error(f"Failed to clean dummy club hashes: {e}")

    # 3. Seed / Update Argentine clubs (Lanús and Gimnasia Mendoza)
    arg_clubs = [
        {
            "id": "lanus",
            "team_id": 16,
            "name": "Lanús",
            "short_name": "Lanús",
            "competition_code": "ARG",
            "competition_name": "Argentine Liga Profesional",
            "youtube_handle": "@clublanusoficial",
            "aliases": ["lanus", "club atletico lanus"],
            "is_active": True,
            "channel_tier": 1,
            "backup_channel_handle": "@ligaprofesional",
        },
        {
            "id": "gimnasia_mendoza",
            "team_id": 11520,
            "name": "Gimnasia (Mendoza)",
            "short_name": "Gimnasia Mza",
            "competition_code": "ARG",
            "competition_name": "Argentine Liga Profesional",
            "youtube_handle": None,
            "aliases": ["gimnasia mendoza", "gimnasia y esgrima de mendoza", "gimnasia de mendoza"],
            "is_active": True,
            "channel_tier": 2,
            "backup_channel_handle": "@ligaprofesional",
        },
        {
            "id": "atlanta_united_fc",
            "team_id": 18418,
            "name": "Atlanta United FC",
            "short_name": "Atlanta United",
            "competition_code": "MLS",
            "competition_name": "Major League Soccer",
            "youtube_handle": "@atlutd",
            "youtube_channel_id": None,
            "aliases": ["atlanta united", "atlanta utd", "atl utd"],
            "is_active": True,
            "channel_tier": 1,
            "backup_channel_handle": "@MLS",
        },
        {
            "id": "toronto_fc",
            "team_id": 7318,
            "name": "Toronto FC",
            "short_name": "Toronto",
            "competition_code": "MLS",
            "competition_name": "Major League Soccer",
            "youtube_handle": "@torontofc",
            "youtube_channel_id": "UCc1qA_64TEqAX9pnGJOO0Dw",
            "aliases": ["toronto fc", "toronto"],
            "is_active": True,
            "channel_tier": 1,
            "backup_channel_handle": "@MLS",
        },
    ]

    try:
        db_client.table("supported_clubs").upsert(arg_clubs, on_conflict="id").execute()
        logger.info(f"Upserted {len(arg_clubs)} target clubs with clean handles and aliases.")
    except Exception as e:
        logger.error(f"Failed to upsert target clubs: {e}")

    logger.info("Database healing complete!")


if __name__ == "__main__":
    heal_database()
