#!/usr/bin/env python3
"""
Deploy Next.js Web Platform and WASM Telemetry App to Firebase Hosting.
"""
import shutil
import subprocess
import sys
import os

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
        sys.stderr.reconfigure(encoding='utf-8', errors='replace')
    except Exception:
        pass

def run_command(cmd, cwd=None):
    print(f"\n🚀 Running: {' '.join(cmd) if isinstance(cmd, list) else cmd}")
    res = subprocess.run(cmd, cwd=cwd, shell=True, stdin=subprocess.DEVNULL)
    if res.returncode != 0:
        print(f"❌ Command failed with return code {res.returncode}")
        sys.exit(res.returncode)

def main():
    root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    web_dir = os.path.join(root_dir, "web")
    out_telemetry_dir = os.path.join(web_dir, "out", "telemetry")

    print("==================================================")
    print("⚽ Deploying Football Clips Web & Telemetry Hub")
    print("==================================================")

    # 1. Build Next.js
    print("\n📦 Building Next.js Web application...")
    run_command("npm run build", cwd=web_dir)

    # 1.5 Ensure clean /match/index.html fallback exists for Firebase SPA routing
    out_match_dir = os.path.join(web_dir, "out", "match")
    match_html_path = os.path.join(web_dir, "out", "match.html")
    match_index_path = os.path.join(out_match_dir, "index.html")
    if not os.path.exists(out_match_dir):
        os.makedirs(out_match_dir, exist_ok=True)
    if os.path.exists(match_html_path):
        shutil.copyfile(match_html_path, match_index_path)
        print(f"📄 Generated clean Match Centre fallback shell from match.html at {match_index_path}")
    elif os.path.exists(match_index_path):
        print(f"📄 Verified Match Centre fallback shell at {match_index_path}")

    public_telemetry_dir = os.path.join(web_dir, "public", "telemetry")
    telemetry_html_path = os.path.join(web_dir, "out", "telemetry.html")
    telemetry_index_path = os.path.join(out_telemetry_dir, "index.html")

    # 2. Setup Next.js Telemetry distribution
    print("\n📋 Preparing Next.js Telemetry Hub static assets...")
    if not os.path.exists(out_telemetry_dir):
        os.makedirs(out_telemetry_dir, exist_ok=True)
    if os.path.exists(telemetry_html_path):
        shutil.copyfile(telemetry_html_path, telemetry_index_path)
        print(f"📄 Generated clean Telemetry Hub shell at {telemetry_index_path}")
    elif os.path.exists(telemetry_index_path):
        print(f"📄 Verified Telemetry Hub shell at {telemetry_index_path}")

    # Also sync to public_telemetry_dir for local preview
    if not os.path.exists(public_telemetry_dir):
        os.makedirs(public_telemetry_dir, exist_ok=True)
    if os.path.exists(telemetry_index_path):
        shutil.copyfile(telemetry_index_path, os.path.join(public_telemetry_dir, "index.html"))

    # 3. Deploy to Firebase Hosting
    print("\n🔥 Deploying to Firebase Hosting (footballclips & telemetry)...")
    run_command("firebase deploy --only hosting", cwd=root_dir)

    print("\n✅ Deployment completed successfully!")
    print("🌐 URLs:")
    print("   • Primary Custom Domain: https://fc.dirzaaulia.com")
    print("   • Telemetry Console:     https://fc.dirzaaulia.com/telemetry")
    print("   • Standalone Telemetry:  https://footballclips-telemetry.web.app")
    print("   • Firebase Default URL:  https://footballclips.web.app")

if __name__ == "__main__":
    main()
