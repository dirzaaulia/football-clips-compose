package com.dirzaaulia.footballclips.data.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BigLeague(
    val id: String = "",
    val name: String = "",
    @SerialName("short_name") val shortName: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    @SerialName("emblem_url") val emblemUrl: String? = null,
    @SerialName("emblem_dark_url") val emblemDarkUrl: String? = null,
    @SerialName("league_id") val leagueId: Int? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("display_order") val displayOrder: Int = 99,
    @SerialName("type") val type: String = "league",
    @SerialName("slug") val slug: String? = null,
) {
    // Adapter properties to match previous usage
    val competitionId: String
        get() = id

    fun getEmblemUrl(isDarkTheme: Boolean = true): String {
        return if (isDarkTheme) {
            emblemDarkUrl?.takeIf { it.isNotBlank() } ?: emblemUrl.orEmpty()
        } else {
            emblemUrl.orEmpty()
        }
    }

    val logo: String
        get() = getEmblemUrl(isDarkTheme = true)

    val isCup: Boolean
        get() = type.equals("cup", ignoreCase = true)

    val isHybrid: Boolean
        get() = type.equals("hybrid", ignoreCase = true)

    val isInternational: Boolean
        get() = type.equals("international", ignoreCase = true)

    val isLeague: Boolean
        get() = !isCup && !isHybrid && !isInternational
}
