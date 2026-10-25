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

    private val CLUB_KIT_IMAGES: Map<String, String> = mapOf(
        "arsenal" to "https://images.footballfanatics.com/arsenal/arsenal-adidas-home-shirt-2024-25_ss5_p-200924976+u-2xvdubqffy8gq6qvycct+v-3mffoeyy4lffh8hlyptn.jpg",
        "real madrid" to "https://images.footballfanatics.com/real-madrid/real-madrid-adidas-home-shirt-2024-25_ss5_p-200924956+u-7n36hvy7wqqi9o4y0zpt+v-mcvgflzrt8f8045vbt4q.jpg",
        "manchester city" to "https://images.footballfanatics.com/manchester-city/manchester-city-puma-home-shirt-2024-25_ss5_p-200924933+u-8f37h8d7j3j19i2z8vbt+v-2mv8flzrt8f8045vbt4q.jpg",
        "barcelona" to "https://images.footballfanatics.com/barcelona/barcelona-nike-home-stadium-shirt-2024-25_ss5_p-200924922+u-4f37h8d7j3j19i2z8vbt+v-4mv8flzrt8f8045vbt4q.jpg",
        "liverpool" to "https://images.footballfanatics.com/liverpool/liverpool-nike-home-stadium-shirt-2024-25_ss5_p-200924911+u-3f37h8d7j3j19i2z8vbt+v-1mv8flzrt8f8045vbt4q.jpg",
        "chelsea" to "https://images.footballfanatics.com/chelsea/chelsea-nike-home-stadium-shirt-2024-25_ss5_p-200924944+u-9f37h8d7j3j19i2z8vbt+v-5mv8flzrt8f8045vbt4q.jpg",
        "manchester united" to "https://images.footballfanatics.com/manchester-united/manchester-united-adidas-home-shirt-2024-25_ss5_p-200924901+u-1f37h8d7j3j19i2z8vbt+v-6mv8flzrt8f8045vbt4q.jpg",
        "bayern munich" to "https://images.footballfanatics.com/bayern-munich/fc-bayern-munich-adidas-home-shirt-2024-25_ss5_p-200924966+u-5f37h8d7j3j19i2z8vbt+v-7mv8flzrt8f8045vbt4q.jpg",
        "paris saint-germain" to "https://images.footballfanatics.com/paris-saint-germain/paris-saint-germain-nike-home-stadium-shirt-2024-25_ss5_p-200924988+u-6f37h8d7j3j19i2z8vbt+v-8mv8flzrt8f8045vbt4q.jpg",
        "psg" to "https://images.footballfanatics.com/paris-saint-germain/paris-saint-germain-nike-home-stadium-shirt-2024-25_ss5_p-200924988+u-6f37h8d7j3j19i2z8vbt+v-8mv8flzrt8f8045vbt4q.jpg",
        "juventus" to "https://images.footballfanatics.com/juventus/juventus-adidas-home-shirt-2024-25_ss5_p-200924999+u-7f37h8d7j3j19i2z8vbt+v-9mv8flzrt8f8045vbt4q.jpg",
        "inter milan" to "https://images.footballfanatics.com/inter-milan/inter-milan-nike-home-stadium-shirt-2024-25_ss5_p-200924955+u-8f37h8d7j3j19i2z8vbt+v-0mv8flzrt8f8045vbt4q.jpg",
        "ac milan" to "https://images.footballfanatics.com/ac-milan/ac-milan-puma-home-shirt-2024-25_ss5_p-200924977+u-9f37h8d7j3j19i2z8vbt+v-1mv8flzrt8f8045vbt4q.jpg",
        "tottenham hotspur" to "https://images.footballfanatics.com/tottenham-hotspur/tottenham-hotspur-nike-home-stadium-shirt-2024-25_ss5_p-200924933+u-0f37h8d7j3j19i2z8vbt+v-2mv8flzrt8f8045vbt4q.jpg",
        "atletico madrid" to "https://images.footballfanatics.com/atletico-de-madrid/atletico-de-madrid-nike-home-stadium-shirt-2024-25_ss5_p-200924911+u-1f37h8d7j3j19i2z8vbt+v-3mv8flzrt8f8045vbt4q.jpg",
        "borussia dortmund" to "https://images.footballfanatics.com/borussia-dortmund/borussia-dortmund-puma-home-shirt-2024-25_ss5_p-200924922+u-2f37h8d7j3j19i2z8vbt+v-4mv8flzrt8f8045vbt4q.jpg",
    )

    fun resolveClubJerseyImageUrl(clubName: String, crestUrl: String?): String {
        val lower = clubName.lowercase().trim()
        for ((key, url) in CLUB_KIT_IMAGES) {
            if (lower.contains(key)) return url
        }
        return crestUrl.orEmpty()
    }
}

