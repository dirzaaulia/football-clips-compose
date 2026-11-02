from pydantic import BaseModel, ConfigDict, Field


class BroadcastChannel(BaseModel):
    model_config = ConfigDict(extra="ignore")

    name: str
    type: str = "TV"  # "TV" | "STREAMING" | "FREE_TO_AIR"
    url: str | None = None
    app_package: str | None = None


class CountryBroadcastProfile(BaseModel):
    model_config = ConfigDict(extra="ignore")

    country_code: str  # "ID", "GB", "US"
    country_name: str
    channels: list[BroadcastChannel] = Field(default_factory=list)


class CompetitionBroadcastRights(BaseModel):
    model_config = ConfigDict(extra="ignore")

    competition_code: str
    countries: dict[str, CountryBroadcastProfile] = Field(default_factory=dict)


class BroadcastDirectoryPayload(BaseModel):
    model_config = ConfigDict(extra="ignore")

    competitions: dict[str, CompetitionBroadcastRights] = Field(default_factory=dict)
    version: str = "2026.1"
    last_updated: str | None = None
