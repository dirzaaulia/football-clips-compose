import os
from collections import Counter
from datetime import datetime, timezone

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import httpx
from pydantic import BaseModel, Field

EDGE_POSTGREST_URL = os.getenv("FC_POSTGREST_URL", "https://api.fc.dirzaaulia.com/rest/v1")

app = FastAPI(title="FootballClips Telemetry Device Sync Service", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class DeviceSyncRequest(BaseModel):
    device_id: str = Field(..., min_length=4, max_length=128)
    user_id: str = Field(..., min_length=4, max_length=128)
    platform: str = Field(default="android")
    device_name: str = Field(default="Unknown Device")
    favorite_clubs: list[int] = Field(default_factory=list)
    app_version: str = Field(default="2.14.8")


@app.get("/health")
def health():
    return {"status": "ok", "service": "device-sync", "version": "2.0.0"}


@app.post("/api/device-sync")
def sync_device(req: DeviceSyncRequest):
    try:
        now_iso = datetime.now(timezone.utc).isoformat()

        # 1. Fetch current audience state from Oracle VM PostgREST
        state = {}
        with httpx.Client(timeout=10.0) as client:
            fetch_res = client.get(
                f"{EDGE_POSTGREST_URL}/telemetry_state?key=eq.audience&select=payload",
                headers={"Accept": "application/json"},
            )
            if fetch_res.status_code == 200:
                rows = fetch_res.json()
                if rows and len(rows) > 0 and "payload" in rows[0]:
                    state = rows[0]["payload"]

            devices = state.get("devices", {})

            # 2. Update device entry
            devices[req.device_id] = {
                "device_id": req.device_id,
                "user_id": req.user_id,
                "platform": req.platform.lower(),
                "device_name": req.device_name,
                "favorite_clubs": req.favorite_clubs,
                "app_version": req.app_version,
                "last_active": now_iso,
            }

            # 3. Recalculate clubs metrics
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
                        "web": plat_list.count("web"),
                    },
                    "devices_per_user_ratio": round(total_devs / max(distinct_users, 1), 2),
                    "distribution": {
                        "single_device_users": single_users,
                        "multi_device_users": multi_users,
                    },
                }

            updated_state = {
                "updated_at": now_iso,
                "total_registered_devices": len(devices),
                "total_registered_users": len(set(d.get("user_id") for d in devices.values())),
                "clubs": all_clubs,
                "devices": devices,
            }

            # 4. Upsert into Oracle VM PostgREST telemetry_state
            upsert_res = client.post(
                f"{EDGE_POSTGREST_URL}/telemetry_state",
                headers={
                    "Content-Type": "application/json",
                    "Prefer": "resolution=merge-duplicates",
                },
                json={
                    "key": "audience",
                    "payload": updated_state,
                    "updated_at": now_iso,
                },
            )
            if upsert_res.status_code >= 400:
                raise HTTPException(status_code=500, detail=f"PostgREST upsert failed: {upsert_res.text}")

        return {
            "status": "success",
            "device_id": req.device_id,
            "total_clubs_subscribed": len(req.favorite_clubs),
            "updated_at": now_iso,
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
