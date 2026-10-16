#!/bin/bash
set -euo pipefail

# ==============================================================================
# Backblaze B2 Automated Sync Script
# Target: /home/ubuntu/jarvis/storage/cloud/Backblaze
# ==============================================================================

ENV_FILE="/home/ubuntu/jarvis/.env"
DEST_DIR="/home/ubuntu/jarvis/storage/cloud/Backblaze"
LOG_FILE="/home/ubuntu/jarvis/storage/cloud/Backups/b2_sync.log"

if [ -f "$ENV_FILE" ]; then
    export $(grep -v '^#' "$ENV_FILE" | grep -E '^B2_' | xargs -d '\n')
fi

B2_ID="${B2_APPLICATION_KEY_ID:-}"
B2_KEY="${B2_APPLICATION_KEY:-}"
B2_BUCKET="${B2_BUCKET_NAME:-}"

if [ -z "$B2_ID" ] || [ -z "$B2_KEY" ] || [ -z "$B2_BUCKET" ]; then
    echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] [ERROR] Backblaze credentials missing."
    echo "Please set B2_APPLICATION_KEY_ID, B2_APPLICATION_KEY, and B2_BUCKET_NAME in $ENV_FILE"
    exit 1
fi

echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] [INFO] Starting Backblaze B2 Sync: b2:$B2_BUCKET -> $DEST_DIR"
mkdir -p "$DEST_DIR"
mkdir -p "$(dirname "$LOG_FILE")"

export RCLONE_CONFIG_B2_TYPE="b2"
export RCLONE_CONFIG_B2_ACCOUNT="$B2_ID"
export RCLONE_CONFIG_B2_KEY="$B2_KEY"

rclone sync "b2:${B2_BUCKET}" "$DEST_DIR" \
    --fast-list \
    --transfers 4 \
    --checkers 8 \
    --log-file="$LOG_FILE" \
    --log-level INFO \
    --stats 1m

echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] [SUCCESS] Backblaze B2 Sync completed successfully."
