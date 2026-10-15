import os
import json
from datetime import datetime, timezone
from collections import Counter
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from google.cloud import storage

BUCKET_NAME = os.getenv("TELEMETRY_BUCKET", "highlightdata-telemetry")
DEVICES_PREFIX = "audience/devices/"
SUMMARY_BLOB = "audience/audience_state.json"

app = FastAPI(title="FootballClips Telemetry Device Sync Service", version="1.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

storage_client = storage.Client()


class DeviceSyncRequest(BaseModel):
    device_id: str = Field(..., min_length=4, max_length=128)
    user_id: str = Field(..., min_length=4, max_length=128)
    platform: str = Field(default="android")
    device_name: str = Field(default="Unknown Device")
    favorite_clubs: list[int] = Field(default_factory=list)
    app_version: str = Field(default="2.14.8")


@app.get("/health")
def health():
    return {"status": "ok", "service": "device-sync", "version": "1.1.0"}


@app.post("/api/device-sync")
def sync_device(req: DeviceSyncRequest):
    try:
        bucket = storage_client.bucket(BUCKET_NAME)
        now_iso = datetime.now(timezone.utc).isoformat()

        # 1. Atomic write per device to prevent any concurrent overwrite
        device_data = {
            "device_id": req.device_id,
            "user_id": req.user_id,
            "platform": req.platform.lower(),
            "device_name": req.device_name,
            "favorite_clubs": req.favorite_clubs,
            "app_version": req.app_version,
            "last_active": now_iso
        }
        device_blob = bucket.blob(f"{DEVICES_PREFIX}{req.device_id}.json")
        device_blob.upload_from_string(
            json.dumps(device_data, indent=2),
            content_type="application/json"
        )

        # 2. Aggregate all devices into audience_state.json
        devices = {}
        for blob in bucket.list_blobs(prefix=DEVICES_PREFIX):
            if not blob.name.endswith(".json"):
                continue
            try:
                content = json.loads(blob.download_as_text())
                d_id = content.get("device_id")
                if d_id:
                    devices[d_id] = content
            except Exception:
                continue

        club_device_map = {}
        club_user_map = {}
        club_platform_map = {}

        for dev_id, d in devices.items():
            uid = d.get("user_id")
            plat = d.get("platform", "android")
            for cid in d.get("favorite_clubs", []):
                cid_str = str(cid)
                club_device_map.setdefault(cid_str, []).append(dev_id)
                club_user_map.setdefault(cid_str, []).append(uid)
                club_platform_map.setdefault(cid_str, []).append(plat)

        all_clubs = {}
        for cid_str, dev_list in club_device_map.items():
            user_list = club_user_map.get(cid_str, [])
            plat_list = club_platform_map.get(cid_str, [])
            user_counts = Counter(user_list)
            total_devs = len(dev_list)
            distinct_users = len(user_counts)
            single_users = sum(1 for cnt in user_counts.values() if cnt == 1)
            multi_users = sum(1 for cnt in user_counts.values() if cnt > 1)

            all_clubs[cid_str] = {
                "club_id": int(cid_str),
                "total_devices": total_devs,
                "distinct_users": distinct_users,
                "platform_breakdown": {
                    "android": plat_list.count("android"),
                    "web": plat_list.count("web")
                },
                "devices_per_user_ratio": round(total_devs / max(distinct_users, 1), 2),
                "distribution": {
                    "single_device_users": single_users,
                    "multi_device_users": multi_users
                }
            }

        state = {
            "updated_at": now_iso,
            "total_registered_devices": len(devices),
            "total_registered_users": len(set(d.get("user_id") for d in devices.values())),
            "clubs": all_clubs,
            "devices": devices
        }

        summary_blob = bucket.blob(SUMMARY_BLOB)
        summary_blob.cache_control = "no-cache, max-age=0"
        summary_blob.upload_from_string(
            json.dumps(state, indent=2),
            content_type="application/json"
        )
        summary_blob.make_public()

        return {
            "status": "success",
            "device_id": req.device_id,
            "total_clubs_subscribed": len(req.favorite_clubs),
            "updated_at": now_iso
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
