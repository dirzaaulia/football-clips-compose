import hashlib
import hmac
import json
import os
import urllib.parse
from datetime import UTC, datetime
from typing import Any

from loguru import logger

AMAZON_MARKETPLACES: dict[str, dict[str, str]] = {
    "US": {"host": "webservices.amazon.com", "region": "us-east-1", "domain": "https://www.amazon.com", "currency": "USD"},
    "GB": {"host": "webservices.amazon.co.uk", "region": "eu-west-1", "domain": "https://www.amazon.co.uk", "currency": "GBP"},
    "DE": {"host": "webservices.amazon.de", "region": "eu-west-1", "domain": "https://www.amazon.de", "currency": "EUR"},
    "FR": {"host": "webservices.amazon.fr", "region": "eu-west-1", "domain": "https://www.amazon.fr", "currency": "EUR"},
    "IT": {"host": "webservices.amazon.it", "region": "eu-west-1", "domain": "https://www.amazon.it", "currency": "EUR"},
    "ES": {"host": "webservices.amazon.es", "region": "eu-west-1", "domain": "https://www.amazon.es", "currency": "EUR"},
    "NL": {"host": "webservices.amazon.nl", "region": "eu-west-1", "domain": "https://www.amazon.nl", "currency": "EUR"},
    "BE": {"host": "webservices.amazon.com.be", "region": "eu-west-1", "domain": "https://www.amazon.com.be", "currency": "EUR"},
    "SE": {"host": "webservices.amazon.se", "region": "eu-west-1", "domain": "https://www.amazon.se", "currency": "SEK"},
    "PL": {"host": "webservices.amazon.pl", "region": "eu-west-1", "domain": "https://www.amazon.pl", "currency": "PLN"},
    "TR": {"host": "webservices.amazon.com.tr", "region": "eu-west-1", "domain": "https://www.amazon.com.tr", "currency": "TRY"},
    "JP": {"host": "webservices.amazon.co.jp", "region": "us-west-2", "domain": "https://www.amazon.co.jp", "currency": "JPY"},
    "AU": {"host": "webservices.amazon.com.au", "region": "us-west-2", "domain": "https://www.amazon.com.au", "currency": "AUD"},
    "IN": {"host": "webservices.amazon.in", "region": "eu-west-1", "domain": "https://www.amazon.in", "currency": "INR"},
    "SG": {"host": "webservices.amazon.sg", "region": "us-west-2", "domain": "https://www.amazon.sg", "currency": "SGD"},
    "AE": {"host": "webservices.amazon.ae", "region": "eu-west-1", "domain": "https://www.amazon.ae", "currency": "AED"},
    "SA": {"host": "webservices.amazon.sa", "region": "eu-west-1", "domain": "https://www.amazon.sa", "currency": "SAR"},
    "EG": {"host": "webservices.amazon.eg", "region": "eu-west-1", "domain": "https://www.amazon.eg", "currency": "EGP"},
    "CA": {"host": "webservices.amazon.ca", "region": "us-east-1", "domain": "https://www.amazon.ca", "currency": "CAD"},
    "MX": {"host": "webservices.amazon.com.mx", "region": "us-east-1", "domain": "https://www.amazon.com.mx", "currency": "MXN"},
    "BR": {"host": "webservices.amazon.com.br", "region": "us-east-1", "domain": "https://www.amazon.com.br", "currency": "BRL"},
}

