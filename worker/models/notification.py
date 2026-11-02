from pydantic import BaseModel, ConfigDict, Field


class PlatformBreakdown(BaseModel):
    model_config = ConfigDict(extra="ignore")

    android: int = 0
    ios: int = 0
    web: int = 0


class AudienceMetrics(BaseModel):
    model_config = ConfigDict(extra="ignore")

    eligible_tokens: int = 0
    delivered: int = 0
    failed: int = 0
    delivery_rate: float = 0.0
    platforms: PlatformBreakdown = Field(default_factory=PlatformBreakdown)


class NotificationEntry(BaseModel):
    model_config = ConfigDict(extra="ignore")

    id: str
    type: str  # "MATCH_HIGHLIGHT" | "GOAL_ALERT" | "LINEUP" | "MATCH_FINISHED"
    title: str
    body: str
    match_id: int | None = None
    competition_code: str | None = None
    teams: str | None = None
    created_at: str
    status: str = "DELIVERED"
    audience: AudienceMetrics = Field(default_factory=AudienceMetrics)


class NotificationsStatePayload(BaseModel):
    model_config = ConfigDict(extra="ignore")

    history: list[NotificationEntry] = Field(default_factory=list)
    stats: dict[str, int] = Field(default_factory=dict)
    last_updated: str | None = None
