import json
import urllib.parse
import urllib.request
import re
from worker.clients.youtube_client import youtube_client

with open("audit_findings.json", encoding="utf-16") as f:
    d = json.load(f)

missing = d["missing_channel_ids"]
print(f"Resolving {len(missing)} clubs with missing channel IDs...")

updates = []

for item in missing:
    cid = item["id"]
    name = item["name"]
    handle = item.get("handle") or ""
    ch_id = None

    if handle and handle.startswith("@"):
        ch_id = youtube_client.resolve_channel_id(handle)
        if ch_id:
            print(f"[OK] Found via handle {handle}: {ch_id} ({name})")
            updates.append({"id": cid, "name": name, "handle": handle, "channel_id": ch_id})
            continue

    # Fallback to search if handle invalid or empty
    q = f"{name} official channel"
    try:
        req = youtube_client.youtube.search().list(
            q=q,
            type="channel",
            part="snippet",
            maxResults=3
        )
        res = req.execute()
        items = res.get("items", [])
        if items:
            best_ch = items[0]
            ch_id = best_ch["id"]["channelId"]
            ch_title = best_ch["snippet"]["title"]
            c_handle = best_ch["snippet"].get("customUrl") or handle
            if c_handle and not c_handle.startswith("@"):
                c_handle = "@" + c_handle
            print(f"[OK] Found via Search '{q}': [{ch_id}] {ch_title} (Handle: {c_handle})")
            updates.append({"id": cid, "name": name, "handle": c_handle, "channel_id": ch_id})
        else:
            print(f"[FAIL] Not found: {name}")
    except Exception as e:
        print(f"[FAIL] Search error for {name}: {e}")

print(f"\nSuccessfully resolved {len(updates)} / {len(missing)} clubs!")
with open("resolved_channel_updates.json", "w", encoding="utf-8") as f:
    json.dump(updates, f, indent=2)
