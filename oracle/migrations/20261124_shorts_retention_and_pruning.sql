-- Migration: 20261124_shorts_retention_and_pruning.sql
-- Description: Add index for reverse-chronological pagination and pruning function for rolling pool

CREATE INDEX IF NOT EXISTS idx_football_shorts_published_hype
    ON public.football_shorts (published_at DESC, hype_score DESC);

CREATE OR REPLACE FUNCTION public.prune_stale_shorts(
    p_max_age_days INT DEFAULT 30,
    p_max_capacity INT DEFAULT 3000
)
RETURNS INT
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_deleted_ttl INT := 0;
    v_deleted_cap INT := 0;
    v_total INT;
    v_excess INT;
BEGIN
    -- 1. Evict shorts older than p_max_age_days
    DELETE FROM public.football_shorts
    WHERE published_at < (NOW() - (p_max_age_days || ' days')::INTERVAL);
    GET DIAGNOSTICS v_deleted_ttl = ROW_COUNT;

    -- 2. Evict lowest-ranked excess shorts if total exceeds p_max_capacity
    SELECT COUNT(*) INTO v_total FROM public.football_shorts;
    IF v_total > p_max_capacity THEN
        v_excess := v_total - p_max_capacity;
        DELETE FROM public.football_shorts
        WHERE id IN (
            SELECT id FROM public.football_shorts
            ORDER BY is_featured ASC, hype_score ASC, published_at ASC
            LIMIT v_excess
        );
        GET DIAGNOSTICS v_deleted_cap = ROW_COUNT;
    END IF;

    RETURN v_deleted_ttl + v_deleted_cap;
END;
$$;
