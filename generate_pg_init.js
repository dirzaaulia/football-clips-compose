const fs = require('fs');

const matches = JSON.parse(fs.readFileSync('web/matches_dump.json', 'utf8'));
const clubs = JSON.parse(fs.readFileSync('web/supported_clubs_dump.json', 'utf8'));
const standings = JSON.parse(fs.readFileSync('web/standings_dump.json', 'utf8'));

let sql = '';

// Schema
sql += `-- FootballClips PostgreSQL Schema & Initial Data
CREATE TABLE IF NOT EXISTS matches (
    id BIGINT PRIMARY KEY,
    competition_id TEXT NOT NULL,
    competition_name TEXT NOT NULL,
    utc_date TIMESTAMPTZ NOT NULL,
    status TEXT NOT NULL,
    matchday INT,
    home_team_id BIGINT,
    home_team_name TEXT NOT NULL,
    home_team_crest TEXT,
    away_team_id BIGINT,
    away_team_name TEXT NOT NULL,
    away_team_crest TEXT,
    home_score INT,
    away_score INT,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    highlight_video_id TEXT,
    last_youtube_check TIMESTAMPTZ,
    region_restriction TEXT,
    youtube_region_info JSONB,
    round_name TEXT,
    display_clock TEXT,
    provider TEXT DEFAULT 'football_data'
);

CREATE INDEX IF NOT EXISTS idx_matches_comp_round ON matches(competition_id, round_name);
CREATE INDEX IF NOT EXISTS idx_matches_comp_matchday ON matches(competition_id, matchday);
CREATE INDEX IF NOT EXISTS idx_matches_utc_status ON matches(utc_date, status);
CREATE INDEX IF NOT EXISTS idx_matches_status_utc ON matches(status, utc_date DESC);
CREATE INDEX IF NOT EXISTS idx_matches_unlinked_cooldown ON matches(last_youtube_check) WHERE highlight_video_id IS NULL AND status = 'FINISHED';
CREATE INDEX IF NOT EXISTS idx_matches_teams ON matches(home_team_id, away_team_id);
CREATE INDEX IF NOT EXISTS idx_matches_updated_at ON matches(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_matches_highlight ON matches(highlight_video_id) WHERE highlight_video_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS supported_clubs (
    id TEXT PRIMARY KEY,
    team_id BIGINT,
    name TEXT NOT NULL,
    short_name TEXT,
    competition_code TEXT NOT NULL,
    competition_name TEXT,
    crest_url TEXT,
    aliases TEXT[] DEFAULT '{}',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_supported_clubs_comp ON supported_clubs(competition_code);

CREATE TABLE IF NOT EXISTS standings (
    id TEXT PRIMARY KEY,
    competition_code TEXT NOT NULL,
    position INT NOT NULL,
    team_id BIGINT NOT NULL,
    team_name TEXT NOT NULL,
    team_short_name TEXT,
    crest_url TEXT,
    played_games INT NOT NULL,
    won INT NOT NULL,
    draw INT NOT NULL,
    lost INT NOT NULL,
    points INT NOT NULL,
    goals_for INT NOT NULL,
    goals_against INT NOT NULL,
    goal_difference INT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_standings_comp_pos ON standings(competition_code, position);

CREATE TABLE IF NOT EXISTS worker_cycles (
    cycle_id BIGSERIAL PRIMARY KEY,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    duration_ms INT,
    status TEXT NOT NULL,
    fixtures_synced INT DEFAULT 0,
    live_matches_detected INT DEFAULT 0,
    highlights_scraped INT DEFAULT 0,
    gemini_evaluations INT DEFAULT 0,
    edge_cache_purged BOOLEAN DEFAULT FALSE,
    error_message TEXT
);

CREATE TABLE IF NOT EXISTS worker_events (
    event_id BIGSERIAL PRIMARY KEY,
    cycle_id BIGINT REFERENCES worker_cycles(cycle_id) ON DELETE CASCADE,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    level TEXT NOT NULL,
    subsystem TEXT NOT NULL,
    action TEXT NOT NULL,
    message TEXT NOT NULL,
    metadata JSONB
);

CREATE INDEX IF NOT EXISTS idx_worker_events_cycle ON worker_events(cycle_id);
CREATE INDEX IF NOT EXISTS idx_worker_events_timestamp ON worker_events(timestamp DESC);

-- PostgREST role setup
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'web_anon') THEN
        CREATE ROLE web_anon NOLOGIN;
    END IF;
END
$$;

GRANT USAGE ON SCHEMA public TO web_anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO web_anon;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO web_anon;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO web_anon;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO web_anon;
`;

