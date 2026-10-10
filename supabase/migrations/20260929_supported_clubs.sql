-- ==============================================================================
-- FOOTBALL CLIPS - SUPPORTED CLUBS (BIG 5 LEAGUES) DYNAMIC TABLE MIGRATION
-- Season 2026/2027
-- ==============================================================================

CREATE TABLE IF NOT EXISTS supported_clubs (
    id               TEXT PRIMARY KEY,
    team_id          INT NOT NULL,
    name             TEXT NOT NULL,
    short_name       TEXT,
    competition_code TEXT NOT NULL,
    competition_name TEXT NOT NULL,
    crest_url        TEXT,
    aliases          TEXT[] DEFAULT '{}',
    is_active        BOOLEAN DEFAULT true,
    created_at       TIMESTAMPTZ DEFAULT NOW(),
    updated_at       TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_supported_clubs_comp ON supported_clubs (competition_code, is_active);
CREATE INDEX IF NOT EXISTS idx_supported_clubs_team_id ON supported_clubs (team_id);

-- RLS Policies
ALTER TABLE supported_clubs ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE tablename = 'supported_clubs' AND policyname = 'public_read_supported_clubs'
    ) THEN
        CREATE POLICY "public_read_supported_clubs" ON supported_clubs FOR SELECT USING (true);
    END IF;
END $$;

