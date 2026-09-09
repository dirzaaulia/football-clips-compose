import os
import sys
import json
import time
import argparse
import requests
import jwt

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

PACKAGE_NAME = "com.dirzaaulia.footballclips"
KEY_FILE = "fastlane/play-console-key.json"

APP_NAME = "Football Highlights & Clips"

SHORT_DESCRIPTION_EN = "Watch football video highlights, track live scores, and fixtures in one app."
SHORT_DESCRIPTION_ID = "Skor langsung, jadwal, dan cuplikan video sepak bola dari liga-liga top dunia."

FULL_DESCRIPTION_EN = """Welcome to Football Highlights & Clips (FC) – your ultimate companion for football highlights, match clips, live scores, and fixtures!

Catch the best moments from the top football leagues and competitions around the world. Never miss a goal, dramatic comeback, or match-winning play.

⚡ KEY FEATURES:

• 📺 Official Match Highlights & Clips
Watch high-quality match recaps and best plays sourced and embedded directly via official broadcaster channels using official YouTube Player APIs.

• ⏱️ Real-Time Live Scores & Schedules
Stay updated with match fixtures, kickoff times, live scores, and results across major leagues.

• 🔍 Multi-League Filtering & Search
Quickly find your favorite teams and filter by competition: Premier League, La Liga, Serie A, Bundesliga, Champions League, and more.

• ☁️ Cross-Device Experience
Sign in with Google to seamlessly sync your profile and access Football Highlights & Clips on both Android and Web (fc.dirzaaulia.com).

• 🚫 Ad-Free Premium (Optional)
Enjoy an uninterrupted, pure football experience with an optional lifetime Remove Ads upgrade.

---
IMPORTANT DISCLAIMERS & LEGAL:
1. Football Highlights & Clips does not host, upload, or broadcast copyrighted video files. All video highlights are embedded directly from official verified channels via the official YouTube Player API in full compliance with YouTube Terms of Service.
2. All team names, logos, trademarks, and league assets belong to their respective copyright owners.

• Privacy Policy: https://fc.dirzaaulia.com/privacy
• Terms of Service: https://fc.dirzaaulia.com/tnc
• Support: dirzaaulia@gmail.com
"""

FULL_DESCRIPTION_ID = """Selamat datang di Football Highlights & Clips (FC) – aplikasi lengkap untuk cuplikan video pertandingan sepak bola, skor langsung, dan jadwal pertandingan!

Saksikan momen-momen terbaik dari liga dan turnamen sepak bola terkemuka di dunia. Jangan lewatkan setiap gol spektakuler, aksi dramatis, dan hasil pertandingan tim favorit Anda.

⚡ FITUR UTAMA:

• 📺 Cuplikan Pertandingan Resmi
Tonton rangkuman video pertandingan resmi berkualitas tinggi yang disematkan langsung dari saluran penyiar resmi menggunakan YouTube Player API resmi.

• ⏱️ Live Skor & Jadwal Pertandingan
Dapatkan pembaruan langsung jadwal kick-off, skor live, dan hasil akhir pertandingan liga-liga top dunia secara real-time.

• 🔍 Filter Liga & Pencarian Cepat
Temukan klub favorit Anda dengan mudah melalui filter kompetisi: Premier League, La Liga, Serie A, Bundesliga, Liga Champions, dan lainnya.

• ☁️ Sinkronisasi Lintas Perangkat
Masuk dengan Akun Google untuk menyinkronkan profil Anda antara aplikasi Android dan Web (fc.dirzaaulia.com).

• 🚫 Opsi Bebas Iklan (Premium)
Nikmati pengalaman menonton sepak bola tanpa jeda dengan opsi pembelian satu kali Hapus Iklan.

---
INFORMASI HAK CIPTA & KEPATUHAN:
1. Football Highlights & Clips tidak menyimpan, mengunggah, atau menyiarkan file video ilegal/bajakan. Semua cuplikan video disematkan langsung dari saluran resmi yang terverifikasi menggunakan YouTube Player API resmi sesuai dengan Ketentuan Layanan YouTube.
2. Semua nama tim, logo, merek dagang, dan aset kompetisi adalah milik pemegang hak cipta masing-masing.

• Kebijakan Privasi: https://fc.dirzaaulia.com/privacy
• Syarat & Ketentuan: https://fc.dirzaaulia.com/tnc
• Kontak & Bantuan: dirzaaulia@gmail.com
"""

SCREENSHOT_FILES = [
    "screenshots/store_listing/01_highlights_banner.png",
    "screenshots/store_listing/02_fixtures_banner.png",
    "screenshots/store_listing/03_leagues_banner.png",
    "screenshots/store_listing/04_premium_banner.png",
]

def get_access_token():
    if not os.path.exists(KEY_FILE):
        print(f"Error: Key file '{KEY_FILE}' not found.")
        sys.exit(1)
        
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
    
    if resp.status_code != 200:
        print(f"Failed to authenticate with Google OAuth: {resp.status_code} {resp.text}")
        sys.exit(1)
        
    return resp.json()["access_token"]

