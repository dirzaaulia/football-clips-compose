-- ==============================================================================
-- FOOTBALL CLIPS - STANDINGS TABLE MIGRATION WITH ROW LEVEL SECURITY (RLS)
-- Season 2026/2027
-- ==============================================================================

-- 1. Create Standings Table
CREATE TABLE IF NOT EXISTS standings (
    id               TEXT PRIMARY KEY,              -- Format: "{COMPETITION_CODE}_{TEAM_ID}" e.g. "PL_65"
    competition_code TEXT NOT NULL,                -- "PL", "PD", "SA", "BL1", "FL1", "CL"
    competition_name TEXT,                         -- e.g. "Premier League"
    season           INT NOT NULL,                 -- e.g. 2026
    position         INT NOT NULL,                 -- 1, 2, 3 ...
    team_id          INT NOT NULL,                 -- Football-Data team ID
    team_name        TEXT NOT NULL,                -- e.g. "Manchester City FC"
    team_short_name  TEXT,                         -- e.g. "Man City"
    crest_url        TEXT,                         -- URL to team crest PNG/SVG
    played_games     INT NOT NULL DEFAULT 0,
    won              INT NOT NULL DEFAULT 0,
    draw             INT NOT NULL DEFAULT 0,
    lost             INT NOT NULL DEFAULT 0,
    points           INT NOT NULL DEFAULT 0,
    goals_for        INT NOT NULL DEFAULT 0,
    goals_against    INT NOT NULL DEFAULT 0,
    goal_difference  INT NOT NULL DEFAULT 0,
    form             TEXT,                         -- e.g. "W,W,D,L,W"
    updated_at       TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Create Optimized Query Indexes
CREATE INDEX IF NOT EXISTS idx_standings_comp_pos ON standings (competition_code, position ASC);
CREATE INDEX IF NOT EXISTS idx_standings_team_id ON standings (team_id);

-- 3. ENABLE ROW LEVEL SECURITY (RLS)
ALTER TABLE standings ENABLE ROW LEVEL SECURITY;

-- 4. Create Public Read Policy (Allow anyone with anon/authenticated key to view standings)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE tablename = 'standings' AND policyname = 'public_read_standings'
    ) THEN
        CREATE POLICY "public_read_standings" ON standings FOR SELECT USING (true);
    END IF;
END $$;

-- Note: INSERT, UPDATE, and DELETE are strictly restricted to service_role (our Python Worker)
