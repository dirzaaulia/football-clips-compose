"""Typed facade for the PostgreSQL / seed Broadcaster Registry (`BroadcasterEntity`)."""

from typing import Any

from worker.models.taxonomy_models import BroadcasterEntity
from worker.repositories.channel_repository import channel_repository
from worker.repositories.taxonomy_seed_loader import load_seed_broadcasters

BROADCASTER_ENTITIES: list[BroadcasterEntity] = load_seed_broadcasters()
GLOBAL_BROADCASTER_REGISTRY: list[dict[str, Any]] = [b.model_dump() for b in BROADCASTER_ENTITIES]


def get_broadcaster_entities(comp_id: str | None = None, country_code: str | None = None) -> list[BroadcasterEntity]:
    """Returns typed BroadcasterEntity models from ChannelRepository (PostgreSQL + seed fallback)."""
    all_broadcasters = channel_repository.get_broadcasters() or BROADCASTER_ENTITIES
    if not comp_id:
        return [b for b in all_broadcasters if b.status == "ACTIVE"]

    comp_clean = comp_id.upper().strip()
    results: list[BroadcasterEntity] = []
    for b in all_broadcasters:
        if b.status != "ACTIVE":
            continue
        if comp_clean in b.competitions:
            if not country_code or not b.allowed_countries or country_code.upper() in b.allowed_countries:
                results.append(b)
    return results


def get_broadcasters_for_competition(comp_id: str, country_code: str | None = None) -> list[dict[str, Any]]:
    """Backward-compatible helper returning serialized BroadcasterEntity dicts."""
    return [b.model_dump() for b in get_broadcaster_entities(comp_id, country_code)]