function esc(val) {
  if (val === null || val === undefined) return 'NULL';
  if (typeof val === 'number') return val;
  if (typeof val === 'boolean') return val ? 'TRUE' : 'FALSE';
  if (typeof val === 'object') return "'" + JSON.stringify(val).replace(/'/g, "''") + "'";
  return "'" + String(val).replace(/'/g, "''") + "'";
}

// Matches Inserts
console.log('Generating matches inserts...');
for (const m of matches) {
  sql += `INSERT INTO matches (id, competition_id, competition_name, utc_date, status, matchday, home_team_id, home_team_name, home_team_crest, away_team_id, away_team_name, away_team_crest, home_score, away_score, updated_at, highlight_video_id, last_youtube_check, region_restriction, youtube_region_info, round_name, display_clock, provider) VALUES (${esc(m.id)}, ${esc(m.competition_id)}, ${esc(m.competition_name)}, ${esc(m.utc_date)}, ${esc(m.status)}, ${esc(m.matchday)}, ${esc(m.home_team_id)}, ${esc(m.home_team_name)}, ${esc(m.home_team_crest)}, ${esc(m.away_team_id)}, ${esc(m.away_team_name)}, ${esc(m.away_team_crest)}, ${esc(m.home_score)}, ${esc(m.away_score)}, ${esc(m.updated_at)}, ${esc(m.highlight_video_id)}, ${esc(m.last_youtube_check)}, ${esc(m.region_restriction)}, ${esc(m.youtube_region_info)}, ${esc(m.round_name)}, ${esc(m.display_clock)}, ${esc(m.provider)}) ON CONFLICT (id) DO NOTHING;\n`;
}

// Supported Clubs Inserts
console.log('Generating supported clubs inserts...');
for (const c of clubs) {
  const aliasesArr = Array.isArray(c.aliases) && c.aliases.length > 0
    ? "ARRAY[" + c.aliases.map(a => esc(a)).join(',') + "]::text[]"
    : "'{}'::text[]";
  sql += `INSERT INTO supported_clubs (id, team_id, name, short_name, competition_code, competition_name, crest_url, aliases, is_active, created_at, updated_at) VALUES (${esc(c.id)}, ${esc(c.team_id)}, ${esc(c.name)}, ${esc(c.short_name)}, ${esc(c.competition_code)}, ${esc(c.competition_name)}, ${esc(c.crest_url)}, ${aliasesArr}, ${esc(c.is_active)}, ${esc(c.created_at)}, ${esc(c.updated_at)}) ON CONFLICT (id) DO NOTHING;\n`;
}

// Standings Inserts
console.log('Generating standings inserts...');
for (const s of standings) {
  sql += `INSERT INTO standings (id, competition_code, position, team_id, team_name, team_short_name, crest_url, played_games, won, draw, lost, points, goals_for, goals_against, goal_difference, updated_at) VALUES (${esc(s.id)}, ${esc(s.competition_code)}, ${esc(s.position)}, ${esc(s.team_id)}, ${esc(s.team_name)}, ${esc(s.team_short_name)}, ${esc(s.crest_url)}, ${esc(s.played_games)}, ${esc(s.won)}, ${esc(s.draw)}, ${esc(s.lost)}, ${esc(s.points)}, ${esc(s.goals_for)}, ${esc(s.goals_against)}, ${esc(s.goal_difference)}, ${esc(s.updated_at)}) ON CONFLICT (id) DO NOTHING;\n`;
}

fs.writeFileSync('init_footballclips.sql', sql);
console.log('Generated init_footballclips.sql successfully! Total size: ' + (sql.length / 1024).toFixed(1) + ' KB');
