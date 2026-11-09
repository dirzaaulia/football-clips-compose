-- Create public.profiles table in PostgreSQL 16 on Oracle VM
CREATE TABLE IF NOT EXISTS public.profiles (
    id TEXT PRIMARY KEY,
    email TEXT,
    display_name TEXT,
    avatar_url TEXT,
    is_premium BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

GRANT SELECT, INSERT, UPDATE, DELETE ON public.profiles TO web_anon;
CREATE INDEX IF NOT EXISTS idx_profiles_email ON public.profiles(email);

-- Create public.app_sessions table for real-time active client version tracking
CREATE TABLE IF NOT EXISTS public.app_sessions (
    device_id TEXT PRIMARY KEY,
    app_version TEXT NOT NULL,
    version_code INT NOT NULL,
    platform TEXT DEFAULT 'android',
    last_active TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

GRANT SELECT, INSERT, UPDATE, DELETE ON public.app_sessions TO web_anon;
CREATE INDEX IF NOT EXISTS idx_app_sessions_last_active ON public.app_sessions(last_active);
CREATE INDEX IF NOT EXISTS idx_app_sessions_version ON public.app_sessions(app_version);

NOTIFY pgrst, 'reload schema';
