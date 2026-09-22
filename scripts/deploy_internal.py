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
KEY_FILE = "play-console-key.json" if os.path.exists("play-console-key.json") else ("fastlane/play-console-key.json" if os.path.exists("fastlane/play-console-key.json") else "play-key.json")
AAB_PATH = "app/build/outputs/bundle/release/app-release.aab"

INTERNAL_RELEASE_NOTES = [
    {
        "language": "en-US",
        "text": "• Internal testing build with latest stability enhancements and bug fixes.\n• Fixed video playback in bottom sheet player.\n• RevenueCat billing and subscription flow improvements.\n• AdMob layout adjustments and performance optimizations."
    },
    {
        "language": "id",
        "text": "• Versi pengujian internal dengan perbaikan bug dan peningkatan stabilitas.\n• Perbaikan pemutaran video pada bottom sheet player.\n• Peningkatan sistem langganan & pembelian RevenueCat.\n• Pengoptimalan tata letak AdMob dan performa aplikasi."
    },
    {
        "language": "es-419",
        "text": "• Versión de prueba interna con mejoras de estabilidad y correcciones.\n• Corrección de reproducción de video en el reproductor.\n• Mejoras en el flujo de suscripción y facturación de RevenueCat.\n• Optimizaciones de diseño de AdMob y rendimiento general."
    },
    {
        "language": "es-ES",
        "text": "• Versión de prueba interna con mejoras de estabilidad y correcciones.\n• Corrección de reproducción de video en el reproductor.\n• Mejoras en el flujo de suscripción y facturación de RevenueCat.\n• Optimizaciones de diseño de AdMob y rendimiento general."
    },
    {
        "language": "pt-BR",
        "text": "• Versão de teste interno com melhorias de estabilidade e correções de bugs.\n• Correção na reprodução de vídeo no player inferior.\n• Aprimoramentos no fluxo de faturamento e assinaturas do RevenueCat.\n• Otimizações de layout do AdMob e desempenho do aplicativo."
    }
]

