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
        "text": "• Perbaikan masalah masuk (sign-in) dengan Akun Google untuk kelancaran pendaftaran & akses pembelian Premium.\n• Pengoptimalan ukuran aplikasi (33% lebih kecil & hemat penyimpanan).\n• Peningkatan performa dan stabilitas aplikasi."
    },
    {
        "language": "en-US",
        "text": "• Fixed Google sign-in issue for seamless Premium access and purchases.\n• Optimized app size (33% smaller APK & reduced storage footprint).\n• Performance improvements and stability fixes."
    },
    {
        "language": "es-419",
        "text": "• Corrección en el inicio de sesión con Google para acceso y compra de versión Premium.\n• Optimización del tamaño de la aplicación (33% más pequeña).\n• Mejoras de rendimiento y estabilidad."
    },
    {
        "language": "es-ES",
        "text": "• Corrección en el inicio de sesión con Google para acceso y compra de versión Premium.\n• Optimización del tamaño de la aplicación (33% más pequeña).\n• Mejoras de rendimiento y estabilidad."
    },
    {
        "language": "pt-BR",
        "text": "• Correção no login com Google para acesso e compra de versão Premium.\n• Otimização do tamanho do aplicativo (33% menor).\n• Melhorias de desempenho e estabilidade."
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
