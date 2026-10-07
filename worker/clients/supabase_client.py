from loguru import logger
from supabase import Client, create_client
from worker.config.settings import settings


class SupabaseClientWrapper:
    def __init__(self):
        if settings.supabase_url and settings.supabase_service_role_key:
            self.client: Client = create_client(
                settings.supabase_url,
                settings.supabase_service_role_key
            )
            logger.info("Supabase client initialized successfully.")
        else:
            self.client = None
            logger.debug("Supabase credentials not set, client is None.")

supabase_client = SupabaseClientWrapper().client
