package com.dirzaaulia.footballclips.data.affiliate

object AffiliateConfiguration {
    const val AMAZON_ASSOCIATE_TAG = "drzfc-20"
    const val DEFAULT_MARKETPLACE = "amazon.com"
    const val AFFILIATE_DISCLOSURE = "As an Amazon Associate, FootballClips earns from qualifying purchases."

    val AMAZON_MARKETPLACES: Map<String, String> = mapOf(
        "US" to "amazon.com",
        "CA" to "amazon.ca",
        "MX" to "amazon.com.mx",
        "BR" to "amazon.com.br",
        "GB" to "amazon.co.uk",
        "DE" to "amazon.de",
        "FR" to "amazon.fr",
        "ES" to "amazon.es",
        "IT" to "amazon.it",
        "NL" to "amazon.nl",
        "PL" to "amazon.pl",
        "SE" to "amazon.se",
        "BE" to "amazon.com.be",
        "TR" to "amazon.com.tr",
        "JP" to "amazon.co.jp",
        "IN" to "amazon.in",
        "AU" to "amazon.com.au",
        "SG" to "amazon.sg",
        "AE" to "amazon.ae",
        "SA" to "amazon.sa",
        "EG" to "amazon.eg",
    )

    private val REGIONAL_FALLBACK_MAP: Map<String, String> = mapOf(
        // UK & Ireland
        "IE" to "amazon.co.uk",
        // DACH & Central / Eastern Europe (route to DE)
        "AT" to "amazon.de",
        "CH" to "amazon.de",
        "CZ" to "amazon.de",
        "HU" to "amazon.de",
        "SK" to "amazon.de",
        "SI" to "amazon.de",
        "HR" to "amazon.de",
        "RO" to "amazon.de",
        "BG" to "amazon.de",
        "GR" to "amazon.de",
        "CY" to "amazon.de",
        // Nordic & Baltic
        "NO" to "amazon.se",
        "DK" to "amazon.de",
        "FI" to "amazon.de",
        "EE" to "amazon.de",
        "LV" to "amazon.de",
        "LT" to "amazon.pl",
        // Western Europe
        "PT" to "amazon.es",
        "LU" to "amazon.fr",
        "MC" to "amazon.fr",
        // Asia-Pacific
        "NZ" to "amazon.com.au",
        "MY" to "amazon.sg",
        "ID" to "amazon.com",
        "PH" to "amazon.com",
        "TH" to "amazon.com",
        "VN" to "amazon.com",
        "KR" to "amazon.co.jp",
        "TW" to "amazon.co.jp",
        "HK" to "amazon.com",
        // Middle East & North Africa
        "QA" to "amazon.ae",
        "KW" to "amazon.ae",
        "BH" to "amazon.ae",
        "OM" to "amazon.ae",
        "JO" to "amazon.ae",
        "LB" to "amazon.ae",
        // South America
        "AR" to "amazon.com.br",
        "CL" to "amazon.com",
        "CO" to "amazon.com",
        "PE" to "amazon.com",
        "UY" to "amazon.com.br",
    )

    fun resolveMarketplace(countryCode: String?): String {
        if (countryCode.isNullOrBlank()) return DEFAULT_MARKETPLACE
        val normalized = countryCode.trim().uppercase()
        return AMAZON_MARKETPLACES[normalized]
            ?: REGIONAL_FALLBACK_MAP[normalized]
            ?: DEFAULT_MARKETPLACE
    }

    fun resolveClubJerseyImageUrl(clubName: String, crestUrl: String?): String {
        return ClubKitRegistry.resolveKitPhoto(clubName) ?: crestUrl.orEmpty()
    }
}

