-- ==============================================================================
-- FOOTBALL CLIPS - WORKER TELEMETRY & RETENTION MIGRATION
-- ==============================================================================

-- 1. worker_cycles table
CREATE TABLE IF NOT EXISTS worker_cycles (
    id          BIGSERIAL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    timestamp   TEXT NOT NULL,
    mode        TEXT NOT NULL,
    next_sleep  TEXT,
    cycle_index BIGINT
);

CREATE INDEX IF NOT EXISTS idx_cycles_created_at ON worker_cycles (created_at DESC);

-- 2. worker_events table
CREATE TABLE IF NOT EXISTS worker_events (
    id           BIGSERIAL PRIMARY KEY,
    cycle_id     BIGINT NOT NULL REFERENCES worker_cycles(id) ON DELETE CASCADE,
    step_title   TEXT NOT NULL,
    step_status  TEXT NOT NULL,
    event_text   TEXT NOT NULL,
    event_order  INT  NOT NULL,
    is_highlight BOOLEAN GENERATED ALWAYS AS (
        event_text LIKE '%BERHASIL LINK%' OR event_text LIKE '%🏆 [DAPAT]%'
    ) STORED,
    is_goal      BOOLEAN GENERATED ALWAYS AS (
        event_text LIKE '%⚽ [SKOR!]%' OR event_text LIKE '%🔄 [STATUS]%'
    ) STORED,
    is_error     BOOLEAN GENERATED ALWAYS AS (
        event_text LIKE '%🚨%' OR event_text LIKE '%429%'
    ) STORED
);

CREATE INDEX IF NOT EXISTS idx_events_cycle ON worker_events (cycle_id);
CREATE INDEX IF NOT EXISTS idx_events_flags ON worker_events (is_highlight, is_goal, is_error);

-- 3. Row Level Security (RLS)
ALTER TABLE worker_cycles ENABLE ROW LEVEL SECURITY;
ALTER TABLE worker_events ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE tablename = 'worker_cycles' AND policyname = 'public_read_cycles'
    ) THEN
        CREATE POLICY "public_read_cycles" ON worker_cycles FOR SELECT USING (true);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_policies WHERE tablename = 'worker_events' AND policyname = 'public_read_events'
    ) THEN
        CREATE POLICY "public_read_events" ON worker_events FOR SELECT USING (true);
    END IF;
END $$;

-- 4. Retention Policy (pg_cron - if extension is enabled in Supabase)
-- Note: Worker sync_all.py also executes cleanup during Step 3 Maintenance.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pg_cron') THEN
        -- Daily cleanup of worker cycles older than 15 days (cascades to events)
        PERFORM cron.schedule(
            'delete_old_cycles',
            '0 3 * * *',
            'DELETE FROM worker_cycles WHERE created_at < NOW() - INTERVAL ''15 days'''
        );
        -- Daily cleanup of matches older than 15 days (existing retention)
        PERFORM cron.schedule(
            'delete_old_matches',
            '0 2 * * *',
            'DELETE FROM matches WHERE utc_date < NOW() - INTERVAL ''15 days'''
        );
        -- Clean up cron run history older than 7 days
        PERFORM cron.schedule(
            'cleanup_cron_history',
            '30 3 * * *',
            'DELETE FROM cron.job_run_details WHERE end_time < NOW() - INTERVAL ''7 days'''
        );
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'pg_cron schedule skipped or already registered';
END $$;
