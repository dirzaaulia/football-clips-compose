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
        get() = when {
            emblemUrl != null && !emblemUrl.contains("football-data.org/ECL.png") &&
                !emblemUrl.contains("football-data.org/FAC.png") &&
                !emblemUrl.contains("football-data.org/EFL.png") &&
                !emblemUrl.contains("football-data.org/CDR.png") &&
                !emblemUrl.contains("football-data.org/DFB.png") &&
                emblemUrl.isNotBlank() -> emblemUrl
            id.equals("ECL", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/20296.png"
            id.equals("FAC", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/40.png"
            id.equals("EFL", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/41.png"
            id.equals("CDR", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/80.png"
            id.equals("DFB", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2061.png"
            id.equals("CIT", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/270.png"
            id.equals("LIB", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/58.png"
            id.equals("SUD", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/1208.png"
            id.equals("REC", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2335.png"
            id.equals("BRA", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/85.png"
            id.equals("ARG", ignoreCase = true) -> "https://a.espncdn.com/i/leaguelogos/soccer/500/1.png"
            else -> emblemUrl ?: ""
        }

    val isCup: Boolean
        get() = type.equals("cup", ignoreCase = true)

    val isHybrid: Boolean
        get() = type.equals("hybrid", ignoreCase = true)

    val isLeague: Boolean
        get() = !isCup && !isHybrid
}
