package com.dirzaaulia.footballclips.data.affiliate

import com.dirzaaulia.footballclips.util.getDeviceCountryCode
import io.ktor.http.encodeURLParameter

object AmazonUrlBuilder {

    fun buildClubMerchandiseUrl(
        clubName: String,
        countryCode: String? = null,
        category: String = "jersey",
    ): String {
        val cc = countryCode ?: runCatching { getDeviceCountryCode() }.getOrNull()
        val marketplace = AffiliateConfiguration.resolveMarketplace(cc)
        val query = "$clubName official $category".trim()
        val encodedQuery = query.encodeURLParameter()
        return "https://www.$marketplace/s?k=$encodedQuery&tag=${AffiliateConfiguration.AMAZON_ASSOCIATE_TAG}"
    }

    fun buildMatchDaySearchUrl(
        homeClub: String,
        awayClub: String,
        countryCode: String? = null,
    ): String {
        val cc = countryCode ?: runCatching { getDeviceCountryCode() }.getOrNull()
        val marketplace = AffiliateConfiguration.resolveMarketplace(cc)
        val query = "$homeClub vs $awayClub matchday scarf jersey".trim()
        val encodedQuery = query.encodeURLParameter()
        return "https://www.$marketplace/s?k=$encodedQuery&tag=${AffiliateConfiguration.AMAZON_ASSOCIATE_TAG}"
    }

    fun buildCompetitionBallUrl(
        competitionName: String,
        countryCode: String? = null,
    ): String {
        val cc = countryCode ?: runCatching { getDeviceCountryCode() }.getOrNull()
        val marketplace = AffiliateConfiguration.resolveMarketplace(cc)
        val query = "$competitionName official match ball".trim()
        val encodedQuery = query.encodeURLParameter()
        return "https://www.$marketplace/s?k=$encodedQuery&tag=${AffiliateConfiguration.AMAZON_ASSOCIATE_TAG}"
    }

    fun buildStreamingDeviceUrl(countryCode: String? = null): String {
        val cc = countryCode ?: runCatching { getDeviceCountryCode() }.getOrNull()
        val marketplace = AffiliateConfiguration.resolveMarketplace(cc)
        val query = "Fire TV Stick 4K streaming device".trim()
        val encodedQuery = query.encodeURLParameter()
        return "https://www.$marketplace/s?k=$encodedQuery&tag=${AffiliateConfiguration.AMAZON_ASSOCIATE_TAG}"
    }
}
