from pydantic import BaseModel


class SupportedClubRecord(BaseModel):
    id: str
    team_id: int
    name: str
    short_name: str
    competition_code: str
    competition_name: str
    crest_url: str | None = None
    aliases: list[str] = []
    is_active: bool = True
    updated_at: str | None = None
