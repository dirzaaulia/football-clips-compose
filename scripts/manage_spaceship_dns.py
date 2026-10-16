"""
Spaceship DNS Automated Sync Utility
Safely syncs subdomains for FootballClips while preserving existing DNS records.
"""

import sys
import json
import urllib.request
import urllib.error

SPACESHIP_API_URL = "https://spaceship.dev/api/v1/dns/records"

def get_headers(api_key: str, api_secret: str):
    return {
        "X-Api-Key": api_key,
        "X-Api-Secret": api_secret,
        "User-Agent": "curl/8.4.0",
        "Content-Type": "application/json",
        "Accept": "application/json"
    }

def fetch_existing_records(domain: str, api_key: str, api_secret: str):
    url = f"{SPACESHIP_API_URL}/{domain}?take=100&skip=0"
    req = urllib.request.Request(url, headers=get_headers(api_key, api_secret), method="GET")
    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode())
            return data.get("items", [])
    except urllib.error.HTTPError as e:
        print(f"Error fetching DNS records: {e.code} - {e.read().decode()}")
        sys.exit(1)

def sync_footballclips_dns(domain: str, api_key: str, api_secret: str):
    print(f"[*] Fetching existing DNS records for {domain} from Spaceship API...")
    existing = fetch_existing_records(domain, api_key, api_secret)
    print(f"[+] Found {len(existing)} existing records:")
    for r in existing:
        print(f"    - {r.get('type')} {r.get('name')}: {r.get('address') or r.get('cname') or r.get('value')}")
    
    # Target records to inject/update
    target_records = [
        {
            "type": "CNAME",
            "name": "fc",
            "cname": "footballclips.web.app",
            "ttl": 1800
        },
        {
            "type": "A",
            "name": "api.fc",
            "address": "168.110.196.30",
            "ttl": 1800
        },
        {
            "type": "A",
            "name": "files.fc",
            "address": "168.110.196.30",
            "ttl": 1800
        },
        {
            "type": "A",
            "name": "edge.fc",
            "address": "168.110.196.30",
            "ttl": 1800
        }
    ]

    # Clean existing records (keep only type, name, ttl, and value/cname/address)
    clean_items = []
    target_names = {r["name"] for r in target_records}
    
    for item in existing:
        name = item.get("name")
        if name in target_names:
            print(f"[*] Removing old record for: {item.get('type')} {name}")
            continue
            
        record = {
            "type": item.get("type"),
            "name": name,
            "ttl": item.get("ttl", 1800)
        }
        if "address" in item:
            record["address"] = item["address"]
        if "cname" in item:
            record["cname"] = item["cname"]
        if "value" in item:
            record["value"] = item["value"]
            
        clean_items.append(record)
        
    merged = clean_items + target_records
    
    payload = {
        "force": True,
        "items": merged
    }

    url = f"{SPACESHIP_API_URL}/{domain}"
    req = urllib.request.Request(
        url,
        data=json.dumps(payload).encode("utf-8"),
        headers=get_headers(api_key, api_secret),
        method="PUT"
    )

    print(f"[*] Submitting authoritative zone update ({len(merged)} records) to Spaceship...")
    try:
        with urllib.request.urlopen(req) as resp:
            resp_body = resp.read().decode()
            print(f"[SUCCESS] Spaceship DNS updated successfully! Status: {resp.status}")
            if resp_body:
                print(f"[RESPONSE] {resp_body}")
    except urllib.error.HTTPError as e:
        print(f"[ERROR] Failed to update DNS: {e.code} - {e.read().decode()}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python manage_spaceship_dns.py <SPACESHIP_API_KEY> <SPACESHIP_API_SECRET> [domain]")
        sys.exit(1)
        
    api_key = sys.argv[1]
    api_secret = sys.argv[2]
    domain = sys.argv[3] if len(sys.argv) > 3 else "dirzaaulia.com"
    
    sync_footballclips_dns(domain, api_key, api_secret)
