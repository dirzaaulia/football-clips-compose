-- ==============================================================================
-- FOOTBALL CLIPS - THEME-AWARE COMPETITION EMBLEMS & DEVICE TELEMETRY
-- Adds emblem_dark_url to competitions and theme_mode to device telemetry
-- ==============================================================================

-- 1. Add emblem_dark_url to competitions
ALTER TABLE IF EXISTS public.competitions
    ADD COLUMN IF NOT EXISTS emblem_dark_url TEXT;

-- 2. Populate emblem_dark_url with dark-mode variants where applicable
-- ESPN CDN uses /500/ for default/light and /500-dark/ for dark backgrounds
UPDATE public.competitions
SET emblem_dark_url = CASE
    WHEN emblem_url LIKE '%/500-dark/%' THEN emblem_url
    WHEN emblem_url LIKE '%/500/%' THEN REPLACE(emblem_url, '/500/', '/500-dark/')
    ELSE emblem_url
END
WHERE emblem_dark_url IS NULL;

-- Ensure emblem_url uses standard/light variants
UPDATE public.competitions
SET emblem_url = REPLACE(emblem_url, '/500-dark/', '/500/')
WHERE emblem_url LIKE '%/500-dark/%';

-- 3. Add theme_mode to devices telemetry table if table exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'devices') THEN
        ALTER TABLE public.devices ADD COLUMN IF NOT EXISTS theme_mode VARCHAR(20) DEFAULT 'dark';
    END IF;
END $$;

-- 4. Reload PostgREST schema cache
NOTIFY pgrst, 'reload schema';
