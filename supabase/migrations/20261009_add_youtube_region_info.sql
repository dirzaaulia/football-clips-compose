-- Add youtube_region_info column to matches table for storing geoblocking/region restrictions
ALTER TABLE matches ADD COLUMN IF NOT EXISTS youtube_region_info JSONB;
