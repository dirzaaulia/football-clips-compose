-- Migration: 20261017_add_backup_highlight_video_id.sql
-- Description: Adds backup_highlight_video_id to matches table for graceful auto-failover in-app video playback.

ALTER TABLE matches ADD COLUMN IF NOT EXISTS backup_highlight_video_id VARCHAR(50);
