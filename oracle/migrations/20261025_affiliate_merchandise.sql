-- FootballClips Oracle VM PostgreSQL 16 Migration
-- Table: affiliate_merchandise
-- Stores automated Amazon Associates merchandise recommendations (Jerseys, Balls, Fan Gear)

CREATE TABLE IF NOT EXISTS affiliate_merchandise (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    club_id TEXT,
    club_name TEXT NOT NULL,
    competition_code TEXT,
    category TEXT NOT NULL, -- 'KIT', 'BALL', 'FAN_GEAR', 'STREAMING_TECH'
    product_title TEXT NOT NULL,
    asin TEXT,
    image_url TEXT,
    affiliate_url TEXT NOT NULL,
    marketplace TEXT NOT NULL DEFAULT 'US',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_affiliate_club_category ON affiliate_merchandise(club_name, category);
CREATE INDEX IF NOT EXISTS idx_affiliate_marketplace ON affiliate_merchandise(marketplace);
CREATE INDEX IF NOT EXISTS idx_affiliate_active ON affiliate_merchandise(is_active);

-- Comment for PostgREST schema introspection
COMMENT ON TABLE affiliate_merchandise IS 'Curated Amazon Associates physical merchandise and fan gear for clubs and competitions';
