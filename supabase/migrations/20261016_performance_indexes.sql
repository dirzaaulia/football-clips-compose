-- ==============================================================================
-- FOOTBALL CLIPS - DATABASE OPTIMIZATION & PERFORMANCE INDEXES
-- Purpose: Accelerate queries for 100,000+ DAU and sub-10ms PostgREST response times
-- ==============================================================================

-- 1. Index for fast status & date range queries (Live matches & Upcoming matches)
CREATE INDEX IF NOT EXISTS idx_matches_utc_status 
ON matches(utc_date, status);

-- 2. Index for reverse chronological finished matches & highlights
CREATE INDEX IF NOT EXISTS idx_matches_status_utc 
ON matches(status, utc_date DESC);

-- 3. Partial index for unlinked matches awaiting highlight discovery
CREATE INDEX IF NOT EXISTS idx_matches_unlinked_cooldown 
ON matches(last_youtube_check) 
WHERE highlight_video_id IS NULL AND status = 'FINISHED';

-- 4. Index for club-to-club head-to-head queries
CREATE INDEX IF NOT EXISTS idx_matches_teams 
ON matches(home_team_id, away_team_id);

-- 5. Index for incremental worker updates
CREATE INDEX IF NOT EXISTS idx_matches_updated_at 
ON matches(updated_at DESC);
