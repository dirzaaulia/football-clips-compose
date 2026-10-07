from pydantic import BaseModel


class VideoCandidate(BaseModel):
    video_id: str
    title: str
    channel_handle: str
    channel_id: str | None = None
    duration_seconds: int = 0
    is_embeddable: bool = True
    tier: int = 1
    region_info: dict | None = None
    ai_rank: int | None = None
    ai_reason: str | None = None
