-- Migration: Add highlight_videos JSONB array for multi-tier video highlights
-- Tier 1: Club official channels (100% worldwide availability)
-- Tier 2: Major regional broadcasters (e.g. beIN Sports Asia, CBS Golazo)
-- Tier 3: Domestic/local broadcaster channels

ALTER TABLE matches
ADD COLUMN IF NOT EXISTS highlight_videos JSONB DEFAULT '[]'::jsonb;

-- Index for querying matches with multiple highlight videos
CREATE INDEX IF NOT EXISTS idx_matches_highlight_videos ON matches USING gin (highlight_videos);

COMMENT ON COLUMN matches.highlight_videos IS 'Ordered array of ranked highlight video candidates with tiering, region restrictions, and metadata';
