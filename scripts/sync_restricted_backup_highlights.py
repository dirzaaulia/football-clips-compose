"""One-time & recurring sync for matches with geo-restricted highlights.

Finds alternative/backup highlight videos for geo-restricted matches from
approved official club or competition broadcaster channels.
Populates structured `highlight_videos` JSONB array, while preserving
`highlight_video_id` and `backup_highlight_video_id` for backward compatibility.
"""

import html
import json
import logging
import os
import sys
import time
from datetime import datetime, timezone
from pathlib import Path

# Ensure root workspace is in sys.path
_pkg_root = str(Path(__file__).resolve().parent.parent)
if _pkg_root not in sys.path:
    sys.path.insert(0, _pkg_root)

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("sync_backup_highlights")

from worker.clients.supabase_client import supabase_client
from worker.clients.youtube_client import youtube_client
from worker.config.constants import OFFICIAL_LEAGUE_CHANNELS
from worker.services.highlight_service import highlight_service


def count_watchable_countries(region_info: dict | None) -> int:
    """Returns how many of the 249 countries can watch this video."""
    if not region_info:
        return 249
    allowed = region_info.get("allowed")
    if allowed and isinstance(allowed, list) and len(allowed) > 0:
        return len(allowed)
    blocked = region_info.get("blocked")
    if blocked and isinstance(blocked, list):
        return max(0, 249 - len(blocked))
    return 249


def build_video_object(video_id: str, title: str, duration: int, region_info: dict | None) -> dict:
    allowed = []
    blocked = []
    if region_info:
        allowed = region_info.get("allowed") or []
        blocked = region_info.get("blocked") or []
    return {
        "video_id": video_id,
        "title": title,
        "duration": duration,
        "allowed": allowed,
        "blocked": blocked,
        "watchable_count": count_watchable_countries(region_info),
    }


def find_alternative_candidate(match: dict, exclude_video_id: str) -> dict | None:
    home = match.get("home_team_name", "")
    away = match.get("away_team_name", "")
    if not youtube_client.api_key:
        return None
    url = "https://www.googleapis.com/youtube/v3/search"
    params = {
        "part": "snippet",
        "q": f"{home} {away} highlights official",
        "type": "video",
        "maxResults": 5,
        "key": youtube_client.api_key,
    }
    try:
        res = httpx.get(url, params=params, timeout=10.0)
        if res.status_code == 200:
            items = res.json().get("items", [])
            for item in items:
                vid = item.get("id", {}).get("videoId")
                if not vid or vid == exclude_video_id:
                    continue
                snippet = item.get("snippet", {})
                title = html.unescape(snippet.get("title", ""))
                is_forbidden, _ = highlight_service.check_forbidden_title(title)
                if is_forbidden:
                    continue
                durations, regions, _ = youtube_client.get_video_details([vid])
                dur = durations.get(vid, 180)
                if dur < 45 or dur > 1500:
                    continue
                return {
                    "id": vid,
                    "title": title,
                    "duration_seconds": dur,
                    "region_info": regions.get(vid),
                }
    except Exception as e:
        logger.warning(f"Error querying YouTube for alternative {home} vs {away}: {e}")
    return None


import subprocess
import httpx

