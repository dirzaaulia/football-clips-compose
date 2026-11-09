-- Migration: 20261109_shorts_hype_and_rotation.sql
-- Description: Add hype_score and index to football_shorts, add last_shorts_check to supported_clubs for round-robin polling

ALTER TABLE public.football_shorts 
    ADD COLUMN IF NOT EXISTS hype_score INT NOT NULL DEFAULT 50;

CREATE INDEX IF NOT EXISTS idx_football_shorts_hype 
    ON public.football_shorts (hype_score DESC, published_at DESC);

ALTER TABLE public.supported_clubs 
    ADD COLUMN IF NOT EXISTS last_shorts_check TIMESTAMPTZ;
