from pydantic import BaseModel, ConfigDict, Field


class StandingRow(BaseModel):
    model_config = ConfigDict(extra="ignore")

    id: str  # "{comp_code}_{season}_{team_id}"
    competition_code: str
    season: int = 2026
    position: int
    team_id: int
    team_name: str
    team_crest: str | None = None
    played: int = 0
    won: int = 0
    drawn: int = 0
    lost: int = 0
    goals_for: int = 0
    goals_against: int = 0
    goal_difference: int = 0
    points: int = 0
    form: str | None = None
    group_name: str | None = None
    updated_at: str | None = None


class CompetitionStandingsPayload(BaseModel):
    model_config = ConfigDict(extra="ignore")

    competition_code: str
    season: int = 2026
    table: list[StandingRow] = Field(default_factory=list)
