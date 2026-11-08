from datetime import UTC, datetime
from enum import Enum
from pydantic import BaseModel, Field


class ShortCategory(str, Enum):
    GOALS_SKILLS = "GOALS_SKILLS"
    MOMENTS = "MOMENTS"
    BEHIND_THE_SCENES = "BEHIND_THE_SCENES"
    COMMERCIAL_SPAM = "COMMERCIAL_SPAM"
    PRESS_CONFERENCE = "PRESS_CONFERENCE"


class ShortEvaluationResult(BaseModel):
    video_id: str
    verdict: str = "REJECTED"  # "ACCEPTED" | "REJECTED"
    category: ShortCategory = ShortCategory.GOALS_SKILLS
    confidence: int = 0
    reason: str
    evaluator: str = "ORACLE_AI_GATEWAY"


class FootballShortCandidate(BaseModel):
    video_id: str
    title: str
    channel_id: str
    channel_name: str
    description: str = ""
    thumbnail_url: str
    duration_seconds: int = 45
    published_at: str
    club_id: str | int | None = None
    evaluation: ShortEvaluationResult | None = None


class FootballShortRecord(BaseModel):
    id: str | None = None
    youtube_video_id: str
    title: str
    channel_id: str
    channel_name: str
    club_id: int | None = None
    category: str = ShortCategory.GOALS_SKILLS.value
    thumbnail_url: str
    duration_seconds: int = 45
    like_count: int = 0
    is_featured: bool = False
    published_at: str
    created_at: str = Field(default_factory=lambda: datetime.now(UTC).isoformat())


class ShortsWaterfallTrace(BaseModel):
    started_at: str = Field(default_factory=lambda: datetime.now(UTC).isoformat())
    duration_ms: int = 0
    channels_queried: list[str] = Field(default_factory=list)
    candidates: list[FootballShortCandidate] = Field(default_factory=list)
    accepted_count: int = 0
    rejected_count: int = 0
    ingested_video_ids: list[str] = Field(default_factory=list)
