from pydantic import BaseModel, ConfigDict, Field


class QuotaMetrics(BaseModel):
    model_config = ConfigDict(extra="ignore")

    used_today: int = 0
    daily_limit: int = 10000
    percentage_used: float = 0.0
    resets_at: str | None = None


class PipelineStatus(BaseModel):
    model_config = ConfigDict(extra="ignore")

    name: str
    status: str = "IDLE"  # "IDLE" | "RUNNING" | "DEGRADED" | "ERROR"
    last_run: str | None = None
    items_processed: int = 0
    errors_count: int = 0


class EventRecord(BaseModel):
    model_config = ConfigDict(extra="ignore")

    id: str | None = None
    created_at: str
    pipeline: str
    service: str
    level: str  # "INFO" | "WARNING" | "ERROR"
    match_id: int | None = None
    job_id: str | None = None
    trace_id: str | None = None
    message: str


class CycleTelemetryPayload(BaseModel):
    model_config = ConfigDict(extra="ignore")

    cycle_id: str
    started_at: str
    ended_at: str | None = None
    duration_ms: int = 0
    quota: QuotaMetrics = Field(default_factory=QuotaMetrics)
    pipelines: list[PipelineStatus] = Field(default_factory=list)
    events: list[EventRecord] = Field(default_factory=list)