# Exhaustive country fallback resolution to closest marketplace
COUNTRY_MARKETPLACE_MAP: dict[str, str] = {
    # UK & Ireland
    "GB": "GB", "UK": "GB", "IE": "GB",
    # Germany & Central Europe
    "DE": "DE", "AT": "DE", "CH": "DE", "CZ": "DE", "SK": "DE", "HU": "DE", "SI": "DE", "HR": "DE",
    # Spain & Portugal
    "ES": "ES", "PT": "ES", "AD": "ES",
    # France & Francophone
    "FR": "FR", "MC": "FR", "LU": "FR",
    # Italy & Mediterranean
    "IT": "IT", "SM": "IT", "VA": "IT", "MT": "IT", "GR": "IT", "CY": "IT",
    # Netherlands & Nordics
    "NL": "NL", "BE": "BE", "SE": "SE", "NO": "SE", "DK": "SE", "FI": "SE", "IS": "SE",
    # Poland & Baltics
    "PL": "PL", "EE": "PL", "LV": "PL", "LT": "PL",
    # Turkey
    "TR": "TR",
    # Asia-Pacific
    "JP": "JP", "KR": "JP",
    "AU": "AU", "NZ": "AU",
    "IN": "IN", "BD": "IN", "LK": "IN", "NP": "IN",
    "SG": "SG", "MY": "SG", "TH": "SG", "PH": "SG", "VN": "SG",
    # Middle East & North Africa
    "AE": "AE", "KW": "AE", "QA": "AE", "BH": "AE", "OM": "AE",
    "SA": "SA",
    "EG": "EG",
    # Americas
    "CA": "CA",
    "MX": "MX",
    "BR": "BR",
}


