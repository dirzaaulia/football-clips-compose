from pydantic import BaseModel


class MatchPayload(BaseModel):
    id: int
    competition_id: str
    competition_name: str
    utc_date: str
    status: str
    matchday: int | None = None
    home_team_id: int | None = None
    home_team_name: str
    home_team_crest: str | None = None
    away_team_id: int | None = None
    away_team_name: str
    away_team_crest: str | None = None
    home_score: int | None = None
    away_score: int | None = None
    highlight_video_id: str | None = None
    updated_at: str | None = None

class MatchDbRecord(BaseModel):
    id: int
    status: str
    home_score: int | None = None
    away_score: int | None = None
    highlight_video_id: str | None = None
    last_youtube_check: str | None = None