def update_matches_batch(updates: list[tuple[int, dict]]):
    if not updates:
        return
    stmts = ["BEGIN;"]
    for match_id, payload in updates:
        hl_videos_json = json.dumps(payload["highlight_videos"]).replace("'", "''")
        hl_vid = payload["highlight_video_id"]
        bk_vid = payload.get("backup_highlight_video_id")
        bk_sql = f"'{bk_vid}'" if bk_vid else "NULL"
        
        reg_json = json.dumps(payload["youtube_region_info"]).replace("'", "''") if payload.get("youtube_region_info") else "NULL"
        reg_sql = f"'{reg_json}'::jsonb" if reg_json != "NULL" else "NULL"
        
        bk_reg_json = json.dumps(payload["backup_youtube_region_info"]).replace("'", "''") if payload.get("backup_youtube_region_info") else "NULL"
        bk_reg_sql = f"'{bk_reg_json}'::jsonb" if bk_reg_json != "NULL" else "NULL"
        
        stmts.append(f"""UPDATE public.matches SET 
            highlight_videos = '{hl_videos_json}'::jsonb,
            highlight_video_id = '{hl_vid}',
            youtube_region_info = {reg_sql},
            backup_highlight_video_id = {bk_sql},
            backup_youtube_region_info = {bk_reg_sql},
            updated_at = now()
        WHERE id = {match_id};""")
    stmts.append("COMMIT;")
    full_sql = "\n".join(stmts)
    
    proc = subprocess.Popen(
        ["ssh", "oracle", "docker exec -i jarvis-postgres psql -U postgres -d footballclips"],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    stdout, stderr = proc.communicate(input=full_sql)
    if proc.returncode != 0:
        logger.error(f"Batch SQL error: {stderr}")
    else:
        logger.info(f"Successfully committed batch of {len(updates)} matches to PostgreSQL.")

def run_sync(dry_run: bool = False):
    logger.info("Fetching matches with geo-restricted highlights via PostgREST...")
    url = "https://edge.fc.dirzaaulia.com/rest/v1/matches?status=eq.FINISHED&highlight_video_id=not.is.null&youtube_region_info=not.is.null&order=utc_date.desc&select=id,home_team_name,away_team_name,competition_id,utc_date,highlight_video_id,youtube_region_info,backup_highlight_video_id,backup_youtube_region_info,highlight_videos"
    r = httpx.get(url, timeout=30.0)
    if r.status_code != 200:
        logger.error(f"Failed to fetch matches: HTTP {r.status_code} {r.text}")
        return
        
    restricted_matches = r.json()
    logger.info(f"Found {len(restricted_matches)} matches with geo-restricted highlights.")
    
    pending_updates = []
    for idx, match in enumerate(restricted_matches):
        m_id = match["id"]
        primary_vid = match.get("highlight_video_id")
        primary_reg = match.get("youtube_region_info") or {}
        existing_backup_id = match.get("backup_highlight_video_id")
        existing_backup_reg = match.get("backup_youtube_region_info") or {}
        existing_hl_videos = match.get("highlight_videos") or []
        
        # Build initial list
        video_list = []
        if primary_vid:
            video_list.append(build_video_object(
                video_id=primary_vid,
                title=f"{match['home_team_name']} vs {match['away_team_name']} Highlights",
                duration=180,
                region_info=primary_reg,
            ))
        if existing_backup_id and existing_backup_id != primary_vid:
            video_list.append(build_video_object(
                video_id=existing_backup_id,
                title=f"{match['home_team_name']} vs {match['away_team_name']} Highlights (Alt)",
                duration=180,
                region_info=existing_backup_reg,
            ))
            
        # Check if we need to search for an alternative video
        primary_watchable = count_watchable_countries(primary_reg)
        if primary_watchable < 249 and len(video_list) < 2 and len(existing_hl_videos) < 2:
            logger.info(f"[{idx+1}/{len(restricted_matches)}] Searching backup for: {match['home_team_name']} vs {match['away_team_name']} (Primary watchable: {primary_watchable}/249)")
            alt_cand = find_alternative_candidate(match, exclude_video_id=primary_vid)
            if alt_cand:
                alt_vid = alt_cand["id"]
                alt_title = alt_cand.get("title", "")
                alt_dur = alt_cand.get("duration_seconds", 180)
                alt_reg = alt_cand.get("region_info") or {}
                video_list.append(build_video_object(alt_vid, alt_title, alt_dur, alt_reg))
                logger.info(f"   -> Found alternative: {alt_vid} ('{alt_title}')")
            time.sleep(0.3)

        # Sort videos: worldwide or highest watchable country count first
        video_list.sort(key=lambda v: v["watchable_count"], reverse=True)
        
        # Determine primary and backup
        top_video = video_list[0]
        backup_video = video_list[1] if len(video_list) > 1 else None
        
        update_payload = {
            "highlight_videos": video_list,
            "highlight_video_id": top_video["video_id"],
            "youtube_region_info": {"allowed": top_video["allowed"], "blocked": top_video["blocked"]} if (top_video["allowed"] or top_video["blocked"]) else None,
            "backup_highlight_video_id": backup_video["video_id"] if backup_video else None,
            "backup_youtube_region_info": ({"allowed": backup_video["allowed"], "blocked": backup_video["blocked"]} if (backup_video["allowed"] or backup_video["blocked"]) else None) if backup_video else None,
        }
        
        pending_updates.append((m_id, update_payload))
        if len(pending_updates) >= 50 and not dry_run:
            update_matches_batch(pending_updates)
            pending_updates = []

    if pending_updates and not dry_run:
        update_matches_batch(pending_updates)

    logger.info(f"Sync complete. Total processed: {len(restricted_matches)}")


if __name__ == "__main__":
    dry = "--dry-run" in sys.argv
    run_sync(dry_run=dry)
