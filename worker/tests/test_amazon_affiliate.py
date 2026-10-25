import pytest
from worker.services.amazon_affiliate_service import (
    AMAZON_MARKETPLACES,
    COUNTRY_MARKETPLACE_MAP,
    AmazonAffiliateService,
)


@pytest.fixture
def affiliate_service():
    return AmazonAffiliateService()


def test_tag_in_generated_urls(affiliate_service):
    url = affiliate_service.build_club_merchandise_url("Arsenal", "US")
    assert "tag=drzfc-20" in url
    assert "amazon.com" in url
    assert "Arsenal" in url


def test_uk_geo_targeting(affiliate_service):
    url = affiliate_service.build_club_merchandise_url("Liverpool", "GB")
    assert "amazon.co.uk" in url
    assert "tag=drzfc-20" in url

    # Ireland should route to UK
    ie_url = affiliate_service.build_club_merchandise_url("Liverpool", "IE")
    assert "amazon.co.uk" in ie_url


def test_germany_and_central_europe_geo_targeting(affiliate_service):
    de_url = affiliate_service.build_club_merchandise_url("Bayern Munich", "DE")
    assert "amazon.de" in de_url

    at_url = affiliate_service.build_club_merchandise_url("Bayern Munich", "AT")
    assert "amazon.de" in at_url


def test_japan_and_australia_geo_targeting(affiliate_service):
    jp_url = affiliate_service.build_club_merchandise_url("Real Madrid", "JP")
    assert "amazon.co.jp" in jp_url

    au_url = affiliate_service.build_competition_ball_url("Premier League", "AU")
    assert "amazon.com.au" in au_url


def test_indonesia_fallback(affiliate_service):
    # Indonesia (ID) routes to amazon.com international with tag
    id_url = affiliate_service.build_club_merchandise_url("Manchester United", "ID")
    assert "amazon.com" in id_url
    assert "tag=drzfc-20" in id_url


def test_streaming_tech_url(affiliate_service):
    url = affiliate_service.build_streaming_tech_url("US")
    assert "Fire+TV+Stick+4K" in url
    assert "tag=drzfc-20" in url


import asyncio


def test_search_paapi_items_fallback(affiliate_service):
    # When PA-API keys are unconfigured, graceful fallback to direct search items occurs
    items = asyncio.run(affiliate_service.search_paapi_items("Barcelona jersey", "US"))
    assert len(items) == 1
    assert "Barcelona" in items[0]["title"]
    assert "tag=drzfc-20" in items[0]["affiliate_url"]
    assert "amazon.com" in items[0]["affiliate_url"]

