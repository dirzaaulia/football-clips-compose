package com.dirzaaulia.footballclips.data.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompetitionDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("short_name") val shortName: String? = null,
    @SerialName("country_code") val countryCode: String? = null,
    @SerialName("emblem_url") val emblemUrl: String? = null,
    @SerialName("league_id") val leagueId: Int? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("display_order") val displayOrder: Int = 99
)
