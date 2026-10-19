-- Add backup_youtube_region_info JSONB column to matches table for storing geoblocking/region restrictions of secondary stream
ALTER TABLE matches ADD COLUMN IF NOT EXISTS backup_youtube_region_info JSONB;
