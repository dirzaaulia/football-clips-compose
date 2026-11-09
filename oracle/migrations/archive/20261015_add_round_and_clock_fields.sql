-- Migration: 20261015_add_round_and_clock_fields.sql
-- Description: Adds round_name, display_clock, and provider to matches table for rich matchweek & cup round filtering

ALTER TABLE matches ADD COLUMN IF NOT EXISTS round_name TEXT;
ALTER TABLE matches ADD COLUMN IF NOT EXISTS display_clock TEXT;
ALTER TABLE matches ADD COLUMN IF NOT EXISTS provider TEXT DEFAULT 'football_data';

-- Index on (competition_id, round_name) for ultra-fast matchweek and tournament phase filtering in the mobile app
CREATE INDEX IF NOT EXISTS idx_matches_comp_round ON matches(competition_id, round_name);

-- Index on (competition_id, matchday)
CREATE INDEX IF NOT EXISTS idx_matches_comp_matchday ON matches(competition_id, matchday);
