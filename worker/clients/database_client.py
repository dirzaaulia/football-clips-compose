"""
Database client for PostgreSQL 16 on Oracle VM Super-Node
accessed via PostgREST enterprise edge tier.
"""

from loguru import logger
from supabase import Client, create_client

from worker.config.settings import settings


class DatabaseClientWrapper:
    def __init__(self):
        if settings.supabase_url:
            key = settings.supabase_service_role_key or "anon-proxy-key"
            self.client: Client = create_client(
                settings.supabase_url,
                key,
            )
            logger.info("Database client initialized successfully.")
        else:
            self.client = None
            logger.debug("Database URL not set, client is None.")


_wrapper = DatabaseClientWrapper()
db_client = _wrapper.client
database_client = _wrapper.client
# Alias for test patches and backward compatibility
supabase_client = _wrapper.client
