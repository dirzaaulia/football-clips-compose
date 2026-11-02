from fastapi import APIRouter, Query
from worker.services.broadcast_service import broadcast_rights_service

router = APIRouter(prefix="/broadcast", tags=["Broadcast Rights"])


@router.get("/rights")
async def get_broadcast_rights(
    competition: str = Query(default="PL", description="Competition code (e.g. PL, CL, SA, BL1, etc.)"),
    country: str = Query(default="US", description="ISO-3166-1 alpha-2 country code (e.g. US, GB, ID, ES, DE, FR)"),
):
    """Returns official live stream broadcast rights holder and legal direct stream redirection URL."""
    return broadcast_rights_service.resolve_broadcaster(competition_id=competition, country_code=country)


@router.get("/directory")
async def get_broadcast_directory():
    """Returns full worldwide broadcast rights directory covering 195+ countries across all 17 competitions."""
    return broadcast_rights_service.generate_full_world_directory()
