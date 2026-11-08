"""Strict Pydantic v2 domain models for taxonomy, broadcasters, clubs, and match state transitions."""

from enum import StrEnum
from typing import Any

from pydantic import BaseModel, Field


class TaxonomyCategory(StrEnum):
    HIGHLIGHT_POSITIVE = "HIGHLIGHT_POSITIVE"
    SHORTS_REEL = "SHORTS_REEL"
    PRESS_CONFERENCE = "PRESS_CONFERENCE"
    INTERVIEW = "INTERVIEW"
    POST_PRE_MATCH = "POST_PRE_MATCH"
    TRAINING_BTS = "TRAINING_BTS"
    FAN_REACTION = "FAN_REACTION"
    PREVIEW = "PREVIEW"
    WOMEN_YOUTH = "WOMEN_YOUTH"
    COMPILATION = "COMPILATION"
    RETRO_SOCIAL = "RETRO_SOCIAL"
    FULL_REPLAY = "FULL_REPLAY"
    GAMING_SIM = "GAMING_SIM"
    STUDIO_DEBATE = "STUDIO_DEBATE"
    CLUB_AFFIXES = "CLUB_AFFIXES"


class TaxonomyEntry(BaseModel):
    category: TaxonomyCategory
    language: str = "global"
    terms: list[str] = Field(default_factory=list)
    regex_patterns: list[str] = Field(default_factory=list)
    override_if_positive_highlight: bool = False
    rejection_label: str = ""


class BroadcasterEntity(BaseModel):
    broadcaster_id: str
    name: str
    channel_handle: str
    channel_id: str | None = None
    competitions: list[str] = Field(default_factory=list)
    allowed_countries: list[str] = Field(default_factory=list)
    contract_start_year: int = 2024
    contract_end_year: int = 2030
    status: str = "ACTIVE"

    @property
    def tier(self) -> int:
        if not self.allowed_countries:
            return 1
        if len(self.allowed_countries) > 3:
            return 2
        return 3


class ClubEntity(BaseModel):
    id: str = ""
    name: str
    short_name: str | None = None
    tla: str | None = None
    youtube_handle: str | None = None
    channel_id: str | None = None
    aliases: list[str] = Field(default_factory=list)


class CompetitionEntity(BaseModel):
    id: str
    name: str
    youtube_handle: str | None = None
    channel_id: str | None = None
    backup_handles: list[str] = Field(default_factory=list)


class TitleClassificationResult(BaseModel):
    is_forbidden: bool = False
    rejection_reason: str | None = None
    matched_category: TaxonomyCategory | None = None
    matched_term: str | None = None
    has_positive_highlight_keyword: bool = False
    positive_keywords_matched: list[str] = Field(default_factory=list)
    masked_title: str = ""


class MatchTransitionResult(BaseModel):
    changes_found: bool = False
    newly_finished_delta: int = 0
    finished_dispatches: list[dict[str, Any]] = Field(default_factory=list)
