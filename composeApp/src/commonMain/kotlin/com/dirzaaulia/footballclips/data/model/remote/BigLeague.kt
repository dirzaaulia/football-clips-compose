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
    @SerialName("league_id") val leagueId: Int? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("display_order") val displayOrder: Int = 99,
    @SerialName("type") val type: String = "league",
) {
    // Adapter properties to match previous usage
    val competitionId: String
        get() = id

    val logo: String
        get() = emblemUrl ?: ""

    val isCup: Boolean
        get() = type.equals("cup", ignoreCase = true)

    val isHybrid: Boolean
        get() = type.equals("hybrid", ignoreCase = true)

    val isLeague: Boolean
        get() = !isCup && !isHybrid
}