PROD_RELEASE_NOTES = [
    {
        "language": "en-US",
        "text": "• Fixed video playback in player bottom sheet.\n• Expanded compatibility and smoother overall experience.\n• Performance improvements and stability enhancements."
    },
    {
        "language": "id",
        "text": "• Perbaikan pemutaran video pada pemutar video.\n• Peningkatan kompatibilitas untuk pengalaman yang lebih lancar.\n• Peningkatan stabilitas dan performa aplikasi."
    },
    {
        "language": "es-419",
        "text": "• Se corrigió la reproducción de video en el reproductor.\n• Mayor compatibilidad para una experiencia más fluida.\n• Mejoras de estabilidad y optimización de rendimiento."
    },
    {
        "language": "es-ES",
        "text": "• Se corrigió la reproducción de video en el reproductor.\n• Mayor compatibilidad para una experiencia más fluida.\n• Mejoras de estabilidad y optimización de rendimiento."
    },
    {
        "language": "pt-BR",
        "text": "• Correção na reprodução de vídeo no player.\n• Maior compatibilidade para uma experiência mais suave.\n• Melhorias de desempenho e estabilidade geral."
    }
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
    parser = argparse.ArgumentParser(description="Deploy Release AAB to Google Play Console tracks.")
    parser.add_argument("--dry-run", action="store_true", help="Validate edit without committing changes to Play Store.")
    parser.add_argument("--aab", default=AAB_PATH, help="Path to release AAB bundle.")
    parser.add_argument("--tracks", nargs="+", default=["internal"], choices=["internal", "production", "beta", "alpha"], help="Tracks to deploy to (e.g. --tracks internal production)")
    args = parser.parse_args()

    aab_path = args.aab

    print("==================================================")
    print("  Google Play Console Deployer")
    print(f"  Package: {PACKAGE_NAME}")
    print(f"  Tracks: {', '.join(args.tracks)}")
    print(f"  AAB Path: {aab_path}")
    print(f"  Dry Run: {'YES (Will validate and not publish)' if args.dry_run else 'NO (LIVE DEPLOY)'}")
    print("==================================================")

    if not os.path.exists(aab_path):
        print(f"Error: Release AAB bundle '{aab_path}' does not exist. Please run `./gradlew.bat :app:bundleRelease` first.")
        sys.exit(1)

    aab_size_mb = os.path.getsize(aab_path) / (1024 * 1024)
    print(f"  Bundle Size: {aab_size_mb:.2f} MB")

    token = get_access_token()
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    # 1. Create an Edit session
    print("\n[1/4] Creating new edit session...")
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
        # 2. Upload the Release AAB Bundle
        print("\n[2/4] Uploading release App Bundle (.aab)...")
        upload_url = (
            f"https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/"
            f"{PACKAGE_NAME}/edits/{edit_id}/bundles?uploadType=media"
        )
        upload_headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/octet-stream"
        }
        with open(aab_path, "rb") as f:
            aab_data = f.read()
            upload_resp = requests.post(upload_url, headers=upload_headers, data=aab_data)

        if upload_resp.status_code not in (200, 201):
            print(f"  [FAIL] Failed to upload AAB: {upload_resp.status_code} {upload_resp.text}")
            sys.exit(1)

        bundle_info = upload_resp.json()
        version_code = bundle_info.get("versionCode")
        sha256 = bundle_info.get("sha256")
        print(f"  [OK] Successfully uploaded AAB!")
        print(f"       Version Code: {version_code}")
        print(f"       SHA256: {sha256}")

        # 3. Assign to specified tracks
        for track_name in args.tracks:
            notes = PROD_RELEASE_NOTES if track_name == "production" else INTERNAL_RELEASE_NOTES
            print(f"\n[3/4] Assigning Version Code {version_code} to '{track_name}' track...")
            track_url = (
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/"
                f"{PACKAGE_NAME}/edits/{edit_id}/tracks/{track_name}"
            )
            track_payload = {
                "track": track_name,
                "releases": [
                    {
                        "name": f"3.0.{version_code}",
                        "versionCodes": [str(version_code)],
                        "status": "completed",
                        "releaseNotes": notes
                    }
                ]
            }
            track_resp = requests.put(track_url, headers=headers, json=track_payload)
            if track_resp.status_code not in (200, 201):
                print(f"  [FAIL] Failed to assign track '{track_name}': {track_resp.status_code} {track_resp.text}")
                sys.exit(1)
            print(f"  [OK] Assigned release 3.0.{version_code} to '{track_name}' track.")

        # 4. Commit or Validate Edit
        if args.dry_run:
            print("\n[4/4] Validating edit (Dry-run mode)...")
            val_res = requests.post(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}:validate",
                headers=headers
            )
            if val_res.status_code == 200:
                print("  [OK] Deployment validation passed successfully! (Dry-run)")
            else:
                print(f"  [FAIL] Validation returned: {val_res.status_code} {val_res.text}")
            requests.delete(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}",
                headers=headers
            )
            print("  [OK] Draft edit session discarded cleanly.")
        else:
            print(f"\n[4/4] Committing release to Google Play track(s): {', '.join(args.tracks)}...")
            commit_res = requests.post(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}:commit",
                headers=headers
            )
            if commit_res.status_code == 200:
                print(f"  [SUCCESS] Release 3.0.{version_code} successfully deployed to: {', '.join(args.tracks)}!")
            else:
                print(f"  [FAIL] Failed to commit changes: {commit_res.status_code} {commit_res.text}")
                sys.exit(1)

    except Exception as e:
        print(f"\n[EXCEPTION] Error during deployment: {e}")
        try:
            requests.delete(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}",
                headers=headers
            )
            print("  Discarded draft edit session.")
        except:
            pass
        sys.exit(1)

if __name__ == "__main__":
    main()
