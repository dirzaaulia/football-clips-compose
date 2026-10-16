from datetime import timedelta, timezone
from zoneinfo import ZoneInfo

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=(".env", "worker/.env"),
        env_file_encoding="utf-8",
        extra="ignore"
    )

    supabase_url: str = Field(default="", alias="SUPABASE_URL")
    supabase_service_role_key: str = Field(default="", alias="SUPABASE_SERVICE_ROLE_KEY")
    football_data_api_key: str = Field(default="", alias="FOOTBALL_DATA_API_KEY")
    youtube_api_key: str = Field(default="", alias="YOUTUBE_API_KEY")
    gemini_api_key: str | None = Field(default=None, alias="GEMINI_API_KEY")
    match_data_provider: str = Field(default="football_data", alias="MATCH_DATA_PROVIDER")
    standings_provider: str = Field(default="espn", alias="STANDINGS_PROVIDER")
    r2_account_id: str | None = Field(default=None, alias="R2_ACCOUNT_ID")
    r2_access_key_id: str | None = Field(default=None, alias="R2_ACCESS_KEY_ID")
    r2_secret_access_key: str | None = Field(default=None, alias="R2_SECRET_ACCESS_KEY")
    r2_bucket_name: str = Field(default="footballclips-archives", alias="R2_BUCKET_NAME")

    # Target competitions: Premier League, LaLiga, Serie A, Bundesliga, Ligue 1, Champions League, Europa League, Conference League, Domestic Cups, SPL, MLS, Eredivisie, Liga Portugal
    target_competitions: list[str] = ["PL", "PD", "SA", "BL1", "FL1", "CL", "EL", "ECL", "FAC", "EFL", "CDR", "DFB", "CIT", "SPL", "MLS", "DED", "PPL"]
    big_five_competitions: list[str] = ["PL", "PD", "SA", "BL1", "FL1"]

    # YouTube Quota & Durations
    max_quota_per_day: int = 10000
    min_duration_seconds: int = 45
    max_duration_seconds: int = 1500

    # Retention window in days (keeps catalog populated even during 2-week FIFA breaks)
    retention_days: int = 30

    # Forward upcoming fixture window in days (keeps full month catalog of future matches)
    upcoming_fixture_days: int = 30

    # Sleep intervals in seconds
    sleep_live_match: int = 60       # 60 seconds (1 minute) for near-live match & score sync
    sleep_near_match: int = 900      # 15 minutes when match kicks off soon
    sleep_normal: int = 7200         # 2 hours idle
    youtube_live_standby_interval: int = 900  # 15 minutes minimum between YouTube highlight sweeps during live play

    @property
    def wib_tz(self):
        try:
            return ZoneInfo("Asia/Jakarta")
        except Exception:
            return timezone(timedelta(hours=7))

    @property
    def pacific_tz(self):
        try:
            return ZoneInfo("America/Los_Angeles")
        except Exception:
            return timezone(timedelta(hours=-7))

settings = Settings()