class AmazonAffiliateService:
    """
    Automated Amazon Associates affiliate service supporting PA-API 5.0 (AWS SigV4)
    and robust, zero-configuration direct search fallback URLs for all 249 countries.
    """

    def __init__(self):
        self.default_tag = os.getenv("AMAZON_ASSOCIATE_TAG", "drzfc-20")
        self.access_key = os.getenv("AMAZON_ACCESS_KEY")
        self.secret_key = os.getenv("AMAZON_SECRET_KEY")

    def get_marketplace(self, country_code: str | None) -> dict[str, str]:
        if not country_code:
            return AMAZON_MARKETPLACES["US"]
        code = country_code.upper().strip()
        target = COUNTRY_MARKETPLACE_MAP.get(code, "US")
        return AMAZON_MARKETPLACES.get(target, AMAZON_MARKETPLACES["US"])

    def build_club_merchandise_url(self, club_name: str, country_code: str | None = None) -> str:
        """
        Builds a compliant external Amazon search URL for official club jerseys/kits.
        """
        marketplace = self.get_marketplace(country_code)
        domain = marketplace["domain"]
        query = urllib.parse.quote_plus(f"{club_name} official jersey kit")
        return f"{domain}/s?k={query}&tag={self.default_tag}"

    def build_competition_ball_url(self, competition_name: str, country_code: str | None = None) -> str:
        """
        Builds a compliant external Amazon search URL for official competition match balls.
        """
        marketplace = self.get_marketplace(country_code)
        domain = marketplace["domain"]
        query = urllib.parse.quote_plus(f"{competition_name} official match ball")
        return f"{domain}/s?k={query}&tag={self.default_tag}"

    def build_streaming_tech_url(self, country_code: str | None = None) -> str:
        """
        Builds a compliant external Amazon search URL for Fire TV streaming devices.
        """
        marketplace = self.get_marketplace(country_code)
        domain = marketplace["domain"]
        query = urllib.parse.quote_plus("Fire TV Stick 4K")
        return f"{domain}/s?k={query}&tag={self.default_tag}"

    def build_sigv4_headers(
        self,
        payload_bytes: bytes,
        marketplace_host: str,
        region: str,
        amz_date: str,
        datestamp: str,
    ) -> dict[str, str]:
        """
        Constructs AWS Signature Version 4 headers for PA-API 5.0 requests.
        """
        if not self.access_key or not self.secret_key:
            raise ValueError("Amazon PA-API access_key and secret_key required for SigV4 signing.")

        service = "ProductAdvertisingAPI"
        canonical_uri = "/paapi5/searchitems"
        canonical_querystring = ""
        canonical_headers = (
            f"content-encoding:amz-1.0\n"
            f"content-type:application/json; charset=utf-8\n"
            f"host:{marketplace_host}\n"
            f"x-amz-date:{amz_date}\n"
            f"x-amz-target:com.amazon.paapi5.v1.ProductAdvertisingAPIv1.SearchItems\n"
        )
        signed_headers = "content-encoding;content-type;host;x-amz-date;x-amz-target"
        payload_hash = hashlib.sha256(payload_bytes).hexdigest()

        canonical_request = (
            f"POST\n{canonical_uri}\n{canonical_querystring}\n"
            f"{canonical_headers}\n{signed_headers}\n{payload_hash}"
        )

        algorithm = "AWS4-HMAC-SHA256"
        credential_scope = f"{datestamp}/{region}/{service}/aws4_request"
        string_to_sign = (
            f"{algorithm}\n{amz_date}\n{credential_scope}\n"
            f"{hashlib.sha256(canonical_request.encode('utf-8')).hexdigest()}"
        )

        # Signing key generation
        k_date = hmac.new(("AWS4" + self.secret_key).encode("utf-8"), datestamp.encode("utf-8"), hashlib.sha256).digest()
        k_region = hmac.new(k_date, region.encode("utf-8"), hashlib.sha256).digest()
        k_service = hmac.new(k_region, service.encode("utf-8"), hashlib.sha256).digest()
        k_signing = hmac.new(k_service, b"aws4_request", hashlib.sha256).digest()

        signature = hmac.new(k_signing, string_to_sign.encode("utf-8"), hashlib.sha256).hexdigest()
        authorization_header = (
            f"{algorithm} Credential={self.access_key}/{credential_scope}, "
            f"SignedHeaders={signed_headers}, Signature={signature}"
        )

        return {
            "Content-Type": "application/json; charset=utf-8",
            "Content-Encoding": "amz-1.0",
            "X-Amz-Date": amz_date,
            "X-Amz-Target": "com.amazon.paapi5.v1.ProductAdvertisingAPIv1.SearchItems",
            "Authorization": authorization_header,
        }

    async def search_paapi_items(self, keywords: str, country_code: str | None = None) -> list[dict[str, Any]]:
        """
        Executes PA-API 5.0 search if keys are configured, falling back to direct search item representation.
        """
        marketplace = self.get_marketplace(country_code)
        if not self.access_key or not self.secret_key:
            # Fallback: Return structured item with direct affiliate search link
            return [
                {
                    "title": f"Official {keywords} on Amazon",
                    "asin": None,
                    "image_url": None,
                    "affiliate_url": f"{marketplace['domain']}/s?k={urllib.parse.quote_plus(keywords)}&tag={self.default_tag}",
                    "marketplace": country_code or "US",
                }
            ]

        payload = {
            "Keywords": keywords,
            "SearchIndex": "SportingGoods",
            "ItemCount": 3,
            "PartnerTag": self.default_tag,
            "PartnerType": "Associates",
            "Resources": [
                "Images.Primary.Large",
                "ItemInfo.Title",
                "Offers.Listings.Price",
            ],
        }
        payload_bytes = json.dumps(payload).encode("utf-8")
        now = datetime.now(UTC)
        amz_date = now.strftime("%Y%m%dT%H%M%SZ")
        datestamp = now.strftime("%Y%m%d")

        try:
            import httpx
            headers = self.build_sigv4_headers(
                payload_bytes=payload_bytes,
                marketplace_host=marketplace["host"],
                region=marketplace["region"],
                amz_date=amz_date,
                datestamp=datestamp,
            )
            url = f"https://{marketplace['host']}/paapi5/searchitems"
            async with httpx.AsyncClient(timeout=10.0) as client:
                res = await client.post(url, headers=headers, content=payload_bytes)
                if res.status_code == 200:
                    data = res.json()
                    items = []
                    for item in data.get("SearchResult", {}).get("Items", []):
                        items.append({
                            "title": item.get("ItemInfo", {}).get("Title", {}).get("DisplayValue", keywords),
                            "asin": item.get("ASIN"),
                            "image_url": item.get("Images", {}).get("Primary", {}).get("Large", {}).get("URL"),
                            "affiliate_url": item.get("DetailPageURL"),
                            "marketplace": country_code or "US",
                        })
                    if items:
                        return items
                else:
                    logger.warning(f"[PA-API 5.0] Non-200 response ({res.status_code}): {res.text}")
        except Exception as e:
            logger.warning(f"[PA-API 5.0] Request exception: {e}")

        # Fallback to direct search URL
        search_url = f"{marketplace['domain']}/s?k={urllib.parse.quote_plus(keywords)}&tag={self.default_tag}"
        return [{
            "title": f"Official {keywords} on Amazon",
            "asin": None,
            "image_url": None,
            "affiliate_url": search_url,
            "marketplace": country_code or "US",
        }]


amazon_affiliate_service = AmazonAffiliateService()
