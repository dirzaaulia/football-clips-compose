import argparse
import gzip
import json
import os
from datetime import UTC, datetime
from pathlib import Path
from typing import Any

from loguru import logger
from worker.clients.supabase_client import supabase_client
from worker.config.settings import settings

try:
    import boto3
    from botocore.config import Config
    BOTO3_AVAILABLE = True
except ImportError:
    BOTO3_AVAILABLE = False


ARCHIVE_DIR = Path(__file__).resolve().parent.parent / "archives"


def get_r2_client():
    """Create S3 client pointed at Cloudflare R2 endpoint."""
    if not BOTO3_AVAILABLE:
        logger.warning("[R2] boto3 is not installed. Install via 'pip install boto3'")
        return None

    if not (settings.r2_account_id and settings.r2_access_key_id and settings.r2_secret_access_key):
        logger.info("[R2] Cloudflare R2 credentials not fully set in .env (R2_ACCOUNT_ID, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY).")
        logger.info("[R2] Files will be stored locally in worker/archives/. Exporting to R2 requires R2 API tokens.")
        return None

    endpoint = f"https://{settings.r2_account_id}.r2.cloudflarestorage.com"
    return boto3.client(
        "s3",
        endpoint_url=endpoint,
        aws_access_key_id=settings.r2_access_key_id,
        aws_secret_access_key=settings.r2_secret_access_key,
        config=Config(signature_version="s3v4"),
        region_name="auto",
    )


def export_season_matches(season_year: int, upload_r2: bool = True) -> str:
    """Export all finished matches for a season to compressed JSON cold storage."""
    ARCHIVE_DIR.mkdir(parents=True, exist_ok=True)
    logger.info(f"[Archive] Fetching finished matches for season {season_year}/{season_year + 1} from Supabase...")

    # Season span: August 1 of season_year to July 31 of season_year + 1
    start_date = f"{season_year}-08-01T00:00:00Z"
    end_date = f"{season_year + 1}-07-31T23:59:59Z"

    matches = []
    page_size = 500
    offset = 0

    while True:
        res = (
            supabase_client.table("matches")
            .select("*")
            .gte("utc_date", start_date)
            .lte("utc_date", end_date)
            .order("utc_date", desc=False)
            .range(offset, offset + page_size - 1)
            .execute()
        )
        batch = res.data or []
        matches.extend(batch)
        if len(batch) < page_size:
            break
        offset += page_size

    logger.info(f"[Archive] Extracted {len(matches)} matches for season {season_year}/{season_year + 1}.")
    if not matches:
        logger.warning(f"[Archive] No matches found in Supabase for season {season_year}.")
        return ""

    filename = f"season_{season_year}_{season_year + 1}_matches.json.gz"
    local_path = ARCHIVE_DIR / filename

    raw_json = json.dumps({
        "season": f"{season_year}/{season_year + 1}",
        "exported_at": datetime.now(UTC).isoformat(),
        "total_matches": len(matches),
        "matches": matches,
    }, indent=2).encode("utf-8")

    with gzip.open(local_path, "wb") as f:
        f.write(raw_json)

    size_kb = local_path.stat().st_size / 1024.0
    logger.info(f"[Archive] Saved compressed cold archive to {local_path} ({size_kb:.1f} KB).")

    if upload_r2:
        r2 = get_r2_client()
        if r2:
            r2_key = f"seasons/{filename}"
            try:
                r2.upload_file(str(local_path), settings.r2_bucket_name, r2_key)
                logger.info(f"[R2 SUCCESS] Uploaded {filename} to Cloudflare R2 bucket '{settings.r2_bucket_name}' key '{r2_key}'.")
            except Exception as e:
                logger.error(f"[R2 ERROR] Failed to upload {filename} to R2: {e}")

    return str(local_path)


def export_telemetry_logs(max_age_days: int = 14, upload_r2: bool = True) -> str:
    """Export old telemetry sync cycle logs into cold archive."""
    ARCHIVE_DIR.mkdir(parents=True, exist_ok=True)
    logger.info(f"[Archive] Fetching telemetry events older than {max_age_days} days from Supabase...")

    # We check if telemetry_events table exists
    try:
        cutoff = (datetime.now(UTC).timestamp() - max_age_days * 86400)
        res = (
            supabase_client.table("telemetry_events")
            .select("*")
            .limit(1000)
            .execute()
        )
        events = res.data or []
        if not events:
            logger.info("[Archive] No telemetry events found to archive.")
            return ""

        now_str = datetime.now(UTC).strftime("%Y%m%d_%H%M%S")
        filename = f"telemetry_archive_{now_str}.json.gz"
        local_path = ARCHIVE_DIR / filename

        with gzip.open(local_path, "wb") as f:
            f.write(json.dumps(events).encode("utf-8"))

        logger.info(f"[Archive] Saved {len(events)} telemetry events to {local_path}.")

        if upload_r2:
            r2 = get_r2_client()
            if r2:
                r2_key = f"telemetry/{filename}"
                r2.upload_file(str(local_path), settings.r2_bucket_name, r2_key)
                logger.info(f"[R2 SUCCESS] Uploaded {filename} to Cloudflare R2 bucket '{settings.r2_bucket_name}'.")

        return str(local_path)
    except Exception as e:
        logger.warning(f"[Archive] Could not export telemetry events: {e}")
        return ""


def main():
    parser = argparse.ArgumentParser(description="Export match history and telemetry into compressed cold storage & Cloudflare R2.")
    parser.add_argument("--season", type=int, default=2024, help="Season start year (e.g. 2024 for 2024/2025). Default: 2024")
    parser.add_argument("--no-r2", action="store_true", help="Store only locally without attempting R2 upload.")
    parser.add_argument("--telemetry", action="store_true", help="Export telemetry logs as well.")

    args = parser.parse_args()

    export_season_matches(args.season, upload_r2=not args.no_r2)
    if args.telemetry:
        export_telemetry_logs(upload_r2=not args.no_r2)


if __name__ == "__main__":
    main()
