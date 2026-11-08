"""Update Multilingual Store Listings on Google Play Console."""

import argparse
import json
import os
import sys
import time

import jwt
import requests

from scripts.store_listings_catalog import (
    APP_NAME,
    LISTINGS,
    RELEASE_NOTES,
    SCREENSHOT_FILES,
)

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

PACKAGE_NAME = "com.dirzaaulia.footballclips"
KEY_FILE = (
    "play-console-key.json"
    if os.path.exists("play-console-key.json")
    else ("fastlane/play-console-key.json" if os.path.exists("fastlane/play-console-key.json") else "play-key.json")
)


def get_access_token() -> str:
    with open(KEY_FILE) as f:
        key_data = json.load(f)

    iat = int(time.time())
    payload = {
        "iss": key_data["client_email"],
        "sub": key_data["client_email"],
        "aud": "https://oauth2.googleapis.com/token",
        "iat": iat,
        "exp": iat + 3600,
        "scope": "https://www.googleapis.com/auth/androidpublisher",
    }
    encoded_jwt = jwt.encode(payload, key_data["private_key"], algorithm="RS256")
    resp = requests.post(
        "https://oauth2.googleapis.com/token",
        data={
            "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
            "assertion": encoded_jwt,
        },
    )
    return resp.json()["access_token"]


def main():
    parser = argparse.ArgumentParser(description="Update Multilingual Store Listings on Google Play Console.")
    parser.add_argument("--dry-run", action="store_true", help="Validate without committing")
    args = parser.parse_args()

    print("==================================================")
    print(f"  Google Play Multilingual Listing Updater ({PACKAGE_NAME})")
    print(f"  Title: {APP_NAME} | Dry Run: {'YES' if args.dry_run else 'NO'}")
    print("==================================================")

    token = get_access_token()
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}

    edit_resp = requests.post(
        f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits",
        headers=headers,
    )
    edit_id = edit_resp.json()["id"]
    print(f"  Active Edit ID: {edit_id}")

    try:
        for item in LISTINGS:
            lang = item["lang"]
            payload = {
                "language": lang,
                "title": item["title"],
                "shortDescription": item["short"],
                "fullDescription": item["full"],
            }
            res = requests.put(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}",
                headers=headers,
                json=payload,
            )
            print(f"  [{'OK' if res.status_code in (200, 201) else 'FAIL'}] Listing '{lang}'")

        icon_path = "app/src/androidMain/ic_launcher-playstore.png"
        if os.path.exists(icon_path):
            for item in LISTINGS:
                lang = item["lang"]
                with open(icon_path, "rb") as icon_file:
                    icon_headers = {"Authorization": f"Bearer {token}", "Content-Type": "image/png"}
                    icon_url = (
                        f"https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/"
                        f"{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}/icon"
                    )
                    requests.post(icon_url, headers=icon_headers, data=icon_file)

        for item in LISTINGS:
            lang = item["lang"]
            requests.delete(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}/phoneScreenshots",
                headers=headers,
            )
            for img_path in SCREENSHOT_FILES:
                if not os.path.exists(img_path):
                    continue
                with open(img_path, "rb") as img_file:
                    up_headers = {"Authorization": f"Bearer {token}", "Content-Type": "image/png"}
                    img_url = (
                        f"https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/"
                        f"{PACKAGE_NAME}/edits/{edit_id}/listings/{lang}/phoneScreenshots"
                    )
                    requests.post(img_url, headers=up_headers, data=img_file)

        track_url = f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}/tracks/production"
        track_resp = requests.get(track_url, headers=headers)
        if track_resp.status_code == 200:
            releases = track_resp.json().get("releases", [])
            if releases:
                releases[0]["releaseNotes"] = RELEASE_NOTES
                requests.put(track_url, headers=headers, json={"track": "production", "releases": releases})

        if args.dry_run:
            requests.post(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}:validate",
                headers=headers,
            )
            requests.delete(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}",
                headers=headers,
            )
        else:
            requests.post(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}:commit",
                headers=headers,
            )
    except Exception as e:
        print("Exception occurred:", e)
        try:
            requests.delete(
                f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}",
                headers=headers,
            )
        except Exception:
            pass


if __name__ == "__main__":
    main()
