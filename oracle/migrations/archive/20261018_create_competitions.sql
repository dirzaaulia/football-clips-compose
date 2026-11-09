-- ==============================================================================
-- FOOTBALL CLIPS - DYNAMIC COMPETITIONS TABLE MIGRATION
-- Season 2026/2027 & Zero-Deploy Backward Compatibility
-- ==============================================================================

CREATE TABLE IF NOT EXISTS competitions (
    id               TEXT PRIMARY KEY,              -- Competition code (e.g., 'PL', 'CL', 'PD')
    name             TEXT NOT NULL,                 -- Full display name
    short_name       TEXT NOT NULL,                 -- Short / acronym name
    country_code     TEXT,                          -- ISO/Region code ('GB-ENG', 'EU', 'ES', etc.)
    emblem_url       TEXT,                          -- Crest / logo URL
    league_id        INT,                           -- Optional external API football ID
    is_active        BOOLEAN DEFAULT true,          -- Toggle visibility dynamically
    display_order    INT DEFAULT 99,                -- Ordering in quick filter bar
    created_at       TIMESTAMPTZ DEFAULT NOW(),
    updated_at       TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_competitions_active ON competitions (is_active, display_order);

-- RLS Policies
ALTER TABLE competitions ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE tablename = 'competitions' AND policyname = 'public_read_competitions'
    ) THEN
        CREATE POLICY "public_read_competitions" ON competitions FOR SELECT USING (true);
    END IF;
END $$;

-- Seed Active Competitions
INSERT INTO competitions (id, name, short_name, country_code, emblem_url, league_id, is_active, display_order)
VALUES
    ('CL',  'Champions League',    'UCL',         'EU',     'https://crests.football-data.org/CL.png',                     2486,   true, 1),
    ('EL',  'Europa League',       'UEL',         'EU',     'https://crests.football-data.org/EL.png',                     3337,   true, 2),
    ('ECL', 'Conference League',   'UECL',        'EU',     'https://a.espncdn.com/i/leaguelogos/soccer/500/20296.png',    3338,   true, 3),
    ('PL',  'Premier League',      'Premier Lge', 'GB-ENG', 'https://crests.football-data.org/PL.png',                     33973,  true, 4),
    ('PD',  'La Liga',             'LaLiga',      'ES',     'https://crests.football-data.org/PD.png',                     119924, true, 5),
    ('SA',  'Serie A',             'Serie A',     'IT',     'https://crests.football-data.org/SA.png',                     115669, true, 6),
    ('BL1', 'Bundesliga',          'Bundesliga',  'DE',     'https://crests.football-data.org/BL1.png',                    67162,  true, 7),
    ('FL1', 'Ligue 1',             'Ligue 1',     'FR',     'https://crests.football-data.org/FL1.png',                    52695,  true, 8),
    ('SPL', 'Saudi Pro League',    'Saudi Pro',   'SA',     'https://a.espncdn.com/i/leaguelogos/soccer/500/2488.png',     NULL,   true, 9),
    ('MLS', 'Major League Soccer', 'MLS',         'US',     'https://a.espncdn.com/i/leaguelogos/soccer/500/19.png',       NULL,   true, 10),
    ('DED', 'Eredivisie',          'Eredivisie',  'NL',     'https://crests.football-data.org/ED.png',                     NULL,   true, 11),
    ('PPL', 'Liga Portugal',       'Liga Portugal','PT',    'https://crests.football-data.org/PPL.png',                    NULL,   true, 12),
    ('IDN', 'BRI Super League (Liga Indonesia)', 'Liga Indonesia', 'ID', 'https://fc.dirzaaulia.com/logos/competitions/idn-2026.png', NULL, true, 13),
    ('FAC', 'FA Cup',              'FA Cup',      'GB-ENG', 'https://a.espncdn.com/i/leaguelogos/soccer/500/40.png',       NULL,   true, 14),
    ('EFL', 'Carabao Cup',         'EFL Cup',     'GB-ENG', 'https://a.espncdn.com/i/leaguelogos/soccer/500/41.png',       NULL,   true, 15),
    ('CDR', 'Copa del Rey',        'Copa del Rey','ES',     'https://a.espncdn.com/i/leaguelogos/soccer/500/80.png',       NULL,   true, 16),
    ('DFB', 'DFB-Pokal',           'DFB-Pokal',   'DE',     'https://a.espncdn.com/i/leaguelogos/soccer/500/2061.png',     NULL,   true, 17),
    ('CIT', 'Coppa Italia',        'Coppa Italia','IT',     'https://crests.football-data.org/CIT.png',                    NULL,   true, 18),
    ('LIB', 'CONMEBOL Libertadores','Libertadores','SA',    'https://a.espncdn.com/i/leaguelogos/soccer/500/58.png',       NULL,   true, 19),
    ('SUD', 'CONMEBOL Sudamericana','Sudamericana','SA',    'https://a.espncdn.com/i/leaguelogos/soccer/500/1208.png',     NULL,   true, 20),
    ('REC', 'CONMEBOL Recopa',     'Recopa',      'SA',     'https://a.espncdn.com/i/leaguelogos/soccer/500/2335.png',     NULL,   true, 21),
    ('BRA', 'Brasileirao Serie A', 'Brasileirao', 'BR',     'https://a.espncdn.com/i/leaguelogos/soccer/500/85.png',       NULL,   true, 22),
    ('ARG', 'Argentine Liga Profesional', 'Liga Argentina', 'AR', 'https://a.espncdn.com/i/leaguelogos/soccer/500/1.png', NULL,   true, 23)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    short_name = EXCLUDED.short_name,
    country_code = EXCLUDED.country_code,
    emblem_url = EXCLUDED.emblem_url,
    league_id = EXCLUDED.league_id,
    is_active = EXCLUDED.is_active,
    display_order = EXCLUDED.display_order,
    updated_at = NOW();
