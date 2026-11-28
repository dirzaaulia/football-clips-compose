-- Migration: 20261128_dedup_supported_clubs_unique_team_id.sql
-- Adds competition_codes TEXT[] array, merges duplicate team_id clubs (preferring domestic league),
-- and enforces UNIQUE(team_id) permanently.

ALTER TABLE public.supported_clubs
ADD COLUMN IF NOT EXISTS competition_codes TEXT[] DEFAULT '{}';

WITH ranked AS (
    SELECT
        id,
        team_id,
        competition_code,
        competition_name,
        youtube_handle,
        youtube_channel_id,
        backup_channel_handle,
        crest_url,
        ROW_NUMBER() OVER (
            PARTITION BY team_id
            ORDER BY
                CASE
                    WHEN competition_code IN ('PL','PD','SA','BL1','FL1','SPL','MLS','DED','PPL','IDN','BRA','ARG','ENG2','JPN1','TUR1','MEX1','BEL1') THEN 0
                    WHEN competition_code IN ('CL','EL','ECL','ACL','ACL2','LIB','SUD','REC') THEN 1
                    ELSE 2
                END ASC,
                (youtube_channel_id IS NOT NULL AND youtube_channel_id != '') DESC,
                created_at ASC
        ) AS rn
    FROM public.supported_clubs
    WHERE team_id IS NOT NULL
),
merged AS (
    SELECT
        team_id,
        ARRAY_AGG(DISTINCT competition_code) FILTER (WHERE competition_code IS NOT NULL) AS all_codes,
        MAX(CASE WHEN rn = 1 THEN id END) AS keep_id,
        MAX(CASE WHEN youtube_handle IS NOT NULL AND youtube_handle != '' THEN youtube_handle END) AS best_handle,
        MAX(CASE WHEN youtube_channel_id IS NOT NULL AND youtube_channel_id != '' THEN youtube_channel_id END) AS best_channel_id,
        MAX(CASE WHEN backup_channel_handle IS NOT NULL AND backup_channel_handle != '' THEN backup_channel_handle END) AS best_backup,
        MAX(CASE WHEN crest_url IS NOT NULL AND crest_url != '' THEN crest_url END) AS best_crest
    FROM ranked
    GROUP BY team_id
)
UPDATE public.supported_clubs sc
SET
    competition_codes = m.all_codes,
    youtube_handle = COALESCE(NULLIF(sc.youtube_handle, ''), m.best_handle),
    youtube_channel_id = COALESCE(NULLIF(sc.youtube_channel_id, ''), m.best_channel_id),
    backup_channel_handle = COALESCE(NULLIF(sc.backup_channel_handle, ''), m.best_backup),
    crest_url = COALESCE(NULLIF(sc.crest_url, ''), m.best_crest),
    updated_at = NOW()
FROM merged m
WHERE sc.id = m.keep_id;

WITH ranked_del AS (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY team_id
            ORDER BY
                CASE
                    WHEN competition_code IN ('PL','PD','SA','BL1','FL1','SPL','MLS','DED','PPL','IDN','BRA','ARG','ENG2','JPN1','TUR1','MEX1','BEL1') THEN 0
                    WHEN competition_code IN ('CL','EL','ECL','ACL','ACL2','LIB','SUD','REC') THEN 1
                    ELSE 2
                END ASC,
                (youtube_channel_id IS NOT NULL AND youtube_channel_id != '') DESC,
                created_at ASC
        ) AS rn
    FROM public.supported_clubs
    WHERE team_id IS NOT NULL
)
DELETE FROM public.supported_clubs
WHERE id IN (SELECT id FROM ranked_del WHERE rn > 1);

UPDATE public.supported_clubs
SET competition_codes = ARRAY[competition_code]
WHERE (competition_codes IS NULL OR cardinality(competition_codes) = 0)
  AND competition_code IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_supported_clubs_team_id
ON public.supported_clubs (team_id)
WHERE team_id IS NOT NULL;
