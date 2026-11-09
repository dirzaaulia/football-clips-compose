-- Migration: 20261016_add_season_stats_editorial_fields.sql
-- Description: Adds season and form to standings; adds season, formations, statistics, and editorial fields to matches.

-- 1. Standings Table Enhancements
ALTER TABLE standings ADD COLUMN IF NOT EXISTS season INT DEFAULT 2026;
ALTER TABLE standings ADD COLUMN IF NOT EXISTS form VARCHAR(30);

-- Backfill default season for existing standings rows
UPDATE standings SET season = 2026 WHERE season IS NULL;

-- Fast index for standings filtered by competition and season
CREATE INDEX IF NOT EXISTS idx_standings_comp_season ON standings(competition_code, season);

-- 2. Matches Table Enhancements
ALTER TABLE matches ADD COLUMN IF NOT EXISTS season INT DEFAULT 2026;
ALTER TABLE matches ADD COLUMN IF NOT EXISTS formations JSONB;
ALTER TABLE matches ADD COLUMN IF NOT EXISTS statistics JSONB;
ALTER TABLE matches ADD COLUMN IF NOT EXISTS editorial_headline TEXT;
ALTER TABLE matches ADD COLUMN IF NOT EXISTS editorial_story TEXT;
ALTER TABLE matches ADD COLUMN IF NOT EXISTS editorial_source TEXT;

-- Backfill default season for existing matches rows
UPDATE matches SET season = 2026 WHERE season IS NULL;

-- Fast index for matches filtered by competition and season
CREATE INDEX IF NOT EXISTS idx_matches_comp_season ON matches(competition_id, season);