-- Seed all 96 clubs for Season 2026/2027
INSERT INTO supported_clubs (id, team_id, name, short_name, competition_code, competition_name, crest_url, is_active)
VALUES
    ('leeds_united_fc', 341, 'Leeds United FC', 'Leeds United', 'PL', 'Premier League', 'https://crests.football-data.org/341.png', true),
    ('newcastle_united_fc', 67, 'Newcastle United FC', 'Newcastle Unite', 'PL', 'Premier League', 'https://crests.football-data.org/67.png', true),
    ('nottingham_forest_fc', 351, 'Nottingham Forest FC', 'Nottingham Fore', 'PL', 'Premier League', 'https://crests.football-data.org/351.png', true),
    ('coventry_city_fc', 1076, 'Coventry City FC', 'Coventry City', 'PL', 'Premier League', 'https://crests.football-data.org/1076.png', true),
    ('tottenham_hotspur_fc', 73, 'Tottenham Hotspur FC', 'Tottenham Hotsp', 'PL', 'Premier League', 'https://crests.football-data.org/73.png', true),
    ('aston_villa_fc', 58, 'Aston Villa FC', 'Aston Villa', 'PL', 'Premier League', 'https://crests.football-data.org/58.png', true),
    ('brentford_fc', 402, 'Brentford FC', 'Brentford', 'PL', 'Premier League', 'https://crests.football-data.org/402.png', true),
    ('chelsea_fc', 61, 'Chelsea FC', 'Chelsea', 'PL', 'Premier League', 'https://crests.football-data.org/61.png', true),
    ('afc_bournemouth', 1044, 'AFC Bournemouth', 'AFC Bournemouth', 'PL', 'Premier League', 'https://crests.football-data.org/bournemouth.png', true),
    ('liverpool_fc', 64, 'Liverpool FC', 'Liverpool', 'PL', 'Premier League', 'https://crests.football-data.org/64.png', true),
    ('fulham_fc', 63, 'Fulham FC', 'Fulham', 'PL', 'Premier League', 'https://crests.football-data.org/63.png', true),
    ('manchester_united_fc', 66, 'Manchester United FC', 'Manchester Unit', 'PL', 'Premier League', 'https://crests.football-data.org/66.png', true),
    ('crystal_palace_fc', 354, 'Crystal Palace FC', 'Crystal Palace', 'PL', 'Premier League', 'https://crests.football-data.org/354.png', true),
    ('manchester_city_fc', 65, 'Manchester City FC', 'Manchester City', 'PL', 'Premier League', 'https://crests.football-data.org/65.png', true),
    ('sunderland_afc', 71, 'Sunderland AFC', 'Sunderland', 'PL', 'Premier League', 'https://crests.football-data.org/71.png', true),
    ('everton_fc', 62, 'Everton FC', 'Everton', 'PL', 'Premier League', 'https://crests.football-data.org/62.png', true),
    ('ipswich_town_fc', 349, 'Ipswich Town FC', 'Ipswich Town', 'PL', 'Premier League', 'https://crests.football-data.org/349.png', true),
    ('brighton_hove_albion_fc', 397, 'Brighton & Hove Albion FC', 'Brighton & Hove', 'PL', 'Premier League', 'https://crests.football-data.org/397.png', true),
    ('arsenal_fc', 57, 'Arsenal FC', 'Arsenal', 'PL', 'Premier League', 'https://crests.football-data.org/57.png', true),
    ('hull_city_afc', 322, 'Hull City AFC', 'Hull City', 'PL', 'Premier League', 'https://crests.football-data.org/322.png', true),
    ('elche_cf', 285, 'Elche CF', 'Elche', 'PD', 'La Liga', 'https://crests.football-data.org/285.png', true),
    ('real_madrid_cf', 86, 'Real Madrid CF', 'Real Madrid', 'PD', 'La Liga', 'https://crests.football-data.org/86.png', true),
    ('rayo_vallecano_de_madrid', 87, 'Rayo Vallecano de Madrid', 'Rayo Vallecano ', 'PD', 'La Liga', 'https://crests.football-data.org/87.png', true),
    ('rcd_espanyol_de_barcelona', 80, 'RCD Espanyol de Barcelona', 'RCD Espanyol de', 'PD', 'La Liga', 'https://crests.football-data.org/80.png', true),
    ('deportivo_alav_s', 263, 'Deportivo Alavés', 'Deportivo Alavé', 'PD', 'La Liga', 'https://crests.football-data.org/263.png', true),
    ('valencia_cf', 95, 'Valencia CF', 'Valencia', 'PD', 'La Liga', 'https://crests.football-data.org/95.png', true),
    ('villarreal_cf', 94, 'Villarreal CF', 'Villarreal', 'PD', 'La Liga', 'https://crests.football-data.org/94.png', true),
    ('real_betis_balompi', 90, 'Real Betis Balompié', 'Real Betis Balo', 'PD', 'La Liga', 'https://crests.football-data.org/90.png', true),
    ('m_laga_cf', 84, 'Málaga CF', 'Málaga', 'PD', 'La Liga', 'https://crests.football-data.org/84.png', true),
    ('club_atl_tico_de_madrid', 78, 'Club Atlético de Madrid', 'Club Atlético d', 'PD', 'La Liga', 'https://crests.football-data.org/78.png', true),
    ('ca_osasuna', 79, 'CA Osasuna', 'CA Osasuna', 'PD', 'La Liga', 'https://crests.football-data.org/79.png', true),
    ('rc_deportivo_la_coru_a', 560, 'RC Deportivo La Coruña', 'RC Deportivo La', 'PD', 'La Liga', 'https://crests.football-data.org/560.png', true),
    ('sevilla_fc', 559, 'Sevilla FC', 'Sevilla', 'PD', 'La Liga', 'https://crests.football-data.org/559.png', true),
    ('fc_barcelona', 81, 'FC Barcelona', 'FC Barcelona', 'PD', 'La Liga', 'https://crests.football-data.org/81.png', true),
    ('getafe_cf', 82, 'Getafe CF', 'Getafe', 'PD', 'La Liga', 'https://crests.football-data.org/82.png', true),
    ('levante_ud', 88, 'Levante UD', 'Levante UD', 'PD', 'La Liga', 'https://crests.football-data.org/88.png', true),
    ('athletic_club', 77, 'Athletic Club', 'Athletic Club', 'PD', 'La Liga', 'https://crests.football-data.org/77.png', true),
    ('real_racing_club_de_santander', 5335, 'Real Racing Club de Santander', 'Real Racing Clu', 'PD', 'La Liga', 'https://crests.football-data.org/5335.png', true),
    ('real_sociedad_de_f_tbol', 92, 'Real Sociedad de Fútbol', 'Real Sociedad d', 'PD', 'La Liga', 'https://crests.football-data.org/92.png', true),
    ('rc_celta_de_vigo', 558, 'RC Celta de Vigo', 'RC Celta de Vig', 'PD', 'La Liga', 'https://crests.football-data.org/558.png', true),
    ('venezia_fc', 454, 'Venezia FC', 'Venezia', 'SA', 'Serie A', 'https://crests.football-data.org/454.png', true),
    ('ss_lazio', 110, 'SS Lazio', 'SS Lazio', 'SA', 'Serie A', 'https://crests.football-data.org/110.png', true),
    ('fc_internazionale_milano', 108, 'FC Internazionale Milano', 'FC Internaziona', 'SA', 'Serie A', 'https://crests.football-data.org/108.png', true),
    ('udinese_calcio', 115, 'Udinese Calcio', 'Udinese Calcio', 'SA', 'Serie A', 'https://crests.football-data.org/115.png', true),
    ('as_roma', 100, 'AS Roma', 'AS Roma', 'SA', 'Serie A', 'https://crests.football-data.org/100.png', true),
    ('torino_fc', 586, 'Torino FC', 'Torino', 'SA', 'Serie A', 'https://crests.football-data.org/586.png', true),
    ('como_1907', 7397, 'Como 1907', 'Como 1907', 'SA', 'Serie A', 'https://crests.football-data.org/7397.png', true),
    ('parma_calcio_1913', 112, 'Parma Calcio 1913', 'Parma Calcio', 'SA', 'Serie A', 'https://crests.football-data.org/112.png', true),
    ('acf_fiorentina', 99, 'ACF Fiorentina', 'ACF Fiorentina', 'SA', 'Serie A', 'https://crests.football-data.org/99.png', true),
    ('ssc_napoli', 113, 'SSC Napoli', 'SSC Napoli', 'SA', 'Serie A', 'https://crests.football-data.org/113.png', true),
    ('ac_monza', 5911, 'AC Monza', 'AC Monza', 'SA', 'Serie A', 'https://crests.football-data.org/5911.png', true),
    ('us_sassuolo_calcio', 471, 'US Sassuolo Calcio', 'US Sassuolo Cal', 'SA', 'Serie A', 'https://crests.football-data.org/471.png', true),
    ('genoa_cfc', 107, 'Genoa CFC', 'GenoaC', 'SA', 'Serie A', 'https://crests.football-data.org/107.png', true),
    ('frosinone_calcio', 470, 'Frosinone Calcio', 'Frosinone Calci', 'SA', 'Serie A', 'https://crests.football-data.org/470.png', true),
    ('bologna_fc_1909', 103, 'Bologna FC 1909', 'Bologna', 'SA', 'Serie A', 'https://crests.football-data.org/103.png', true),
    ('cagliari_calcio', 104, 'Cagliari Calcio', 'Cagliari Calcio', 'SA', 'Serie A', 'https://crests.football-data.org/104.png', true),
    ('juventus_fc', 109, 'Juventus FC', 'Juventus', 'SA', 'Serie A', 'https://crests.football-data.org/109.png', true),
    ('ac_milan', 98, 'AC Milan', 'AC Milan', 'SA', 'Serie A', 'https://crests.football-data.org/98.png', true),
    ('us_lecce', 5890, 'US Lecce', 'US Lecce', 'SA', 'Serie A', 'https://crests.football-data.org/5890.png', true),
    ('atalanta_bc', 102, 'Atalanta BC', 'Atalanta BC', 'SA', 'Serie A', 'https://crests.football-data.org/102.png', true),
    ('fc_bayern_m_nchen', 5, 'FC Bayern München', 'FC Bayern Münch', 'BL1', 'Bundesliga', 'https://crests.football-data.org/5.png', true),
    ('1_fc_union_berlin', 28, '1. FC Union Berlin', '1. Union Berlin', 'BL1', 'Bundesliga', 'https://crests.football-data.org/28.png', true),
    ('fc_schalke_04', 6, 'FC Schalke 04', 'FC Schalke', 'BL1', 'Bundesliga', 'https://crests.football-data.org/6.png', true),
    ('sv_07_elversberg', 719, 'SV 07 Elversberg', 'SV Elversberg', 'BL1', 'Bundesliga', 'https://crests.football-data.org/719.png', true),
    ('bayer_04_leverkusen', 3, 'Bayer 04 Leverkusen', 'Bayer Leverkuse', 'BL1', 'Bundesliga', 'https://crests.football-data.org/3.png', true),
    ('rb_leipzig', 721, 'RB Leipzig', 'RB Leipzig', 'BL1', 'Bundesliga', 'https://crests.football-data.org/721.png', true),
    ('hamburger_sv', 7, 'Hamburger SV', 'Hamburger SV', 'BL1', 'Bundesliga', 'https://crests.football-data.org/7.png', true),
    ('1_fc_k_ln', 1, '1. FC Köln', '1. Köln', 'BL1', 'Bundesliga', 'https://crests.football-data.org/1.png', true),
    ('borussia_m_nchengladbach', 18, 'Borussia Mönchengladbach', 'Borussia Mönche', 'BL1', 'Bundesliga', 'https://crests.football-data.org/18.png', true),
    ('1_fsv_mainz_05', 15, '1. FSV Mainz 05', '1. FSV Mainz', 'BL1', 'Bundesliga', 'https://crests.football-data.org/15.png', true),
    ('sv_werder_bremen', 12, 'SV Werder Bremen', 'SV Werder Breme', 'BL1', 'Bundesliga', 'https://crests.football-data.org/12.png', true),
    ('fc_augsburg', 16, 'FC Augsburg', 'FC Augsburg', 'BL1', 'Bundesliga', 'https://crests.football-data.org/16.png', true),
    ('eintracht_frankfurt', 19, 'Eintracht Frankfurt', 'Eintracht Frank', 'BL1', 'Bundesliga', 'https://crests.football-data.org/19.png', true),
    ('sc_freiburg', 17, 'SC Freiburg', 'SC Freiburg', 'BL1', 'Bundesliga', 'https://crests.football-data.org/17.png', true),
    ('vfb_stuttgart', 10, 'VfB Stuttgart', 'VfB Stuttgart', 'BL1', 'Bundesliga', 'https://crests.football-data.org/10.png', true),
    ('borussia_dortmund', 4, 'Borussia Dortmund', 'Borussia Dortmu', 'BL1', 'Bundesliga', 'https://crests.football-data.org/4.png', true),
    ('sc_paderborn_07', 29, 'SC Paderborn 07', 'SC Paderborn', 'BL1', 'Bundesliga', 'https://crests.football-data.org/29.png', true),
    ('tsg_1899_hoffenheim', 2, 'TSG 1899 Hoffenheim', 'TSG 1899 Hoffen', 'BL1', 'Bundesliga', 'https://crests.football-data.org/2.png', true),
    ('olympique_lyonnais', 523, 'Olympique Lyonnais', 'Olympique Lyonn', 'FL1', 'Ligue 1', 'https://crests.football-data.org/523.png', true),
    ('stade_rennais_fc_1901', 529, 'Stade Rennais FC 1901', 'Stade Rennais 1', 'FL1', 'Ligue 1', 'https://crests.football-data.org/529.png', true),
    ('paris_fc', 1045, 'Paris FC', 'Paris', 'FL1', 'Ligue 1', 'https://crests.football-data.org/1045.png', true),
    ('rc_strasbourg_alsace', 576, 'RC Strasbourg Alsace', 'RC Strasbourg A', 'FL1', 'Ligue 1', 'https://crests.football-data.org/576.png', true),
    ('angers_sco', 532, 'Angers SCO', 'Angers SCO', 'FL1', 'Ligue 1', 'https://crests.football-data.org/532.png', true),
    ('es_troyes_ac', 531, 'ES Troyes AC', 'ES Troyes AC', 'FL1', 'Ligue 1', 'https://crests.football-data.org/531.png', true),
    ('le_mans_fc', 535, 'Le Mans FC', 'Le Mans', 'FL1', 'Ligue 1', 'https://upload.wikimedia.org/wikipedia/en/5/57/Le_Mans_FC_logo.svg', true),
    ('fc_lorient', 525, 'FC Lorient', 'FC Lorient', 'FL1', 'Ligue 1', 'https://crests.football-data.org/525.png', true),
    ('toulouse_fc', 511, 'Toulouse FC', 'Toulouse', 'FL1', 'Ligue 1', 'https://crests.football-data.org/511.png', true),
    ('le_havre_ac', 533, 'Le Havre AC', 'Le Havre AC', 'FL1', 'Ligue 1', 'https://crests.football-data.org/533.png', true),
    ('aj_auxerre', 519, 'AJ Auxerre', 'AJ Auxerre', 'FL1', 'Ligue 1', 'https://crests.football-data.org/519.png', true),
    ('stade_brestois_29', 512, 'Stade Brestois 29', 'Stade Brestois ', 'FL1', 'Ligue 1', 'https://crests.football-data.org/512.png', true),
    ('as_monaco_fc', 548, 'AS Monaco FC', 'AS Monaco', 'FL1', 'Ligue 1', 'https://crests.football-data.org/548.png', true),
    ('racing_club_de_lens', 546, 'Racing Club de Lens', 'Racing Club de ', 'FL1', 'Ligue 1', 'https://crests.football-data.org/546.png', true),
    ('paris_saint_germain_fc', 524, 'Paris Saint-Germain FC', 'Paris Saint-Ger', 'FL1', 'Ligue 1', 'https://crests.football-data.org/524.png', true),
    ('olympique_de_marseille', 516, 'Olympique de Marseille', 'Olympique de Ma', 'FL1', 'Ligue 1', 'https://crests.football-data.org/516.png', true),
    ('ogc_nice', 522, 'OGC Nice', 'OGC Nice', 'FL1', 'Ligue 1', 'https://crests.football-data.org/522.png', true),
    ('lille_osc', 521, 'Lille OSC', 'Lille OSC', 'FL1', 'Ligue 1', 'https://crests.football-data.org/521.png', true)
ON CONFLICT (id) DO UPDATE SET
    team_id = EXCLUDED.team_id,
    name = EXCLUDED.name,
    short_name = EXCLUDED.short_name,
    competition_code = EXCLUDED.competition_code,
    competition_name = EXCLUDED.competition_name,
    crest_url = EXCLUDED.crest_url,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();
