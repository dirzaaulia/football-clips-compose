-- Migration: 20261128_reorganize_competition_display_order.sql
-- Reorganizes competitions.display_order into a collision-free 1..38 market-priority ranking.

UPDATE public.competitions
SET display_order = CASE id
    -- Tier 1: Top Europe (1..8)
    WHEN 'CL'           THEN 1
    WHEN 'PL'           THEN 2
    WHEN 'PD'           THEN 3
    WHEN 'SA'           THEN 4
    WHEN 'BL1'          THEN 5
    WHEN 'FL1'          THEN 6
    WHEN 'EL'           THEN 7
    WHEN 'ECL'          THEN 8
    -- Tier 2: Indonesia, Asia & Star-Power (9..14)
    WHEN 'IDN'          THEN 9
    WHEN 'ACL'          THEN 10
    WHEN 'ACL2'         THEN 11
    WHEN 'SPL'          THEN 12
    WHEN 'MLS'          THEN 13
    WHEN 'JPN1'         THEN 14
    -- Tier 3: Domestic Cups & High-Interest Europe (15..24)
    WHEN 'FAC'          THEN 15
    WHEN 'EFL'          THEN 16
    WHEN 'CDR'          THEN 17
    WHEN 'DFB'          THEN 18
    WHEN 'CIT'          THEN 19
    WHEN 'DED'          THEN 20
    WHEN 'ENG2'         THEN 21
    WHEN 'BEL1'         THEN 22
    WHEN 'PPL'          THEN 23
    WHEN 'TUR1'         THEN 24
    -- Tier 4: Americas & International (25..38)
    WHEN 'LIB'          THEN 25
    WHEN 'BRA'          THEN 26
    WHEN 'ARG'          THEN 27
    WHEN 'MEX1'         THEN 28
    WHEN 'SUD'          THEN 29
    WHEN 'REC'          THEN 30
    WHEN 'UNL'          THEN 31
    WHEN 'WCQ_AFC'      THEN 32
    WHEN 'WCQ_UEFA'     THEN 33
    WHEN 'WCQ_CONMEBOL' THEN 34
    WHEN 'CNL'          THEN 35
    WHEN 'WCQ_CONCACAF' THEN 36
    WHEN 'WCQ_CAF'      THEN 37
    WHEN 'FRI'          THEN 38
    ELSE display_order
END,
updated_at = NOW()
WHERE id IN (
    'CL','PL','PD','SA','BL1','FL1','EL','ECL',
    'IDN','ACL','ACL2','SPL','MLS','JPN1',
    'FAC','EFL','CDR','DFB','CIT','DED','ENG2','BEL1','PPL','TUR1',
    'LIB','BRA','ARG','MEX1','SUD','REC',
    'UNL','WCQ_AFC','WCQ_UEFA','WCQ_CONMEBOL','CNL','WCQ_CONCACAF','WCQ_CAF','FRI'
);
