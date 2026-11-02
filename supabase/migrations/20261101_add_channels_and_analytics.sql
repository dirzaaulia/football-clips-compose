-- ==============================================================================
-- FOOTBALL CLIPS - CHANNELS REGISTRY & USER ANALYTICS MIGRATION
-- Season 2026/2027
-- ==============================================================================

-- 1. Add channel fields to competitions
ALTER TABLE IF EXISTS public.competitions
    ADD COLUMN IF NOT EXISTS youtube_handle TEXT,
    ADD COLUMN IF NOT EXISTS youtube_channel_id TEXT,
    ADD COLUMN IF NOT EXISTS backup_channel_handles TEXT[] DEFAULT '{}',
    ADD COLUMN IF NOT EXISTS backup_channel_ids TEXT[] DEFAULT '{}';

-- 2. Add channel fields to supported_clubs
ALTER TABLE IF EXISTS public.supported_clubs
    ADD COLUMN IF NOT EXISTS youtube_handle TEXT,
    ADD COLUMN IF NOT EXISTS youtube_channel_id TEXT;

-- 3. Create user_analytics_events table
CREATE TABLE IF NOT EXISTS public.user_analytics_events (
    id BIGSERIAL PRIMARY KEY,
    event_name TEXT NOT NULL,
    user_id TEXT,
    device_id TEXT,
    platform TEXT NOT NULL,
    payload JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_analytics_event_name ON public.user_analytics_events(event_name);
CREATE INDEX IF NOT EXISTS idx_user_analytics_created ON public.user_analytics_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_analytics_user ON public.user_analytics_events(user_id);

-- RLS & Grants for web_anon & service_role
ALTER TABLE public.user_analytics_events ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE tablename = 'user_analytics_events' AND policyname = 'public_insert_analytics'
    ) THEN
        CREATE POLICY "public_insert_analytics" ON public.user_analytics_events FOR INSERT WITH CHECK (true);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE tablename = 'user_analytics_events' AND policyname = 'public_read_analytics'
    ) THEN
        CREATE POLICY "public_read_analytics" ON public.user_analytics_events FOR SELECT USING (true);
    END IF;
END $$;

GRANT SELECT, INSERT ON public.user_analytics_events TO web_anon;
GRANT ALL ON public.user_analytics_events TO postgres;
GRANT ALL ON public.user_analytics_events TO fc_operations;

NOTIFY pgrst, 'reload schema';
