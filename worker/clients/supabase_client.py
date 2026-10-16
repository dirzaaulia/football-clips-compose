from loguru import logger
from supabase import Client, create_client
from worker.config.settings import settings


class SupabaseClientWrapper:
    def __init__(self):
        if settings.supabase_url:
            key = settings.supabase_service_role_key or "anon-proxy-key"
            self.client: Client = create_client(
                settings.supabase_url,
                key
            )
            logger.info("Supabase client initialized successfully.")
        else:
            self.client = None
            logger.debug("Supabase URL not set, client is None.")

supabase_client = SupabaseClientWrapper().client
