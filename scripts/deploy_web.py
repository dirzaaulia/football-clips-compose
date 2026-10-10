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
    res = subprocess.run(cmd, cwd=cwd, shell=True)
    if res.returncode != 0:
        print(f"❌ Command failed with return code {res.returncode}")
        sys.exit(res.returncode)

def main():
    root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    web_dir = os.path.join(root_dir, "web")
    wasm_dir = os.path.join(root_dir, "webApp", "build", "dist", "wasmJs", "productionExecutable")
    out_telemetry_dir = os.path.join(web_dir, "out", "telemetry")

    print("==================================================")
    print("⚽ Deploying Football Clips Web & WASM Telemetry")
    print("==================================================")

    # 1. Build Next.js
    print("\n📦 Building Next.js Web application...")
    run_command("npm run build", cwd=web_dir)

    # 2. Copy WASM telemetry build into Next.js static export for /telemetry route
    if os.path.exists(wasm_dir):
        print(f"\n📋 Syncing WASM Telemetry assets to {out_telemetry_dir}...")
        if os.path.exists(out_telemetry_dir):
            shutil.rmtree(out_telemetry_dir)
        shutil.copytree(wasm_dir, out_telemetry_dir)
        index_path = os.path.join(out_telemetry_dir, "index.html")
        if os.path.exists(index_path):
            with open(index_path, "r", encoding="utf-8") as f:
                content = f.read()
            if "<head>" in content and "<base" not in content:
                content = content.replace("<head>", '<head>\n    <base href="/telemetry/">')
            with open(index_path, "w", encoding="utf-8") as f:
                f.write(content)
    else:
        print("\n⚠️ WASM Telemetry build not found. Building with Gradle...")
        gradle_cmd = "gradlew.bat" if sys.platform == "win32" else "./gradlew"
        run_command(f"{gradle_cmd} :webApp:wasmJsBrowserDistribution", cwd=root_dir)
        shutil.copytree(wasm_dir, out_telemetry_dir)
        index_path = os.path.join(out_telemetry_dir, "index.html")
        if os.path.exists(index_path):
            with open(index_path, "r", encoding="utf-8") as f:
                content = f.read()
            if "<head>" in content and "<base" not in content:
                content = content.replace("<head>", '<head>\n    <base href="/telemetry/">')
            with open(index_path, "w", encoding="utf-8") as f:
                f.write(content)

    # 3. Deploy to Firebase Hosting
    print("\n🔥 Deploying to Firebase Hosting (footballclips & telemetry)...")
    run_command("firebase deploy --only hosting", cwd=root_dir)

    print("\n✅ Deployment completed successfully!")
    print("🌐 URLs:")
    print("   • Primary Custom Domain: https://fc.dirzaaulia.com")
    print("   • Telemetry on Domain:   https://fc.dirzaaulia.com/telemetry")
    print("   • Standalone Telemetry:  https://footballclips-telemetry.web.app")
    print("   • Firebase Default URL:  https://footballclips.web.app")

if __name__ == "__main__":
    main()
