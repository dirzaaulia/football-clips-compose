import sys, os
sys.stdout.reconfigure(encoding='utf-8')
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
from collections import defaultdict
from worker.clients.database_client import db_client
from worker.repositories.channel_repository import channel_repository

# 1. Audit Competitions
comps_res = db_client.table('competitions').select('*').execute()
comps = comps_res.data or []
print(f"=== COMPETITIONS AUDIT (Total: {len(comps)}) ===")
comp_codes = set()
for c in sorted(comps, key=lambda x: x.get('id', '')):
    code = c.get('id')
    comp_codes.add(code)
    name = c.get('name')
    cid = c.get('youtube_channel_id')
    handle = c.get('youtube_handle')
    valid_cid = bool(cid and cid.startswith('UC') and len(cid) == 24)
    print(f"[{code:4}] {name:34} | Handle: {str(handle):20} | CID: {str(cid):24} | Valid: {valid_cid}")

# 2. Audit Supported Clubs
clubs_res = db_client.table('supported_clubs').select('*').execute()
clubs = clubs_res.data or []
print(f"\n=== SUPPORTED CLUBS AUDIT (Total: {len(clubs)}) ===")
clubs_by_comp = defaultdict(list)
missing_cid = []
invalid_comp = []
for cl in clubs:
    comp = cl.get('competition_code')
    clubs_by_comp[comp].append(cl)
    cid = cl.get('youtube_channel_id') or ''
    if not (cid.startswith('UC') and len(cid) == 24):
        missing_cid.append(cl)
    if comp not in comp_codes:
        invalid_comp.append(cl)

for comp in sorted(clubs_by_comp.keys()):
    c_list = clubs_by_comp[comp]
    valid_cids = sum(1 for c in c_list if (c.get('youtube_channel_id') or '').startswith('UC') and len(c.get('youtube_channel_id') or '') == 24)
    print(f"  {comp:4}: {len(c_list):2} clubs (Valid CIDs: {valid_cids}/{len(c_list)})")

print(f"\nTotal clubs missing or with invalid CID: {len(missing_cid)}")
for m in missing_cid:
    print(f"  MISSING CID: {m.get('name')} [{m.get('competition_code')}] handle={m.get('youtube_handle')}, cid={m.get('youtube_channel_id')}")

print(f"\nTotal clubs with unknown/unsupported competition: {len(invalid_comp)}")
for inv in invalid_comp:
    print(f"  INVALID COMP: {inv.get('name')} [{inv.get('competition_code')}]")

# 3. WebSub Channel Discovery Audit
websub_channels = channel_repository.get_all_websub_channels()
print(f"\n=== WEBSUB SUBSCRIBER CHANNELS AUDIT ===")
print(f"Total Unique WebSub Channels Ready: {len(websub_channels)}")
types = defaultdict(int)
for ch_id, label in websub_channels.items():
    prefix = label.split(':')[0]
    types[prefix] += 1
for p, cnt in sorted(types.items()):
    print(f"  {p}: {cnt} channels")

# 4. Matches Audit for Season 2025 and 2026
print(f"\n=== MATCHES AUDIT (Season 2025 & 2026) ===")
for season in [2025, 2026]:
    res_m = db_client.table('matches').select('id,competition_id,competition_name,home_team_name,away_team_name,status').eq('season', season).execute()
    season_matches = res_m.data or []
    print(f"\n--- Season {season} (Total Matches: {len(season_matches)}) ---")
    comp_counts = defaultdict(int)
    unsupported_comp_matches = []
    
    for m in season_matches:
        cid = m.get('competition_id')
        comp_counts[cid] += 1
        if cid not in comp_codes:
            unsupported_comp_matches.append(m)
            
    print(f"Competitions in season {season}:")
    for cid, cnt in sorted(comp_counts.items()):
        status = "SUPPORTED" if cid in comp_codes else "UNSUPPORTED"
        print(f"  [{cid:4}]: {cnt:4} matches ({status})")
        
    print(f"Matches with unsupported competition code: {len(unsupported_comp_matches)}")
    for m in unsupported_comp_matches[:5]:
        print(f"  UNSUPPORTED: {m.get('home_team_name')} vs {m.get('away_team_name')} [{m.get('competition_id')}] {m.get('competition_name')}")

