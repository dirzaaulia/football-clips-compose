import os
import sys
import asyncio
from datetime import datetime, UTC
from pathlib import Path

sys.path.insert(0, str(Path.cwd()))
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

from worker.clients.supabase_client import supabase_client
from worker.services.match_service import match_service
from worker.services.standings_service import standings_service

COMPETITION_DATA = {
    "id": "IDN",
    "name": "Liga 1 Indonesia",
    "short_name": "Liga 1",
    "country_code": "ID",
    "emblem_url": "https://a.espncdn.com/i/leaguelogos/soccer/500/2309.png",
    "league_id": 2309,
    "is_active": True,
    "display_order": 13,
}

CLUBS_DATA = [
    {"id": "persib_bandung", "team_id": 18320, "name": "Persib Bandung", "short_name": "Persib", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18320.png", "is_active": True},
    {"id": "persija_jakarta", "team_id": 18321, "name": "Persija Jakarta", "short_name": "Persija", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18321.png", "is_active": True},
    {"id": "persebaya_surabaya", "team_id": 18322, "name": "Persebaya Surabaya", "short_name": "Persebaya", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18322.png", "is_active": True},
    {"id": "arema_fc", "team_id": 18323, "name": "Arema FC", "short_name": "Arema", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18323.png", "is_active": True},
    {"id": "bali_united_fc", "team_id": 18324, "name": "Bali United FC", "short_name": "Bali United", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18324.png", "is_active": True},
    {"id": "psm_makassar", "team_id": 18325, "name": "PSM Makassar", "short_name": "PSM Makassar", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18325.png", "is_active": True},
    {"id": "borneo_fc_samarinda", "team_id": 18326, "name": "Borneo FC Samarinda", "short_name": "Borneo FC", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18326.png", "is_active": True},
    {"id": "madura_united_fc", "team_id": 18327, "name": "Madura United FC", "short_name": "Madura United", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18327.png", "is_active": True},
    {"id": "dewa_united_fc", "team_id": 18328, "name": "Dewa United FC", "short_name": "Dewa United", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18328.png", "is_active": True},
    {"id": "psis_semarang", "team_id": 18329, "name": "PSIS Semarang", "short_name": "PSIS", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18329.png", "is_active": True},
    {"id": "persik_kediri", "team_id": 18330, "name": "Persik Kediri", "short_name": "Persik", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18330.png", "is_active": True},
    {"id": "barito_putera", "team_id": 18331, "name": "PS Barito Putera", "short_name": "Barito Putera", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18331.png", "is_active": True},
    {"id": "pss_sleman", "team_id": 18332, "name": "PSS Sleman", "short_name": "PSS Sleman", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18332.png", "is_active": True},
    {"id": "persita_tangerang", "team_id": 18333, "name": "Persita Tangerang", "short_name": "Persita", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18333.png", "is_active": True},
    {"id": "semen_padang_fc", "team_id": 18334, "name": "Semen Padang FC", "short_name": "Semen Padang", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18334.png", "is_active": True},
    {"id": "malut_united_fc", "team_id": 18335, "name": "Malut United FC", "short_name": "Malut United", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18335.png", "is_active": True},
    {"id": "psbs_biak", "team_id": 18336, "name": "PSBS Biak", "short_name": "PSBS Biak", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18336.png", "is_active": True},
    {"id": "persis_solo", "team_id": 18337, "name": "Persis Solo", "short_name": "Persis", "competition_code": "IDN", "competition_name": "Liga 1 Indonesia", "crest_url": "https://a.espncdn.com/i/teamlogos/soccer/500/18337.png", "is_active": True},
]

async def apply_migration_and_sync():
    print("=== 🇮🇩 APPLYING LIGA 1 INDONESIA MIGRATION TO POSTGREST DATASTORE ===")

    # 1. Upsert Competition 'IDN'
    if supabase_client:
        try:
            res = supabase_client.table("competitions").upsert(COMPETITION_DATA).execute()
            print(f"✅ Competition 'IDN' seeded successfully into PostgreSQL datastore!")
        except Exception as e:
            print(f"⚠️ Error upserting competition 'IDN': {e}")

        # 2. Upsert all 18 clubs
        print(f"Seeding {len(CLUBS_DATA)} Liga 1 Indonesia clubs...")
        success_count = 0
        for club in CLUBS_DATA:
            try:
                club["updated_at"] = datetime.now(UTC).isoformat()
                res = supabase_client.table("supported_clubs").upsert(club).execute()
                if res:
                    success_count += 1
            except Exception as e:
                print(f"⚠️ Error upserting club {club['id']}: {e}")
        print(f"✅ Seeded {success_count}/{len(CLUBS_DATA)} Liga 1 clubs into supported_clubs!")
    else:
        print("⚠️ Supabase client not initialized - checking settings.")

    # 3. Synchronize Matches & Live Scores for 'IDN'
    print("\nSynchronizing live fixtures, results, and standings for all competitions including Liga 1 ('IDN')...")
    try:
        synced_matches = await match_service.sync_matches(is_full_sync=True)
        print(f"✅ Synchronized {len(synced_matches)} total matches into PostgreSQL datastore!")
    except Exception as e:
        print(f"⚠️ Match sync notice: {e}")

    # 4. Synchronize Standings
    try:
        synced_standings = await standings_service.sync_standings(season=2026)
        print(f"✅ Synchronized {len(synced_standings)} total standings rows into PostgreSQL datastore!")
    except Exception as e:
        print(f"⚠️ Standings sync notice: {e}")

    print("\n🎉 LIGA 1 INDONESIA MIGRATION & BACKEND SYNC COMPLETE!")

if __name__ == "__main__":
    asyncio.run(apply_migration_and_sync())
