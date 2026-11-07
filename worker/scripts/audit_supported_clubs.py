#!/usr/bin/env python3
"""Audit script for all supported clubs across competitions and seasons.

Validates YouTube handles, channel IDs, aliases, and match highlight coverage.
Outputs structured findings to generate an industry-grade audit report.
"""

import json
from collections import defaultdict
from pathlib import Path
from typing import Any

from loguru import logger
from worker.clients.database_client import db_client


def run_comprehensive_audit() -> dict[str, Any]:
    logger.info("[AUDIT] Starting comprehensive audit of supported clubs and match coverage...")

    # 1. Fetch all supported clubs
    res_clubs = db_client.table("supported_clubs").select(
        "id,team_id,name,short_name,competition_code,youtube_handle,youtube_channel_id,aliases,is_active"
    ).execute()
    clubs = res_clubs.data or []

    # 2. Fetch all competitions
    res_comps = db_client.table("competitions").select(
        "id,name,short_name,youtube_handle,youtube_channel_id,backup_channel_handles,backup_channel_ids"
    ).execute()
    comps = res_comps.data or []
    comp_map = {c["id"]: c for c in comps}

    # 3. Analyze Club Channel Health
    missing_handle = []
    missing_channel_id = []
    malformed_handle = []
    malformed_channel_id = []
    empty_aliases = []
    comp_clubs = defaultdict(list)

    for c in clubs:
        cid = c.get("id")
        name = c.get("name") or "Unknown"
        handle = (c.get("youtube_handle") or "").strip()
        ch_id = (c.get("youtube_channel_id") or "").strip()
        aliases = c.get("aliases") or []
        code = c.get("competition_code") or "OTHER"

        comp_clubs[code].append(c)

        if not handle:
            missing_handle.append({"id": cid, "name": name, "competition": code})
        elif not handle.startswith("@"):
            malformed_handle.append({"id": cid, "name": name, "handle": handle, "competition": code})

        if not ch_id:
            missing_channel_id.append({"id": cid, "name": name, "handle": handle, "competition": code})
        elif not (ch_id.startswith("UC") and len(ch_id) == 24):
            malformed_channel_id.append({"id": cid, "name": name, "channel_id": ch_id, "competition": code})

        if not aliases:
            empty_aliases.append({"id": cid, "name": name, "competition": code})

    # 4. Fetch match highlight coverage by competition & season
    res_matches = db_client.table("matches").select(
        "id,competition_id,season,status,highlight_video_id,home_team_name,away_team_name"
    ).eq("status", "FINISHED").execute()
    matches = res_matches.data or []

    season_coverage = defaultdict(lambda: {"total": 0, "linked": 0})
    comp_coverage = defaultdict(lambda: {"total": 0, "linked": 0})
    unlinked_matches_by_comp = defaultdict(list)

    for m in matches:
        s = m.get("season") or 2026
        c = m.get("competition_id") or "OTHER"
        is_linked = bool(m.get("highlight_video_id"))

        season_coverage[s]["total"] += 1
        if is_linked:
            season_coverage[s]["linked"] += 1

        comp_coverage[c]["total"] += 1
        if is_linked:
            comp_coverage[c]["linked"] += 1
        else:
            unlinked_matches_by_comp[c].append({
                "id": m["id"],
                "home": m["home_team_name"],
                "away": m["away_team_name"],
                "season": s,
            })

    # 5. Summarize per competition health
    competition_summary = []
    for code, club_list in sorted(comp_clubs.items()):
        total = len(club_list)
        with_handle = sum(1 for x in club_list if (x.get("youtube_handle") or "").startswith("@"))
        with_ch_id = sum(1 for x in club_list if (x.get("youtube_channel_id") or "").startswith("UC"))
        c_matches = comp_coverage[code]
        match_tot = c_matches["total"]
        match_lnk = c_matches["linked"]
        pct = round((match_lnk / match_tot * 100), 1) if match_tot > 0 else 100.0

        competition_summary.append({
            "code": code,
            "competition_name": comp_map.get(code, {}).get("name") or code,
            "total_clubs": total,
            "clubs_with_valid_handle": with_handle,
            "clubs_with_valid_channel_id": with_ch_id,
            "finished_matches": match_tot,
            "linked_highlights": match_lnk,
            "highlight_coverage_pct": pct,
        })

    report = {
        "total_supported_clubs": len(clubs),
        "total_competitions": len(comp_clubs),
        "missing_handle_count": len(missing_handle),
        "missing_channel_id_count": len(missing_channel_id),
        "malformed_handle_count": len(malformed_handle),
        "malformed_channel_id_count": len(malformed_channel_id),
        "empty_aliases_count": len(empty_aliases),
        "missing_handles": missing_handle,
        "missing_channel_ids": missing_channel_id,
        "malformed_handles": malformed_handle,
        "malformed_channel_ids": malformed_channel_id,
        "empty_aliases": empty_aliases,
        "competitions": competition_summary,
        "season_coverage": dict(season_coverage),
        "unlinked_matches_count": sum(len(v) for v in unlinked_matches_by_comp.values()),
        "unlinked_matches_by_comp": dict(unlinked_matches_by_comp),
    }

    return report


if __name__ == "__main__":
    rep = run_comprehensive_audit()
    print("\n================ AUDIT SUMMARY ================")
    print(f"Total Supported Clubs: {rep['total_supported_clubs']}")
    print(f"Total Competitions: {rep['total_competitions']}")
    print(f"Clubs with Missing Handle: {rep['missing_handle_count']}")
    print(f"Clubs with Missing Channel ID: {rep['missing_channel_id_count']}")
    print(f"Clubs with Malformed Channel ID: {rep['malformed_channel_id_count']}")
    print(f"Clubs with Empty Aliases: {rep['empty_aliases_count']}")
    print("\n--- Season Coverage ---")
    for s, data in sorted(rep["season_coverage"].items()):
        tot, lnk = data["total"], data["linked"]
        pct = round(lnk / tot * 100, 1) if tot > 0 else 0
        print(f"Season {s}: {lnk} / {tot} highlights linked ({pct}%)")

    print("\n--- Competition Summary ---")
    for c in rep["competitions"]:
        print(
            f"[{c['code']}] {c['competition_name']}: {c['total_clubs']} clubs | "
            f"Handles: {c['clubs_with_valid_handle']}/{c['total_clubs']} | "
            f"Channel IDs: {c['clubs_with_valid_channel_id']}/{c['total_clubs']} | "
            f"Highlights: {c['linked_highlights']}/{c['finished_matches']} ({c['highlight_coverage_pct']}%)"
        )

    # Save raw audit findings to JSON for report generation
    import tempfile
    out_path = Path(tempfile.gettempdir()) / "audit_findings.json"
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(rep, f, indent=2)
    print(f"\nSaved detailed audit findings to {out_path}")
