import json
import urllib.request

EDGE_URL = "https://edge.fc.dirzaaulia.com"
SUPABASE_CLOUD_URL = "https://eiomktvavndorazreyba.supabase.co"

# Load service role key from worker/.env
service_key = ""
with open("worker/.env", "r", encoding="utf-8") as f:
    for line in f:
        if line.startswith("SUPABASE_SERVICE_ROLE_KEY="):
            service_key = line.split("=", 1)[1].strip()

if not service_key:
    raise ValueError("SUPABASE_SERVICE_ROLE_KEY not found in worker/.env")

headers_supabase = {
    "apikey": service_key,
    "Authorization": f"Bearer {service_key}",
    "Content-Type": "application/json",
    "Prefer": "resolution=merge-duplicates"
}

# 1. Inspect exact schema columns from Supabase Cloud
req_sample = urllib.request.Request(f"{SUPABASE_CLOUD_URL}/rest/v1/matches?limit=1", headers=headers_supabase)
with urllib.request.urlopen(req_sample) as resp:
    sample_data = json.loads(resp.read().decode())
    allowed_columns = set(sample_data[0].keys())

print(f"Supabase Cloud supports {len(allowed_columns)} columns: {sorted(allowed_columns)}")

# 2. Fetch all season 2026 matches from Edge Proxy (PostgreSQL 16)
print("\nFetching all season 2026 matches from Edge Proxy...")
req_edge = urllib.request.Request(f"{EDGE_URL}/rest/v1/matches?season=eq.2026&order=utc_date.desc&limit=1000")
with urllib.request.urlopen(req_edge) as resp:
    edge_matches = json.loads(resp.read().decode())

print(f"Retrieved {len(edge_matches)} season 2026 matches from Edge.")

batch_size = 50
total_upserted = 0

for i in range(0, len(edge_matches), batch_size):
    raw_batch = edge_matches[i:i + batch_size]
    # Filter to only allowed columns for backward compatibility with Supabase Cloud
    sanitized_batch = [
        {k: v for k, v in m.items() if k in allowed_columns}
        for m in raw_batch
    ]

    data_json = json.dumps(sanitized_batch).encode('utf-8')
    req_upsert = urllib.request.Request(
        f"{SUPABASE_CLOUD_URL}/rest/v1/matches",
        data=data_json,
        headers=headers_supabase,
        method="POST"
    )
    with urllib.request.urlopen(req_upsert) as resp:
        total_upserted += len(sanitized_batch)
        print(f"Replicated batch {i}..{i + len(sanitized_batch)} ({total_upserted}/{len(edge_matches)})")

print(f"\nSUCCESS! Replicated {total_upserted} matches and video highlights to Legacy Supabase Cloud tier.")
