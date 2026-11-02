from pydantic import BaseModel, ConfigDict, Field


class AmazonMarketplace(BaseModel):
    model_config = ConfigDict(extra="ignore")

    country_code: str
    marketplace_id: str
    currency: str
    tracking_tag: str
    domain: str


class AffiliateProduct(BaseModel):
    model_config = ConfigDict(extra="ignore")

    asin: str
    title: str
    club_name: str
    category: str  # "JERSEY" | "SCARF" | "BALL" | "MERCH"
    image_url: str | None = None
    affiliate_url: str
    price: str | None = None
    rating: float = 4.5
    reviews_count: int = 0
