package com.dirzaaulia.footballclips.data.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClubDto(
    @SerialName("id") val id: String,
    @SerialName("team_id") val teamId: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("short_name") val shortName: String? = null,
    @SerialName("competition_code") val competitionCode: String,
    @SerialName("competition_name") val competitionName: String,
    @SerialName("crest_url") val crestUrl: String? = null,
    @SerialName("aliases") val aliases: List<String> = emptyList(),
    @SerialName("is_active") val isActive: Boolean = true
)
