import os
import sys
from pathlib import Path
sys.path.insert(0, str(Path.cwd()))
import pg8000.native
from dotenv import load_dotenv

load_dotenv('worker/.env')

from worker.config.constants import (
    OFFICIAL_LEAGUE_CHANNELS,
    BACKUP_LEAGUE_CHANNELS,
    PERMANENT_CHANNEL_IDS,
    EUROPEAN_CLUB_HANDLES,
    CUSTOM_KEYWORD_ALIASES,
)
from worker.config.broadcaster_registry import GLOBAL_BROADCASTER_REGISTRY

EXTENDED_LEAGUE_CHANNELS = {
    "PL": ("@SkySportsFootball", "UCNAf1k0yIjyGu3k9BwAg3lg"),
    "CL": ("@tntsports", "UC4i_9WvfPRTuRFFb74YM1Mg"),
    "EL": ("@tntsports", "UC4i_9WvfPRTuRFFb74YM1Mg"),
    "ECL": ("@tntsports", "UC4i_9WvfPRTuRFFb74YM1Mg"),
    "PD": ("@LaLiga", "UCTv-XvfzLX3i4IGWAm4sbmA"),
    "BL1": ("@bundesliga", "UC6UL29enLNe4mqwTfAyeNuw"),
    "FL1": ("@ligue1", "UCQsH5XtIc9hONE1BQjucM0g"),
    "SA": ("@SerieA", "UCBJeMCIeLQos7wacox4hmLQ"),
    "FAC": ("@thefacup", "UCChcWqwYXCEs657MQ00qVWA"),
    "CDR": ("@rfef", "UCmo2IxIA_aPfSJDPrTHyhKw"),
    "CIT": ("@SerieA", "UCBJeMCIeLQos7wacox4hmLQ"),
    "DFB": ("@GermanFootball", "UCfMo0xj-sbdzHuzxvKdu1hw"),
    "EFL": ("@EFL", "UCkgm4b5n3QsMUgB9FjA8S2w"),
    "CDF": ("@FFF", "UCuxiacueNMDlnjIaqwzRh8A"),
    "DED": ("@espnnl", "UC6c1z7bA__85CIWZ_jpCK-Q"),
    "MLS": ("@MLS", "UCsrZhuPkq_i71u1wK_Uq_5w"),
    "PPL": ("@VSPORTSPORTUGAL", "UCuIlu5oGIj1RzHOYmeSV5Eg"),
    "SPL": ("@Spl", "UCq33AlEZl8QBmxatFEwknew"),
    "ACL": ("@TheAFCHub", "UCnj0TjaM0wyxkAWW_nz8_1g"),
    "LIB": ("@libertadores", "UClgFf9mS1mZI2D_vo-ZTpmQ"),
    "SUD": ("@sudamericana", "UCw7eDYHnA-2PIr-qIldYeBQ"),
    "REC": ("@libertadores", "UClgFf9mS1mZI2D_vo-ZTpmQ"),
    "BRA": ("@brasileirao", "UCrf4Fr6uTCoU9RkYa5CbzcA"),
    "ARG": ("@ligaprofesional", "UCJmCVoUfCBQb9lcfXIS8nXQ"),
    "IDN": ("@liga1match", "UCyJejTAHiCrIRqWmFrbgQfQ"),
}

REGIONAL_BACKUP_MAP = {
    "@beINSPORTS_Asia": "UCp7k_8w_j5f3m4QAkX_5YgA",
    "@SonySportsNetwork": "UC4i_9WvfPRTuRFFb74YM1MgA",
    "@Vidio": "UCbL9f6s_w9x8c_0w9kH0v4Q",
    "@vidio_sports": "UCbL9f6s_w9x8c_0w9kH0v4Q",
    "@CazeTV": "UCkz8VjQ_n4rP5tFwQx0_8wA",
    "@TNTSportsBR": "UCt7m_dKq_kX8y_j6pM4m3_A",
    "@ESPNFans": "UCFmMw7yTuLTCuMhpZD5dVsg",
    "@CBSSportsGolazo": "UCqYgW3bT1_K0i_tKk0d8Zg",
    "@espnfc": "UCbL3x3c7V6oA5_k_j1k9mrg",
    "@SuperSportOfficial": "UCcW9A2U97T6gA5qP7Z5_a1A",
}

