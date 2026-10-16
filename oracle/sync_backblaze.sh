#!/bin/bash
set -euo pipefail

# ==============================================================================
# Multi-Bucket Backblaze B2 Automated Sync Script
# Target: /home/ubuntu/jarvis/storage/cloud/Backblaze
# ==============================================================================

ENV_FILE="/home/ubuntu/jarvis/.env"
BASE_DEST="/home/ubuntu/jarvis/storage/cloud/Backblaze"
LOG_DIR="/home/ubuntu/jarvis/storage/cloud/Backups"

if [ -f "$ENV_FILE" ]; then
    export $(grep -v '^#' "$ENV_FILE" | grep -E '^B2_' | xargs -d '\n')
fi

mkdir -p "$BASE_DEST/personal-web"
mkdir -p "$BASE_DEST/pc-backup"
mkdir -p "$LOG_DIR"

# ------------------------------------------------------------------------------
# 1. Sync personal-web Bucket
# ------------------------------------------------------------------------------
PW_ID="${B2_PW_KEY_ID:-${B2_APPLICATION_KEY_ID:-}}"
PW_KEY="${B2_PW_APPLICATION_KEY:-${B2_APPLICATION_KEY:-}}"
PW_BUCKET="${B2_PW_BUCKET:-${B2_BUCKET_NAME:-dirza-personal-web}}"

if [ -n "$PW_ID" ] && [ -n "$PW_KEY" ] && [ -n "$PW_BUCKET" ]; then
    echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] [INFO] Syncing b2:$PW_BUCKET -> $BASE_DEST/personal-web"
    export RCLONE_CONFIG_B2PW_TYPE="b2"
    export RCLONE_CONFIG_B2PW_ACCOUNT="$PW_ID"
    export RCLONE_CONFIG_B2PW_KEY="$PW_KEY"
    rclone sync "b2pw:${PW_BUCKET}" "$BASE_DEST/personal-web" \
        --fast-list --transfers 4 --checkers 8 \
        --log-file="$LOG_DIR/b2_sync_personal_web.log" --log-level INFO --stats 1m || true
    echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] [SUCCESS] personal-web sync finished."
fi

# ------------------------------------------------------------------------------
# 2. Sync dirza-pc-backup Bucket
# ------------------------------------------------------------------------------
PC_ID="${B2_PC_KEY_ID:-0056c66d6c4e56b0000000001}"
PC_KEY="${B2_PC_APPLICATION_KEY:-}"
PC_BUCKET="${B2_PC_BUCKET:-dirza-pc-backup}"

if [ -n "$PC_ID" ] && [ -n "$PC_KEY" ] && [ -n "$PC_BUCKET" ]; then
    echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] [INFO] Syncing b2:$PC_BUCKET -> $BASE_DEST/pc-backup"
    export RCLONE_CONFIG_B2PC_TYPE="b2"
    export RCLONE_CONFIG_B2PC_ACCOUNT="$PC_ID"
    export RCLONE_CONFIG_B2PC_KEY="$PC_KEY"
    rclone sync "b2pc:${PC_BUCKET}" "$BASE_DEST/pc-backup" \
        --fast-list --transfers 4 --checkers 8 \
        --log-file="$LOG_DIR/b2_sync_pc_backup.log" --log-level INFO --stats 1m
    echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] [SUCCESS] dirza-pc-backup sync finished."
else
    echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] [NOTICE] dirza-pc-backup awaiting B2_PC_APPLICATION_KEY in $ENV_FILE"
fi
