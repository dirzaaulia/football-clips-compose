from datetime import UTC, datetime
from pydantic import BaseModel, Field


class CandidateEvaluation(BaseModel):
    video_id: str
    verdict: str = "REJECTED"  # "ACCEPTED" | "REJECTED" | "SKIPPED"
    score: int = 0
    reason: str
    evaluator: str = "DETERMINISTIC_FALLBACK"  # "ORACLE_DEEPSEEK" | "GEMINI_FLASH" | "DETERMINISTIC_FALLBACK"


class VideoCandidate(BaseModel):
    video_id: str
    title: str
    channel_title: str | None = None
    channel_handle: str
    channel_id: str | None = None
    duration_seconds: int = 0
    duration_formatted: str | None = None
    thumbnail_url: str | None = None
    published_at: str | None = None
    is_embeddable: bool = True
    tier: int = 1
    region_info: dict | None = None
    watchability_pct: float = 100.0
    ai_rank: int | None = None
    ai_reason: str | None = None
    evaluation: CandidateEvaluation | None = None


class MatchWaterfallTrace(BaseModel):
    match_id: int
    home_team: str
    away_team: str
    score: str = "0 - 0"
    competition: str = ""
    utc_date: str = ""
    started_at: str = Field(default_factory=lambda: datetime.now(UTC).isoformat())
    duration_ms: int = 0
    channels_searched: list[str] = Field(default_factory=list)
    candidates: list[VideoCandidate] = Field(default_factory=list)
    outcome: str = "PENDING"  # "LINKED" | "NOT_FOUND" | "COOLDOWN_PROTECTED" | "UNAVAILABLE"
    linked_video_id: str | None = None
    linked_video_title: str | None = None
    global_watchability_pct: float = 100.0
    quota_used: int = 0
