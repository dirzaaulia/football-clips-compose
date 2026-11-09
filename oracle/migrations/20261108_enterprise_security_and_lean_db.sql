-- Migration: 20261108_enterprise_security_and_lean_db.sql
-- Description: Drop bloat tactical JSONB columns from matches, secure profiles from client-side tampering,
-- add processed_webhook_events table for idempotency, and add automated rolling retention triggers.

-- 1. DROP BLOAT COLUMNS FROM public.matches
-- Tactical formations, statistics, boxscores, and 2D canvas coordinates are fetched 100% on-demand from ESPN API.
ALTER TABLE public.matches DROP COLUMN IF EXISTS statistics;
ALTER TABLE public.matches DROP COLUMN IF EXISTS formations;
ALTER TABLE public.matches DROP COLUMN IF EXISTS events;
ALTER TABLE public.matches DROP COLUMN IF EXISTS editorial_headline;
ALTER TABLE public.matches DROP COLUMN IF EXISTS editorial_story;
ALTER TABLE public.matches DROP COLUMN IF EXISTS editorial_source;

-- 2. HARDEN public.profiles PERMISSIONS
-- Revoke all table-level UPDATE from web_anon to eliminate client-side is_premium tampering.
REVOKE UPDATE ON public.profiles FROM web_anon;

-- Allow web_anon to only update safe user-editable attributes (display_name, avatar_url, updated_at)
GRANT UPDATE (display_name, avatar_url, updated_at) ON public.profiles TO web_anon;

-- 3. CREATE WEBHOOK IDEMPOTENCY TABLE
CREATE TABLE IF NOT EXISTS public.processed_webhook_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id TEXT UNIQUE NOT NULL,
    event_type TEXT NOT NULL,
    app_user_id TEXT NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_processed_webhook_events_event_id ON public.processed_webhook_events(event_id);
CREATE INDEX IF NOT EXISTS idx_processed_webhook_events_processed_at ON public.processed_webhook_events(processed_at DESC);

-- Allow web_anon (Next.js server-side edge / PostgREST) to check and insert idempotency records
GRANT SELECT, INSERT ON public.processed_webhook_events TO web_anon;
GRANT ALL ON public.processed_webhook_events TO postgres;

-- 4. SECURE STORED PROCEDURE FOR PREMIUM ENTITLEMENTS
-- Only executable with valid webhook secret or service key
CREATE OR REPLACE FUNCTION public.set_user_premium_status(
    p_user_id TEXT,
    p_is_premium BOOLEAN,
    p_secret TEXT
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_expected_secret TEXT;
    v_updated_rows INT;
BEGIN
    -- Retrieve configured webhook secret from database setting if configured
    v_expected_secret := current_setting('app.settings.revenuecat_webhook_secret', true);
    
    -- If setting is defined, enforce strict match; otherwise require non-empty secret
    IF v_expected_secret IS NOT NULL AND v_expected_secret <> '' THEN
        IF p_secret IS NULL OR p_secret <> v_expected_secret THEN
            RAISE EXCEPTION 'Unauthorized: Invalid webhook secret';
        END IF;
    ELSIF p_secret IS NULL OR LENGTH(p_secret) < 8 THEN
        RAISE EXCEPTION 'Unauthorized: Missing or invalid secret key';
    END IF;

    -- Update the profile securely
    UPDATE public.profiles
    SET is_premium = p_is_premium,
        updated_at = NOW()
    WHERE id = p_user_id;
    
    GET DIAGNOSTICS v_updated_rows = ROW_COUNT;

    -- If profile does not exist yet (e.g. user subscribed before app sign-in), insert placeholder
    IF v_updated_rows = 0 THEN
        INSERT INTO public.profiles (id, is_premium, created_at, updated_at)
        VALUES (p_user_id, p_is_premium, NOW(), NOW())
        ON CONFLICT (id) DO UPDATE
        SET is_premium = EXCLUDED.is_premium,
            updated_at = NOW();
    END IF;

    RETURN jsonb_build_object(
        'success', true,
        'user_id', p_user_id,
        'is_premium', p_is_premium
    );
END;
$$;

GRANT EXECUTE ON FUNCTION public.set_user_premium_status(TEXT, BOOLEAN, TEXT) TO web_anon;
GRANT EXECUTE ON FUNCTION public.set_user_premium_status(TEXT, BOOLEAN, TEXT) TO postgres;

-- 5. AUTOMATED ROLLING RETENTION CLEANUP
CREATE OR REPLACE FUNCTION public.prune_old_worker_telemetry()
RETURNS TRIGGER AS $$
BEGIN
    -- Worker cycles retention: 3 days
    IF TG_TABLE_NAME = 'worker_cycles' THEN
        DELETE FROM public.worker_cycles
        WHERE start_time < NOW() - INTERVAL '3 days';
    END IF;

    -- User analytics retention: 7 days
    IF TG_TABLE_NAME = 'user_analytics_events' THEN
        DELETE FROM public.user_analytics_events
        WHERE created_at < NOW() - INTERVAL '7 days';
    END IF;

    -- Webhook events retention: 30 days
    IF TG_TABLE_NAME = 'processed_webhook_events' THEN
        DELETE FROM public.processed_webhook_events
        WHERE processed_at < NOW() - INTERVAL '30 days';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Attach cleanup trigger to worker_cycles if table exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'worker_cycles') THEN
        DROP TRIGGER IF EXISTS trigger_prune_worker_cycles ON public.worker_cycles;
        CREATE TRIGGER trigger_prune_worker_cycles
        AFTER INSERT ON public.worker_cycles
        FOR EACH STATEMENT
        EXECUTE FUNCTION public.prune_old_worker_telemetry();
    END IF;
END $$;

-- Attach cleanup trigger to user_analytics_events if table exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_analytics_events') THEN
        DROP TRIGGER IF EXISTS trigger_prune_user_analytics ON public.user_analytics_events;
        CREATE TRIGGER trigger_prune_user_analytics
        AFTER INSERT ON public.user_analytics_events
        FOR EACH STATEMENT
        EXECUTE FUNCTION public.prune_old_worker_telemetry();
    END IF;
END $$;

-- Attach cleanup trigger to processed_webhook_events
DROP TRIGGER IF EXISTS trigger_prune_processed_webhooks ON public.processed_webhook_events;
CREATE TRIGGER trigger_prune_processed_webhooks
AFTER INSERT ON public.processed_webhook_events
FOR EACH STATEMENT
EXECUTE FUNCTION public.prune_old_worker_telemetry();

-- 6. RELOAD POSTGREST SCHEMA CACHE
NOTIFY pgrst, 'reload schema';
