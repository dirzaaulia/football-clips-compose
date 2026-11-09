WITH all_candidates AS (
    SELECT 
        home_team_id AS team_id,
        home_team_name AS name,
        home_team_crest AS crest_url,
        competition_id,
        competition_name,
        1 AS priority
    FROM matches
    WHERE competition_id IN ('PL', 'PD', 'SA', 'BL1', 'FL1', 'SPL', 'MLS', 'DED', 'PPL')
      AND home_team_id IS NOT NULL 
      AND home_team_name IS NOT NULL
    UNION
    SELECT 
        away_team_id AS team_id,
        away_team_name AS name,
        away_team_crest AS crest_url,
        competition_id,
        competition_name,
        1 AS priority
    FROM matches
    WHERE competition_id IN ('PL', 'PD', 'SA', 'BL1', 'FL1', 'SPL', 'MLS', 'DED', 'PPL')
      AND away_team_id IS NOT NULL 
      AND away_team_name IS NOT NULL
    UNION
    SELECT 
        home_team_id AS team_id,
        home_team_name AS name,
        home_team_crest AS crest_url,
        competition_id,
        competition_name,
        2 AS priority
    FROM matches
    WHERE competition_id IN ('CL', 'EL', 'ECL')
      AND home_team_id IS NOT NULL 
      AND home_team_name IS NOT NULL
    UNION
    SELECT 
        away_team_id AS team_id,
        away_team_name AS name,
        away_team_crest AS crest_url,
        competition_id,
        competition_name,
        2 AS priority
    FROM matches
    WHERE competition_id IN ('CL', 'EL', 'ECL')
      AND away_team_id IS NOT NULL 
      AND away_team_name IS NOT NULL
),
deduped_teams AS (
    SELECT DISTINCT ON (lower(regexp_replace(regexp_replace(name, '[^a-zA-Z0-9]+', '_', 'g'), '^_+|_+$', '', 'g')))
        lower(regexp_replace(regexp_replace(name, '[^a-zA-Z0-9]+', '_', 'g'), '^_+|_+$', '', 'g')) AS id,
        team_id,
        name,
        substring(name from 1 for 15) AS short_name,
        competition_id AS competition_code,
        competition_name,
        crest_url,
        true AS is_active,
        now() AS updated_at
    FROM all_candidates
    ORDER BY lower(regexp_replace(regexp_replace(name, '[^a-zA-Z0-9]+', '_', 'g'), '^_+|_+$', '', 'g')), priority ASC, team_id
)
INSERT INTO supported_clubs (id, team_id, name, short_name, competition_code, competition_name, crest_url, is_active, updated_at)
SELECT id, team_id, name, short_name, competition_code, competition_name, crest_url, is_active, updated_at
FROM deduped_teams
ON CONFLICT (id) DO UPDATE SET
    team_id = EXCLUDED.team_id,
    crest_url = COALESCE(EXCLUDED.crest_url, supported_clubs.crest_url),
    competition_code = EXCLUDED.competition_code,
    competition_name = EXCLUDED.competition_name,
    updated_at = now();
