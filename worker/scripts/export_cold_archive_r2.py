import argparse
import os
from datetime import UTC, datetime
from pathlib import Path

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


def export_season_matches(season_year: int, upload_r2: bool = False) -> str:
    """Stream the actual provider season, including calendar-year competitions."""
    from worker.utils.archive_io import write_archive
    def records():
        last = -1
        while True:
            result = (supabase_client.table("matches").select("*").eq("season", season_year)
                .gt("id", last).order("id").limit(500).execute())
            page = result.data or []
            if not page:
                return
            yield from page
            last = page[-1]["id"]
    path = ARCHIVE_DIR / f"season_{season_year}.json.gz"
    manifest = write_archive(path, records(), season=season_year)
    if upload_r2:
        if os.getenv("FC_REMOTE_ARCHIVE_APPROVED") != "true":
            raise RuntimeError("Verify remote storage allowance and explicitly approve remote archival first")
        client = get_r2_client()
        if not client:
            raise RuntimeError("Remote archive credentials are unavailable")
        key = "seasons/" + path.name
        client.upload_file(str(path), settings.r2_bucket_name, key,
                           ExtraArgs={"Metadata": {"sha256": manifest["sha256"]}})
        remote = client.head_object(Bucket=settings.r2_bucket_name, Key=key)
        if remote["ContentLength"] != manifest["bytes"]:
            raise RuntimeError("Remote archive size verification failed")
    logger.info(f"[Archive] Verified {manifest['records']} records for provider season {season_year}.")
    return str(path)


def export_telemetry_logs(max_age_days: int = 14, upload_r2: bool = False) -> str:
    """Export bounded pages that actually satisfy the requested age cutoff."""
    from datetime import timedelta

    from worker.utils.archive_io import write_archive
    cutoff = (datetime.now(UTC) - timedelta(days=max_age_days)).isoformat()
    def records():
        last = -1
        while True:
            result = (supabase_client.table("worker_events").select("*").lt("created_at", cutoff)
                .gt("id", last).order("id").limit(500).execute())
            page = result.data or []
            if not page:
                return
            yield from page
            last = page[-1]["id"]
    path = ARCHIVE_DIR / ("telemetry_" + datetime.now(UTC).strftime("%Y%m%d_%H%M%S") + ".json.gz")
    write_archive(path, records(), season=None)
    if upload_r2:
        raise RuntimeError("Remote telemetry export is disabled until retention and account allowance are verified")
    return str(path)


def main():
    parser = argparse.ArgumentParser(description="Export match history and telemetry into compressed cold storage & Cloudflare R2.")
    parser.add_argument("--season", type=int, default=2025, help="Season start year (e.g. 2024 for 2024/2025). Default: 2024")
    parser.add_argument("--upload-r2", action="store_true", help="Upload only after remote allowance approval.")
    parser.add_argument("--no-r2", action="store_true", help="Store only locally without attempting R2 upload.")
    parser.add_argument("--telemetry", action="store_true", help="Export telemetry logs as well.")

    args = parser.parse_args()

    export_season_matches(args.season, upload_r2=args.upload_r2 and not args.no_r2)
    if args.telemetry:
        export_telemetry_logs(upload_r2=args.upload_r2 and not args.no_r2)


if __name__ == "__main__":
    main()
