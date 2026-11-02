import json
import re
import urllib.request
from pathlib import Path
import sys

# Ensure project root is in sys.path
sys.path.insert(0, str(Path(__file__).parent.parent))

from worker.config.constants import (
    EUROPEAN_CLUB_HANDLES,
    OFFICIAL_LEAGUE_CHANNELS,
    BACKUP_LEAGUE_CHANNELS,
    PERMANENT_CHANNEL_IDS,
)

def clean_name(s: str) -> str:
    s = s.lower()
    s = re.sub(r'\b(fc|cf|sc|ac|afc|cd|rc|rcd|ud|sd|1\.|fsv|vfl|vfb|tsv|bsc|sp|ss|us|as|ssc)\b', '', s)
    s = re.sub(r'[\d_.-]', ' ', s)
    s = re.sub(r'\s+', ' ', s).strip()
    return s

def main():
    with open("clubs_dump.json", "r", encoding="utf-16") as f:
        clubs = json.load(f)
    with open("sources_dump.json", "r", encoding="utf-16") as f:
        sources = json.load(f)

    print(f"Loaded {len(clubs)} clubs and {len(sources)} sources.")

    # 1. Fetch ESPN teams across all supported leagues
    espn_slugs = {
        "PL": "eng.1", "PD": "esp.1", "SA": "ita.1", "BL1": "ger.1", "FL1": "fra.1",
        "DED": "ned.1", "PPL": "por.1", "SPL": "ksa.1", "MLS": "usa.1", "IDN": "idn.1",
        "ARG": "arg.1", "BRA": "bra.1", "CL": "uefa.champions", "EL": "uefa.europa",
        "ECL": "uefa.europa.conf"
    }

    espn_team_map = {} # name/alias -> (team_id, logo_url)
    headers = {"User-Agent": "Mozilla/5.0"}

    for comp, slug in espn_slugs.items():
        url = f"https://site.api.espn.com/apis/site/v2/sports/soccer/{slug}/teams"
        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=10) as resp:
                data = json.loads(resp.read().decode())
                teams = data.get("sports", [{}])[0].get("leagues", [{}])[0].get("teams", [])
                for t in teams:
                    tm = t.get("team", {})
                    tid = tm.get("id")
                    logos = tm.get("logos", [])
                    logo = logos[0]["href"] if logos else f"https://a.espncdn.com/i/teamlogos/soccer/500/{tid}.png"
                    
                    full_name = tm.get("name", "").lower()
                    disp_name = tm.get("displayName", "").lower()
                    short_disp = tm.get("shortDisplayName", "").lower()
                    
                    for n in [full_name, disp_name, short_disp, clean_name(full_name), clean_name(disp_name)]:
                        if n:
                            espn_team_map[n] = (tid, logo)
        except Exception as e:
            print(f"Notice: Failed to fetch ESPN teams for {comp} ({slug}): {e}")

    print(f"Total ESPN team entries indexed: {len(espn_team_map)}")

    # Build sources map
    source_channel_map = {} # handle -> channel_id
    club_source_handles = {} # team name/alias -> handle
    for s in sources:
        handle = s.get("handle")
        cid = s.get("channel_id")
        if handle and cid:
            source_channel_map[handle] = cid
            source_channel_map[handle.lower()] = cid
        if s.get("kind") == "club":
            label = s.get("label", "").lower()
            if label and handle:
                club_source_handles[label] = handle
                club_source_handles[clean_name(label)] = handle
            for alias in s.get("team_aliases") or []:
                a_clean = alias.lower()
                club_source_handles[a_clean] = handle
                club_source_handles[clean_name(a_clean)] = handle

    # Add permanent channel IDs to map
    for h, cid in PERMANENT_CHANNEL_IDS.items():
        source_channel_map[h] = cid
        source_channel_map[h.lower()] = cid

    # Direct known ESPN ID mapping for European clubs
    DIRECT_ESPN_MAP = {
        "1_fc_k_ln": "122",
        "fc_bayern_m_nchen": "132",
        "hamburger_sv": "124",
        "sv_werder_bremen": "134",
        "club_brugge_kv": "2329",
        "fk_bod_glimt": "2980",
        "fk_shakhtar_donetsk": "2676",
        "fenerbah_e_sk": "436",
        "galatasaray_sk": "437",
        "pae_aek": "887",
        "sk_slavia_praha": "2200",
        "sporting_clube_de_portugal": "508",
        "k_slovan_bratislava": "4192",
        "angers_sco": "284",
        "es_troyes_ac": "270",
        "lille_osc": "166",
        "ogc_nice": "167",
        "olympique_lyonnais": "164",
        "olympique_de_marseille": "160",
        "rc_strasbourg_alsace": "280",
        "racing_club_de_lens": "168",
        "stade_brestois_29": "273",
        "ca_osasuna": "97",
        "club_atl_tico_de_madrid": "1068",
        "deportivo_alav_s": "96",
        "rc_celta_de_vigo": "85",
        "rc_deportivo_la_coru_a": "89",
        "rcd_espanyol_de_barcelona": "88",
        "real_betis_balompi": "244",
        "real_racing_club_de_santander": "91",
        "real_sociedad_de_f_tbol": "86",
        "acf_fiorentina": "109",
        "atalanta_bc": "105",
        "cagliari_calcio": "106",
        "fc_internazionale_milano": "110",
        "frosinone_calcio": "3624",
        "genoa_cfc": "3263",
        "parma_calcio_1913": "111",
        "us_sassuolo_calcio": "3625",
        "udinese_calcio": "114",
    }

    # Known Indonesian fallback crests (clean PNG URLs with CORS or fallback)
    indonesian_crests = {
        "persis solo": "https://a.espncdn.com/i/teamlogos/soccer/500/21590.png",
        "persijap": "https://a.espncdn.com/i/teamlogos/soccer/500/131305.png",
        "madura united fc": "https://a.espncdn.com/i/teamlogos/soccer/500/18732.png",
        "psbs biak": "https://a.espncdn.com/i/teamlogos/soccer/500/22338.png",
        "bhayangkara presisi": "https://a.espncdn.com/i/teamlogos/soccer/500/18734.png",
        "persita": "https://a.espncdn.com/i/teamlogos/soccer/500/8033.png",
        "malut united": "https://a.espncdn.com/i/teamlogos/soccer/500/22339.png",
        "dewa united": "https://a.espncdn.com/i/teamlogos/soccer/500/21591.png",
        "psim yogyakarta": "https://a.espncdn.com/i/teamlogos/soccer/500/131304.png",
        "persik kediri": "https://a.espncdn.com/i/teamlogos/soccer/500/7524.png",
    }

    updates = []
    tier1_count = 0
    tier2_count = 0
    crest_upgraded = 0

    for club in clubs:
        cid = club["id"]
        name = club["name"]
        short_name = club.get("short_name", "")
        comp_code = club.get("competition_code", "")
        crest_url = club.get("crest_url") or ""
        aliases = club.get("aliases") or []

        # 1. Resolve Crest URL
        new_crest = crest_url
        if cid in DIRECT_ESPN_MAP:
            new_crest = f"https://a.espncdn.com/i/teamlogos/soccer/500/{DIRECT_ESPN_MAP[cid]}.png"
            crest_upgraded += 1
        elif "football-data.org" in crest_url or not crest_url or "ileague.id" in crest_url:
            # Try to match with ESPN
            matched_espn = None
            for cand in [name.lower(), short_name.lower(), clean_name(name), clean_name(short_name)] + [a.lower() for a in aliases]:
                if cand in espn_team_map:
                    matched_espn = espn_team_map[cand]
                    break
            
            if matched_espn:
                new_crest = matched_espn[1]
                crest_upgraded += 1
            elif name.lower() in indonesian_crests:
                new_crest = indonesian_crests[name.lower()]
                crest_upgraded += 1

        # 2. Resolve YouTube Handle & Tier
        handle = None
        # Check EUROPEAN_CLUB_HANDLES
        for cand in [name.lower(), short_name.lower(), clean_name(name), clean_name(short_name)] + [a.lower() for a in aliases]:
            if cand in EUROPEAN_CLUB_HANDLES:
                handle = EUROPEAN_CLUB_HANDLES[cand]
                break

        # Check club_source_handles
        if not handle:
            for cand in [name.lower(), short_name.lower(), clean_name(name)] + [a.lower() for a in aliases]:
                if cand in club_source_handles:
                    handle = club_source_handles[cand]
                    break

        league_handle = OFFICIAL_LEAGUE_CHANNELS.get(comp_code, "@SkySportsFootball")
        backup_league_list = BACKUP_LEAGUE_CHANNELS.get(comp_code, ["@ESPNFans"])
        backup_league = backup_league_list[0] if backup_league_list else "@ESPNFans"

        if handle:
            tier = 1
            tier1_count += 1
            backup_handle = league_handle
        else:
            tier = 2
            tier2_count += 1
            handle = league_handle
            backup_handle = backup_league

        # Resolve channel ID
        channel_id = source_channel_map.get(handle) or source_channel_map.get(handle.lower()) or ""
        if not channel_id and tier == 1:
            # Fallback to league channel ID if club channel ID not in map
            channel_id = source_channel_map.get(league_handle, "")

        updates.append({
            "id": cid,
            "youtube_handle": handle,
            "youtube_channel_id": channel_id,
            "channel_tier": tier,
            "backup_channel_handle": backup_handle,
            "crest_url": new_crest,
        })

    print(f"\n--- Backfill Summary ---")
    print(f"Total Clubs: {len(updates)}")
    print(f"Tier 1 (Official Club Channel): {tier1_count}")
    print(f"Tier 2 (Official League Channel): {tier2_count}")
    print(f"Crests Upgraded to ESPN CDN / Valid: {crest_upgraded}")

    # Generate SQL file
    sql_lines = ["BEGIN;"]
    for u in updates:
        h_esc = u["youtube_handle"].replace("'", "''")
        cid_esc = u["youtube_channel_id"].replace("'", "''")
        tier_val = u["channel_tier"]
        b_esc = u["backup_channel_handle"].replace("'", "''")
        c_esc = u["crest_url"].replace("'", "''")
        id_esc = u["id"].replace("'", "''")

        sql = f"""UPDATE public.supported_clubs SET 
    youtube_handle = '{h_esc}',
    youtube_channel_id = '{cid_esc}',
    channel_tier = {tier_val},
    backup_channel_handle = '{b_esc}',
    crest_url = '{c_esc}',
    updated_at = NOW()
WHERE id = '{id_esc}';"""
        sql_lines.append(sql)

    sql_lines.append("COMMIT;")
    sql_lines.append("NOTIFY pgrst, 'reload schema';")

    with open("backfill_clubs.sql", "w", encoding="utf-8") as f:
        f.write("\n".join(sql_lines))

    print(f"Generated backfill_clubs.sql ({len(sql_lines)} statements).")

if __name__ == "__main__":
    main()
