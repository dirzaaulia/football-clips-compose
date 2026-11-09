-- Migration: 20261108_create_football_shorts.sql
-- Description: Create table for short-form football clips with 45-day retention policy and RLS for web_anon

CREATE TABLE IF NOT EXISTS public.football_shorts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    youtube_video_id VARCHAR(11) UNIQUE NOT NULL,
    title TEXT NOT NULL,
    channel_id VARCHAR(32) NOT NULL,
    channel_name VARCHAR(100) NOT NULL,
    club_id INT REFERENCES public.supported_clubs(id) ON DELETE SET NULL,
    category VARCHAR(32) NOT NULL DEFAULT 'GOALS_SKILLS',
    thumbnail_url TEXT NOT NULL,
    duration_seconds INT NOT NULL DEFAULT 45,
    like_count INT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS idx_football_shorts_published_at ON public.football_shorts (published_at DESC);
CREATE INDEX IF NOT EXISTS idx_football_shorts_club_id ON public.football_shorts (club_id);
CREATE INDEX IF NOT EXISTS idx_football_shorts_category ON public.football_shorts (category);

-- 45-Day Rolling Retention Trigger / Function
CREATE OR REPLACE FUNCTION public.prune_old_football_shorts()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM public.football_shorts
    WHERE published_at < NOW() - INTERVAL '45 days'
      AND is_featured = FALSE;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_prune_football_shorts ON public.football_shorts;
CREATE TRIGGER trigger_prune_football_shorts
AFTER INSERT ON public.football_shorts
FOR EACH STATEMENT
EXECUTE FUNCTION public.prune_old_football_shorts();

-- Row Level Security (RLS) & Permissions
ALTER TABLE public.football_shorts ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies
        WHERE tablename = 'football_shorts' AND policyname = 'Allow public read access to football_shorts'
    ) THEN
        CREATE POLICY "Allow public read access to football_shorts"
        ON public.football_shorts
        FOR SELECT
        TO public
        USING (true);
    END IF;
END $$;

GRANT SELECT ON public.football_shorts TO web_anon;
GRANT ALL ON public.football_shorts TO postgres;
