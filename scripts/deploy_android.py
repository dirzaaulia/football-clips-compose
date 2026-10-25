#!/usr/bin/env python3
"""
Android Deployment & In-App Update Automation Runner.

Supports:
  python scripts/deploy_android.py --production --force      (Priority 5: Immediate Fullscreen Blocking)
  python scripts/deploy_android.py --production --prompt     (Priority 2: Flexible Background Prompt)
  python scripts/deploy_android.py --production --priority N (Explicit Priority 0-5)
  python scripts/deploy_android.py --internal               (Internal Testing Track)
"""
import argparse
import os
import re
import subprocess
import sys
import time

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    except Exception:
        pass


def run_cmd(cmd, cwd=None, capture=False, check=True):
    """Executes a shell command."""
    if isinstance(cmd, list):
        cmd_str = " ".join(cmd)
    else:
        cmd_str = cmd
    print(f"🚀 Executing: {cmd_str}")
    res = subprocess.run(
        cmd,
        cwd=cwd,
        shell=True,
        capture_output=capture,
        text=True,
    )
    if check and res.returncode != 0:
        err = res.stderr.strip() if res.stderr else f"Exit code {res.returncode}"
        print(f"❌ Command failed: {err}")
        sys.exit(res.returncode)
    return res


def get_version_info(root_dir):
    """Reads version.properties and returns a dictionary of version fields."""
    prop_path = os.path.join(root_dir, "version.properties")
    props = {}
    if os.path.exists(prop_path):
        with open(prop_path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#") and "=" in line:
                    k, v = line.split("=", 1)
                    props[k.strip()] = v.strip()
    return props, prop_path


def increment_build_number(prop_path, props):
    """Increments VERSION_BUILD in version.properties and saves it."""
    curr_build = int(props.get("VERSION_BUILD", "89832570"))
    new_build = curr_build + 1
    props["VERSION_BUILD"] = str(new_build)

    lines = [
        "# Auto-incremented by deploy_android.py runner\n",
        f"VERSION_BUILD={new_build}\n",
        f"VERSION_MAJOR={props.get('VERSION_MAJOR', '3')}\n",
        f"VERSION_MINOR={props.get('VERSION_MINOR', '0')}\n",
        f"VERSION_PATCH={props.get('VERSION_PATCH', '1')}\n",
    ]
    with open(prop_path, "w", encoding="utf-8") as f:
        f.writelines(lines)
    return new_build


def main():
    parser = argparse.ArgumentParser(
        description="Deploy Android release bundle to Google Play with In-App Update Priority control."
    )
    track_group = parser.add_mutually_exclusive_group()
    track_group.add_argument(
        "--production", action="store_true", help="Deploy to Google Play Production track"
    )
    track_group.add_argument(
        "--internal", action="store_true", help="Deploy to Google Play Internal Testing track"
    )
    track_group.add_argument(
        "--track",
        choices=["internal", "alpha", "beta", "production"],
        help="Target Google Play track",
    )

    priority_group = parser.add_mutually_exclusive_group()
    priority_group.add_argument(
        "--force",
        action="store_true",
        help="Enforce immediate force update (Google Play Priority 5)",
    )
    priority_group.add_argument(
        "--prompt",
        action="store_true",
        help="Show flexible update prompt (Google Play Priority 2)",
    )
    priority_group.add_argument(
        "--silent",
        action="store_true",
        help="Silent release with no prompt (Google Play Priority 0)",
    )
    priority_group.add_argument(
        "--priority",
        type=int,
        choices=[0, 1, 2, 3, 4, 5],
        help="Explicit Google Play In-App Update priority (0-5)",
    )

    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Validate parameters and pre-flight without git push or GitHub Actions dispatch",
    )
    parser.add_argument(
        "--skip-build",
        action="store_true",
        help="Skip local pre-flight compile check",
    )

    args = parser.parse_args()

    # Determine track
    if args.production:
        track = "production"
    elif args.internal:
        track = "internal"
    elif args.track:
        track = args.track
    else:
        # Default track: if priority specified, assume production, else internal
        track = "production" if (args.force or args.prompt or args.priority is not None) else "internal"

    # Determine priority
    if args.force:
        priority = 5
    elif args.prompt:
        priority = 2
    elif args.silent:
        priority = 0
    elif args.priority is not None:
        priority = args.priority
    else:
        priority = 2 if track == "production" else 0

    root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    props, prop_path = get_version_info(root_dir)
    major = props.get("VERSION_MAJOR", "3")
    minor = props.get("VERSION_MINOR", "0")
    patch = props.get("VERSION_PATCH", "1")
    current_build = int(props.get("VERSION_BUILD", "89832570"))
    target_build = current_build + (0 if args.dry_run else 1)

    version_name = (
        f"{major}.{minor}.{patch} ({target_build})"
        if track == "production"
        else f"{major}.{minor}.{patch}-{track}.{target_build}"
    )

    print("==================================================")
    print("⚽ Football Clips Android Release Deployment")
    print("==================================================")
    print(f"📦 Target Track:    {track.upper()}")
    print(f"🔢 Version Name:    {version_name}")
    print(f"⚙️  Version Code:    {target_build}")
    print(f"⚡ Update Priority: Level {priority} ({'FORCE/IMMEDIATE' if priority >= 4 else ('PROMPT/FLEXIBLE' if priority >= 2 else 'SILENT')})")
    print(f"🛡️  Dry Run Mode:    {'ENABLED' if args.dry_run else 'DISABLED'}")
    print("==================================================")

    # 1. Pre-flight compile check
    gradle_cmd = "gradlew.bat" if sys.platform == "win32" else "./gradlew"
    if not args.skip_build:
        print("\n🔍 Running local pre-flight compile check...")
        run_cmd(f"{gradle_cmd} :androidApp:compileReleaseKotlin", cwd=root_dir)
        print("✅ Pre-flight compilation passed!")

    if args.dry_run:
        print("\n✨ Dry run completed successfully. No git or release changes dispatched.")
        return

    # 2. Increment build number
    print(f"\n📈 Incrementing VERSION_BUILD: {current_build} -> {target_build}...")
    increment_build_number(prop_path, props)

    # 3. Commit and push version.properties
    commit_msg = f"chore(release): bump VERSION_BUILD to {target_build} [track: {track}, priority: {priority}]"
    print(f"\n💾 Committing version bump: '{commit_msg}'...")
    run_cmd(f'git add version.properties', cwd=root_dir)
    run_cmd(f'git commit -m "{commit_msg}"', cwd=root_dir)
    print("⬆️  Pushing to origin main...")
    run_cmd("git push origin main", cwd=root_dir)

    # 4. Dispatch GitHub Actions workflow
    print(f"\n🚀 Dispatching GitHub Actions 'play_console_deploy.yml'...")
    dispatch_cmd = (
        f"gh workflow run play_console_deploy.yml "
        f"-f track={track} "
        f"-f update_priority={priority} "
        f"-f status=completed"
    )
    run_cmd(dispatch_cmd, cwd=root_dir)

    # 5. Locate and monitor workflow run
    print("\n⏳ Fetching triggered workflow run...")
    time.sleep(4)
    run_res = run_cmd(
        "gh run list --workflow=play_console_deploy.yml --limit=1 --json databaseId,url,status",
        cwd=root_dir,
        capture=True,
        check=False,
    )

    if run_res.returncode == 0 and run_res.stdout:
        print(f"📋 Workflow Status:\n{run_res.stdout.strip()}")
        m = re.search(r'"databaseId":\s*(\d+)', run_res.stdout)
        if m:
            run_id = m.group(1)
            print(f"🔗 Monitoring workflow run: https://github.com/dirzaaulia/football-clips-compose/actions/runs/{run_id}")
            print(f"💡 You can watch live logs using: gh run watch {run_id}")

    print("\n🎉 Deployment triggered successfully!")
    print(f"👉 Google Play will distribute version {target_build} on track '{track}' with In-App Update Priority {priority}.")


if __name__ == "__main__":
    main()
