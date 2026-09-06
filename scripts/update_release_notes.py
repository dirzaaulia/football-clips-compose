import os
import sys
import json
import time
import requests
import jwt

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

PACKAGE_NAME = "com.dirzaaulia.footballclips"
KEY_FILE = "fastlane/play-console-key.json"

RELEASE_NOTES = [
    {
        "language": "id",
        "text": """🎉 Football Clips hadir dengan pembaruan besar yang makin seru & ngebut!

✨ Desain M3 Expressive: Tampilan lebih estetik dengan animasi droplet ganti tema yang super mulus!
⚡ Cuplikan & Skor Real-Time: Tonton highlight video resmi & skor langsung liga top dunia lebih instan tanpa lag.
🧭 Floating Bar Modern: Jelajah jadwal pertandingan & klub favorit makin mudah dan asyik.
🛠️ Performa Lebih Gesit: Optimasi sistem & perbaikan bug untuk kenyamanan Anda. Enjoy the game! ⚽🔥"""
    },
    {
        "language": "en-US",
        "text": """🎉 Football Clips is back with a massive, exciting update!

✨ Fresh M3 Expressive UI: Sleek modern look with an ultra-smooth droplet theme transition!
⚡ Instant Highlights & Scores: Watch official HD match recaps and real-time scores across top leagues with zero lag.
🧭 Sleek Floating Bar: Effortlessly browse fixtures and your favorite clubs.
🛠️ Faster & Smoother: Speed boosts and stability fixes for the ultimate matchday experience. Enjoy the game! ⚽🔥"""
    }
]

def get_access_token():
    with open(KEY_FILE, "r") as f:
        key_data = json.load(f)
        
    iat = int(time.time())
    payload = {
        "iss": key_data["client_email"],
        "sub": key_data["client_email"],
        "aud": "https://oauth2.googleapis.com/token",
        "iat": iat,
        "exp": iat + 3600,
        "scope": "https://www.googleapis.com/auth/androidpublisher"
    }
    
    encoded_jwt = jwt.encode(payload, key_data["private_key"], algorithm="RS256")
    resp = requests.post("https://oauth2.googleapis.com/token", data={
        "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
        "assertion": encoded_jwt
    })
    return resp.json()["access_token"]

def main():
    token = get_access_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    print("Creating edit session...")
    edit_resp = requests.post(
        f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits",
        headers=headers
    )
    edit_id = edit_resp.json()["id"]
    print(f"Edit ID: {edit_id}")

    try:
        track_url = f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}/tracks/production"
        current_track = requests.get(track_url, headers=headers).json()
        print("Current production track:", current_track)

        releases = current_track.get("releases", [])
        if not releases:
            print("No releases found in production track!")
            return

        releases[0]["releaseNotes"] = RELEASE_NOTES
        update_payload = {
            "track": "production",
            "releases": releases
        }

        print("Updating production release notes...")
        up_resp = requests.put(track_url, headers=headers, json=update_payload)
        if up_resp.status_code not in (200, 201):
            print("Failed to update track:", up_resp.status_code, up_resp.text)
            return

        print("Committing edit...")
        com_resp = requests.post(
            f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}:commit",
            headers=headers
        )
        if com_resp.status_code == 200:
            print("[SUCCESS] Production release notes updated and committed successfully!")
        else:
            print("[FAIL] Commit failed:", com_resp.status_code, com_resp.text)

    except Exception as e:
        print("Exception:", e)
        requests.delete(f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}", headers=headers)

if __name__ == "__main__":
    main()
