import httpx
from loguru import logger
from worker.config.settings import settings

SUPABASE_CLOUD_URL = "https://eiomktvavndorazreyba.supabase.co"

# Exact 28 columns supported on Supabase Cloud matches table
ALLOWED_MATCH_COLUMNS = {
    "away_score", "away_team_crest", "away_team_id", "away_team_name",
    "competition_id", "competition_name", "display_clock", "editorial_headline",
    "editorial_source", "editorial_story", "formations", "highlight_video_id",
    "home_score", "home_team_crest", "home_team_id", "home_team_name", "id",
    "last_youtube_check", "matchday", "provider", "region_restriction",
    "round_name", "season", "statistics", "status", "updated_at", "utc_date",
    "youtube_region_info"
}


class SupabaseCloudClient:
    """
    Dedicated client ensuring backward compatibility with Legacy Supabase Cloud tier
    (eiomktvavndorazreyba.supabase.co).
    Replicates all match updates, highlights, and standings so older Android clients
    on Google Play continue receiving real-time data seamlessly.
    """

    def __init__(self):
        self.cloud_url = SUPABASE_CLOUD_URL
        self.api_key = settings.supabase_service_role_key

    @property
    def is_primary_cloud(self) -> bool:
        return settings.supabase_url.rstrip("/") == self.cloud_url.rstrip("/")

    @property
    def headers(self) -> dict[str, str]:
        return {
            "apikey": self.api_key,
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
            "Prefer": "resolution=merge-duplicates",
        }

    def sync_matches(self, matches: list[dict]):
        """Dual-write matches batch to Supabase Cloud."""
        if self.is_primary_cloud or not matches:
            return
        try:
            # Normalize every object to have identical 28 keys to prevent PostgREST PGRST102
            sanitized = [
                {col: m.get(col, None) for col in sorted(ALLOWED_MATCH_COLUMNS)}
                for m in matches
            ]
            batch_size = 50
            with httpx.Client(timeout=15.0) as client:
                for i in range(0, len(sanitized), batch_size):
                    batch = sanitized[i:i + batch_size]
                    resp = client.post(
                        f"{self.cloud_url}/rest/v1/matches",
                        headers=self.headers,
                        json=batch,
                    )
                    if resp.status_code >= 400:
                        logger.warning(f"[SupabaseCloudSync] Match batch {i} failed: {resp.status_code} - {resp.text[:150]}")
            logger.info(f"[SupabaseCloudSync] Successfully replicated {len(sanitized)} matches to Supabase Cloud.")
        except Exception as e:
            logger.warning(f"[SupabaseCloudSync] Failed to replicate matches: {e}")

    def update_match_highlight(
        self,
        match_id: int | str,
        highlight_video_id: str,
        youtube_region_info: dict | None = None,
        last_youtube_check: str | None = None,
    ):
        """Dual-write single match highlight update to Supabase Cloud."""
        if self.is_primary_cloud:
            return
        try:
            update_data: dict = {
                "highlight_video_id": highlight_video_id,
            }
            if last_youtube_check:
                update_data["last_youtube_check"] = last_youtube_check
            if youtube_region_info is not None:
                update_data["youtube_region_info"] = youtube_region_info

            headers = {
                "apikey": self.api_key,
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json",
                "Prefer": "return=minimal",
            }
            with httpx.Client(timeout=10.0) as client:
                resp = client.patch(
                    f"{self.cloud_url}/rest/v1/matches?id=eq.{match_id}",
                    headers=headers,
                    json=update_data,
                )
                if resp.status_code >= 400:
                    logger.warning(f"[SupabaseCloudSync] Match {match_id} highlight update failed: {resp.status_code} - {resp.text[:150]}")
                else:
                    logger.info(f"[SupabaseCloudSync] Replicated highlight {highlight_video_id} for match {match_id} to Cloud.")
        except Exception as e:
            logger.warning(f"[SupabaseCloudSync] Failed to replicate highlight for match {match_id}: {e}")

    def sync_standings(self, standings: list[dict]):
        """Dual-write standings to Supabase Cloud."""
        if self.is_primary_cloud or not standings:
            return
        try:
            # Normalize keys across all objects to satisfy PostgREST PGRST102
            all_keys = sorted({k for s in standings for k in s.keys()})
            normalized = [
                {k: s.get(k, None) for k in all_keys}
                for s in standings
            ]
            batch_size = 50
            with httpx.Client(timeout=15.0) as client:
                for i in range(0, len(normalized), batch_size):
                    batch = normalized[i:i + batch_size]
                    resp = client.post(
                        f"{self.cloud_url}/rest/v1/standings",
                        headers=self.headers,
                        json=batch,
                    )
                    if resp.status_code >= 400:
                        logger.warning(f"[SupabaseCloudSync] Standings batch {i} failed: {resp.status_code} - {resp.text[:150]}")
            logger.info(f"[SupabaseCloudSync] Replicated {len(standings)} standings to Supabase Cloud.")
        except Exception as e:
            logger.warning(f"[SupabaseCloudSync] Failed to replicate standings: {e}")


supabase_cloud_client = SupabaseCloudClient()
