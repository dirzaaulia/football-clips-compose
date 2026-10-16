#!/usr/bin/env bash
set -euo pipefail

BACKUP_DIR="/home/ubuntu/jarvis/storage/backups"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_FILE="${BACKUP_DIR}/footballclips_${TIMESTAMP}.sql.gz"
LATEST_LINK="${BACKUP_DIR}/footballclips_latest.sql.gz"

mkdir -p "${BACKUP_DIR}"

echo "[$(date -u +'%Y-%m-%d %H:%M:%SZ')] Starting PostgreSQL backup..."
docker exec jarvis-postgres pg_dump -U postgres -d footballclips | gzip -c > "${BACKUP_FILE}"

# Update latest symlink
ln -sf "${BACKUP_FILE}" "${LATEST_LINK}"

BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
echo "[$(date -u +'%Y-%m-%d %H:%M:%SZ')] Backup completed successfully: ${BACKUP_FILE} (${BACKUP_SIZE})"

# Prune local backups older than 14 days
find "${BACKUP_DIR}" -name "footballclips_*.sql.gz" -type f -mtime +14 -delete
echo "[$(date -u +'%Y-%m-%d %H:%M:%SZ')] Local retention policy enforced (14 days)."

# Cloudflare R2 / S3 Sync (Optional when configured)
if [ -f "/home/ubuntu/jarvis/.r2_env" ]; then
    source "/home/ubuntu/jarvis/.r2_env"
    if command -v aws >/dev/null 2>&1 && [ -n "${R2_BUCKET:-}" ]; then
        echo "[$(date -u +'%Y-%m-%d %H:%M:%SZ')] Uploading to Cloudflare R2 bucket: ${R2_BUCKET}..."
        aws s3 cp "${BACKUP_FILE}" "s3://${R2_BUCKET}/backups/footballclips_${TIMESTAMP}.sql.gz" \
            --endpoint-url "https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com"
        echo "[$(date -u +'%Y-%m-%d %H:%M:%SZ')] Uploaded to Cloudflare R2."
    elif command -v rclone >/dev/null 2>&1 && [ -n "${R2_RCLONE_REMOTE:-}" ]; then
        echo "[$(date -u +'%Y-%m-%d %H:%M:%SZ')] Syncing with rclone to ${R2_RCLONE_REMOTE}..."
        rclone copy "${BACKUP_FILE}" "${R2_RCLONE_REMOTE}"
        echo "[$(date -u +'%Y-%m-%d %H:%M:%SZ')] rclone sync complete."
    fi
fi