def main():
    parser = argparse.ArgumentParser(description="Upload listing details and screenshots to Google Play Console.")
    parser.add_argument("--dry-run", action="store_true", help="Validate edit without committing changes to Play Store.")
    args = parser.parse_args()

    print("==================================================")
    print("  Google Play Store Listing & Asset Updater")
    print(f"  Package: {PACKAGE_NAME}")
    print(f"  Dry Run: {'YES (Will not publish)' if args.dry_run else 'NO (Will publish changes)'}")
    print("==================================================")

    token = get_access_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    # 1. Create an Edit session
    print("[1/4] Creating new edit session...")
    edit_resp = requests.post(
        f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits",
        headers=headers
    )
    if edit_resp.status_code != 200:
        print(f"Error creating edit: {edit_resp.status_code} {edit_resp.text}")
        sys.exit(1)
        
    edit_id = edit_resp.json()["id"]
    print(f"  [OK] Active Edit ID: {edit_id}")

    try:
        # 2. Update Listings (id and en-US)
        print("[2/4] Updating store listing text (Title, Short & Full Description)...")
        listings_to_update = [
            {
                "lang": "id",
                "title": APP_NAME,
                "short": SHORT_DESCRIPTION_ID,
                "full": FULL_DESCRIPTION_ID
            },
            {
                "lang": "en-US",
                "title": APP_NAME,
                "short": SHORT_DESCRIPTION_EN,
                "full": FULL_DESCRIPTION_EN
            }
        ]

        for item in listings_to_update:
            lang = item["lang"]
            payload = {
                "language": lang,
                "title": item["title"],
                "shortDescription": item["short"],
                "fullDescription": item["full"]
            }
            res = requests.put(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}",
                headers=headers,
                json=payload
            )
            if res.status_code in (200, 201):
                print(f"  [OK] Updated listing for '{lang}' successfully (Title: '{item['title']}')")
            else:
                print(f"  [FAIL] Failed to update listing for '{lang}': {res.status_code} {res.text}")

        # 3. Upload App Icon
        icon_path = "app/src/androidMain/ic_launcher-playstore.png"
        if os.path.exists(icon_path):
            print("[3/5] Updating official app icon (512x512)...")
            for lang in ["id", "en-US"]:
                with open(icon_path, "rb") as icon_file:
                    icon_headers = {
                        "Authorization": f"Bearer {token}",
                        "Content-Type": "image/png"
                    }
                    icon_url = (
                        f"https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/"
                        f"{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}/icon"
                    )
                    icon_res = requests.post(icon_url, headers=icon_headers, data=icon_file)
                    if icon_res.status_code in (200, 201):
                        print(f"  [OK] Uploaded app icon for '{lang}' successfully")
                    else:
                        print(f"  [FAIL] Failed to upload app icon for '{lang}': {icon_res.status_code} {icon_res.text}")

        # 4. Upload Screenshots
        print("[4/5] Updating promotional screenshots...")
        for lang in ["id", "en-US"]:
            # Check existing and delete
            del_res = requests.delete(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}/phoneScreenshots",
                headers=headers
            )
            print(f"  Cleared old phone screenshots for '{lang}' (Status: {del_res.status_code})")

            # Upload each screenshot
            for idx, img_path in enumerate(SCREENSHOT_FILES, 1):
                if not os.path.exists(img_path):
                    print(f"  Warning: Screenshot not found at {img_path}")
                    continue
                
                with open(img_path, "rb") as img_file:
                    upload_headers = {
                        "Authorization": f"Bearer {token}",
                        "Content-Type": "image/png"
                    }
                    upload_url = (
                        f"https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/"
                        f"{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}/phoneScreenshots"
                    )
                    up_res = requests.post(upload_url, headers=upload_headers, data=img_file)
                    if up_res.status_code in (200, 201):
                        print(f"  [OK] Uploaded screenshot {idx}/4 for '{lang}': {os.path.basename(img_path)}")
                    else:
                        print(f"  [FAIL] Failed to upload screenshot {idx} for '{lang}': {up_res.status_code} {up_res.text}")

        # 5. Commit or Validate Edit
        if args.dry_run:
            print("[5/5] Validating edit (Dry-run mode)...")
            val_res = requests.post(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}:validate",
                headers=headers
            )
            if val_res.status_code == 200:
                print("  [OK] Edit validation passed successfully! No changes were published to users.")
            else:
                print(f"  [FAIL] Validation returned: {val_res.status_code} {val_res.text}")
            
            # Cleanly delete the draft edit so it doesn't leave an uncommitted draft
            requests.delete(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}",
                headers=headers
            )
            print("  [OK] Draft edit session safely discarded.")
        else:
            print("[5/5] Committing changes to Google Play Console...")
            commit_res = requests.post(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}:commit",
                headers=headers
            )
            if commit_res.status_code == 200:
                print("  [SUCCESS] All changes and screenshots have been committed to Google Play Console!")
            else:
                print(f"  [FAIL] Failed to commit changes: {commit_res.status_code} {commit_res.text}")

    except Exception as e:
        print(f"An exception occurred: {e}")
        requests.delete(
            f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}",
            headers=headers
        )

if __name__ == "__main__":
    main()
