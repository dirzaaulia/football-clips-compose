import json
import urllib.request
import pg8000.native

# Connect to DB
conn = pg8000.native.Connection(
    'postgres',
    host='100.82.171.21',
    port=5432,
    database='footballclips',
    password='jarvis_postgres_secure_2026'
)

# Cup competitions that can have shootouts
CUP_SLUGS = {
    "EFL": "eng.league_cup",
    "FAC": "eng.fa",
    "CDR": "esp.copa_del_rey",
    "DFB": "ger.dfb_pokal",
    "CIT": "ita.coppa_italia",
    "CL": "uefa.champions",
    "EL": "uefa.europa",
    "ECL": "uefa.europa.conf",
}

print("Querying finished matches with tied scores in cup competitions...")
rows = conn.run("""
    SELECT id, competition_id, home_team_name, away_team_name, home_score, away_score, status 
    FROM matches 
    WHERE season = 2026 
      AND status = 'FINISHED' 
      AND home_score = away_score 
      AND competition_id IN ('EFL', 'FAC', 'CDR', 'DFB', 'CIT', 'CL', 'EL', 'ECL');
""")

print(f"Found {len(rows)} tied finished cup matches to check on ESPN:")

updated_count = 0
for r in rows:
    match_id = r[0]
    comp_id = r[1]
    home_name = r[2]
    away_name = r[3]
    h_score = r[4]
    a_score = r[5]
    
    slug = CUP_SLUGS.get(comp_id, "eng.league_cup")
    url = f"https://site.api.espn.com/apis/site/v2/sports/soccer/{slug}/summary?event={match_id}"
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            header = data.get("header", {})
            competitions = header.get("competitions", [])
            if not competitions:
                continue
            comp = competitions[0]
            status_detail = comp.get("status", {}).get("type", {}).get("detail", "")
            
            home_pen = None
            away_pen = None
            for c in comp.get("competitors", []):
                s_score = c.get("shootoutScore")
                if s_score is not None:
                    try:
                        val = int(float(s_score))
                        if c.get("homeAway") == "home":
                            home_pen = val
                        else:
                            away_pen = val
                    except (ValueError, TypeError):
                        pass
            
            if home_pen is not None and away_pen is not None:
                print(f" -> MATCH {match_id}: {home_name} {h_score}({home_pen}) - ({away_pen}){a_score} {away_name} (Status: {status_detail})")
                conn.run(
                    "UPDATE matches SET home_penalties = :h_pen, away_penalties = :a_pen WHERE id = :mid;",
                    h_pen=home_pen,
                    a_pen=away_pen,
                    mid=match_id
                )
                updated_count += 1
            else:
                print(f" - MATCH {match_id}: {home_name} {h_score} - {a_score} {away_name} (No shootout: {status_detail})")
    except Exception as e:
        print(f" - Error checking match {match_id}: {e}")

print(f"\nBackfill completed! Updated {updated_count} matches with penalty shootout scores.")