def sql_quote(s: str) -> str:
    if s is None:
        return "NULL"
    return "'" + str(s).replace("'", "''") + "'"

def sql_array(arr: list[str]) -> str:
    if not arr:
        return "ARRAY[]::text[]"
    items = [sql_quote(x) for x in arr]
    return f"ARRAY[{','.join(items)}]::text[]"

def seed_channel_registry():
    print("Connecting to PostgreSQL at 100.82.171.21:5432...", flush=True)
    con = pg8000.native.Connection(
        user='postgres',
        host='100.82.171.21',
        port=5432,
        database='footballclips',
        password='jarvis_postgres_secure_2026',
        timeout=15,
    )

    print("=== 1. SEEDING CHANNELS IN COMPETITIONS ===", flush=True)
    for comp_id, (handle, channel_id) in EXTENDED_LEAGUE_CHANNELS.items():
        backups = BACKUP_LEAGUE_CHANNELS.get(comp_id, [])
        if comp_id in ["CL", "EL", "ECL"]:
            backups = ["@CBSSportsGolazo", "@tntsports", "@Vidio"]
        elif comp_id == "PL":
            backups = ["@SkySportsFootball", "@tntsports", "@Vidio"]
        
        backup_ids = [REGIONAL_BACKUP_MAP.get(b, PERMANENT_CHANNEL_IDS.get(b, "")) for b in backups]
        
        sql = f"""
            UPDATE public.competitions
            SET youtube_handle = {sql_quote(handle)},
                youtube_channel_id = {sql_quote(channel_id)},
                backup_channel_handles = {sql_array(backups)},
                backup_channel_ids = {sql_array(backup_ids)}
            WHERE id = {sql_quote(comp_id)};
        """
        con.run(sql)
    print(f"Updated {len(EXTENDED_LEAGUE_CHANNELS)} competitions with official & backup channel IDs.", flush=True)

    print("\n=== 2. SEEDING CHANNELS & ALIASES IN SUPPORTED CLUBS ===", flush=True)
    clubs = con.run("SELECT id, name, short_name FROM public.supported_clubs;")
    updated_clubs = 0

    for row in clubs:
        club_id, name, short_name = row[0], row[1], row[2]
        norm_name = name.lower().strip()
        norm_short = (short_name or "").lower().strip()

        handle = (
            EUROPEAN_CLUB_HANDLES.get(norm_name)
            or EUROPEAN_CLUB_HANDLES.get(norm_short)
            or EUROPEAN_CLUB_HANDLES.get(norm_name.replace(" fc", ""))
            or EUROPEAN_CLUB_HANDLES.get(norm_short.replace(" fc", ""))
        )
        if not handle:
            for k, h in EUROPEAN_CLUB_HANDLES.items():
                if k in norm_name or norm_name in k:
                    handle = h
                    break

        channel_id = PERMANENT_CHANNEL_IDS.get(handle, "") if handle else ""
        aliases = CUSTOM_KEYWORD_ALIASES.get(norm_name) or CUSTOM_KEYWORD_ALIASES.get(norm_short) or []

        handle_sql = sql_quote(handle) if handle else "youtube_handle"
        channel_id_sql = sql_quote(channel_id) if channel_id else "youtube_channel_id"
        aliases_sql = sql_array(aliases) if aliases else "aliases"

        sql = f"""
            UPDATE public.supported_clubs
            SET youtube_handle = {handle_sql},
                youtube_channel_id = {channel_id_sql},
                aliases = {aliases_sql}
            WHERE id = {sql_quote(club_id)};
        """
        con.run(sql)
        updated_clubs += 1

    print(f"Processed {updated_clubs} clubs in supported_clubs table.", flush=True)

    # Audit coverage
    res = con.run("SELECT count(*) FROM public.supported_clubs WHERE youtube_channel_id IS NOT NULL AND youtube_channel_id != '';")
    with_id = res[0][0]
    total = len(clubs)
    pct = (with_id * 100.0) / total if total > 0 else 0
    print(f"Verified Channel ID Coverage: {with_id} / {total} clubs ({pct:.1f}%).", flush=True)

    # Notify PostgREST
    con.run("NOTIFY pgrst, 'reload schema';")
    con.close()
    print("PostgREST schema reloaded successfully!", flush=True)

if __name__ == "__main__":
    seed_channel_registry()
