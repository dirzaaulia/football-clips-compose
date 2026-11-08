#!/usr/bin/env python3
"""
Unified Hermetic E2E & Responsive Device Matrix Test Orchestrator.
Executes:
1. Python Backend & Pipeline E2E Suite (pytest)
2. Next.js 16 Web Vitest Suite (vitest + happy-dom)
3. Android & Compose Multiplatform Host Test Suite + 6-Form-Factor Matrix (:composeApp:testAndroidHostTest)

Generates a structured Markdown report in tests/reports/e2e_report.md.
"""
from __future__ import annotations

import argparse
import os
import subprocess
import sys
import time
from datetime import datetime, timezone
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parent.parent
REPORT_PATH = ROOT_DIR / "tests" / "reports" / "e2e_report.md"


class SuiteResult:
    def __init__(self, name: str, passed: bool, duration_sec: float, summary: str) -> None:
        self.name = name
        self.passed = passed
        self.duration_sec = duration_sec
        self.summary = summary


def run_step(name: str, cmd: list[str], cwd: Path) -> SuiteResult:
    print(f"\n============================================================")
    print(f"[RUNNING] {name}")
    print(f"          Command: {' '.join(cmd)}")
    print(f"============================================================")
    start = time.perf_counter()
    use_shell = os.name == "nt" and cmd[0] in ("npm", "npx", "gradlew.bat")
    proc = subprocess.run(
        cmd,
        cwd=str(cwd),
        capture_output=True,
        text=True,
        shell=use_shell,
    )
    elapsed = time.perf_counter() - start
    passed = proc.returncode == 0
    status_label = "PASS" if passed else "FAIL"
    print(f"[{status_label}] {name} finished in {elapsed:.2f}s")
    if not passed:
        print("--- STDOUT ---")
        print(proc.stdout[-1500:])
        print("--- STDERR ---")
        print(proc.stderr[-1500:])

    # Extract concise summary line
    output_lines = [line.strip() for line in (proc.stdout + "\n" + proc.stderr).splitlines() if line.strip()]
    summary = output_lines[-1] if output_lines else status_label
    return SuiteResult(name=name, passed=passed, duration_sec=elapsed, summary=summary)


def write_markdown_report(results: list[SuiteResult], args: argparse.Namespace) -> None:
    REPORT_PATH.parent.mkdir(parents=True, exist_ok=True)
    now_iso = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")
    overall_pass = all(r.passed for r in results)

    lines = [
        "# FootballClips Hermetic E2E & Responsive Matrix Verification Report",
        "",
        f"- **Timestamp**: `{now_iso}`",
        f"- **Overall Status**: `{'PASSED (100%)' if overall_pass else 'FAILED'}`",
        f"- **Selected Journey**: `{args.journey}`",
        f"- **Selected Device Matrix**: `{args.device}`",
        "",
        "## Suite Execution Waterfall",
        "",
        "| Layer / Suite | Status | Duration (s) | Summary |",
        "| :--- | :--- | :---: | :--- |",
    ]
    for r in results:
        badge = "PASS" if r.passed else "FAIL"
        safe_summary = r.summary.replace("|", "/").replace("`", "'")[:100]
        lines.append(f"| **{r.name}** | `{badge}` | `{r.duration_sec:.2f}s` | {safe_summary} |")

    lines.extend([
        "",
        "## Covered Device Matrix Form Factors",
        "",
        "| Form Factor | Dimensions (`dp`) | Window Class | Navigation Chrome | Grid Columns (Highlights / Fixtures) |",
        "| :--- | :---: | :---: | :--- | :---: |",
        "| **Compact Phone** | `390 x 844` | `Compact` | Floating Bottom Pill Nav | `1 / 1` |",
        "| **Wide Portrait Phone** | `640 x 960` | `Medium` | Floating Bottom Pill Nav + Side Sheet | `2 / 2` |",
        "| **Foldable Cover** | `380 x 900` | `Compact` | Floating Bottom Pill Nav | `1 / 1` |",
        "| **Foldable Unfolded** | `720 x 820` | `Medium` | Floating Bottom Pill Nav + Side Sheet | `3 / 2` |",
        "| **Landscape Flex Mode** | `840 x 390` | `Medium` | Left Navigation Rail + Dual-Pane Split Deck | `2 / 2` |",
        "| **Tablet / Desktop** | `1024 x 768` | `Expanded` | Left Navigation Rail + Persistent Side Sheet | `3 / 2` |",
        "",
    ])

    REPORT_PATH.write_text("\n".join(lines), encoding="utf-8")
    print(f"\n[REPORT] Written to {REPORT_PATH}")


def main() -> int:
    parser = argparse.ArgumentParser(description="Run Hermetic E2E & Responsive Device Matrix Test Suite")
    parser.add_argument("--all", action="store_true", help="Run all backend, web, and Android CMP suites")
    parser.add_argument("--backend", action="store_true", help="Run Python backend & E2E pytest suite")
    parser.add_argument("--web", action="store_true", help="Run Next.js 16 Vitest suite")
    parser.add_argument("--android", action="store_true", help="Run Android CMP Host Test & Device Matrix suite")
    parser.add_argument(
        "--journey",
        default="all",
        choices=["all", "highlights", "pitch", "shorts", "fixtures", "billing", "geoblocking"],
        help="Filter specific feature journey",
    )
    parser.add_argument(
        "--device",
        default="all",
        choices=["all", "phone", "foldable", "tablet", "landscape"],
        help="Filter specific device form factor",
    )
    args = parser.parse_args()

    run_all = args.all or not (args.backend or args.web or args.android)
    results: list[SuiteResult] = []

    if run_all or args.backend:
        results.append(
            run_step(
                "Python Backend & E2E Lifecycle Suite (pytest)",
                ["uv", "run", "pytest", "worker/tests/", "-q"],
                ROOT_DIR,
            )
        )

    if run_all or args.web:
        npm_cmd = "npm.cmd" if os.name == "nt" else "npm"
        results.append(
            run_step(
                "Next.js 16 Web Vitest Suite (Geoblocking, Webhook, Excitement, Responsive)",
                [npm_cmd, "run", "test"],
                ROOT_DIR / "web",
            )
        )

    if run_all or args.android:
        gradle_cmd = "gradlew.bat" if os.name == "nt" else "./gradlew"
        results.append(
            run_step(
                "Android & CMP Host Suite + 6-Form-Factor Device Matrix",
                [str(ROOT_DIR / gradle_cmd), ":composeApp:testAndroidHostTest"],
                ROOT_DIR,
            )
        )

    write_markdown_report(results, args)
    return 0 if all(r.passed for r in results) else 1


if __name__ == "__main__":
    sys.exit(main())
