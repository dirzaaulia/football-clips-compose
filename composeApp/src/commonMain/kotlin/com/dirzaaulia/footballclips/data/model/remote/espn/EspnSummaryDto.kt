package com.dirzaaulia.footballclips.data.model.remote.espn

import com.dirzaaulia.footballclips.data.model.remote.FormationsDto
import com.dirzaaulia.footballclips.data.model.remote.StarterPlayerDto
import com.dirzaaulia.footballclips.data.model.remote.TeamFormationDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EspnSummaryResponseDto(
    @SerialName("rosters") val rosters: List<EspnRosterDto>? = null,
    @SerialName("boxscore") val boxscore: EspnBoxscoreDto? = null,
) {
    fun toFormationsDto(): FormationsDto? {
        val rosterList = rosters ?: return null
        var homeFormation: TeamFormationDto? = null
        var awayFormation: TeamFormationDto? = null

        for (item in rosterList) {
            val side = item.homeAway?.lowercase() ?: continue
            val starters = item.roster
                ?.filter { it.starter == true }
                ?.map { p ->
                    StarterPlayerDto(
                        name = p.athlete?.displayName ?: "Player",
                        jersey = p.jersey ?: "",
                        position = p.position?.abbreviation ?: "",
                    )
                }
                ?: emptyList()

            val formation = TeamFormationDto(
                formation = item.formation,
                starters = starters,
            )

            if (side == "home") {
                homeFormation = formation
            } else if (side == "away") {
                awayFormation = formation
            }
        }

        return if (homeFormation != null || awayFormation != null) {
            FormationsDto(home = homeFormation, away = awayFormation)
        } else {
            null
        }
    }

    fun toStatisticsMap(): Map<String, Map<String, String>>? {
        val teams = boxscore?.teams ?: return null
        val homeStats = mutableMapOf<String, String>()
        val awayStats = mutableMapOf<String, String>()

        for ((idx, teamObj) in teams.withIndex()) {
            val isHome = idx == 0
            val targetMap = if (isHome) homeStats else awayStats
            for (stat in teamObj.statistics.orEmpty()) {
                val name = stat.name ?: stat.label ?: continue
                val value = stat.displayValue ?: continue
                targetMap[name] = value
            }
        }

        return if (homeStats.isNotEmpty() || awayStats.isNotEmpty()) {
            mapOf("home" to homeStats, "away" to awayStats)
        } else {
            null
        }
    }
}

@Serializable
data class EspnRosterDto(
    @SerialName("homeAway") val homeAway: String? = null,
    @SerialName("formation") val formation: String? = null,
    @SerialName("roster") val roster: List<EspnRosterPlayerDto>? = null,
)

@Serializable
data class EspnRosterPlayerDto(
    @SerialName("starter") val starter: Boolean? = null,
    @SerialName("jersey") val jersey: String? = null,
    @SerialName("athlete") val athlete: EspnAthleteDto? = null,
    @SerialName("position") val position: EspnPositionDto? = null,
)

@Serializable
data class EspnAthleteDto(
    @SerialName("id") val id: String? = null,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("shortName") val shortName: String? = null,
)

@Serializable
data class EspnPositionDto(
    @SerialName("abbreviation") val abbreviation: String? = null,
    @SerialName("name") val name: String? = null,
)

@Serializable
data class EspnBoxscoreDto(
    @SerialName("teams") val teams: List<EspnBoxscoreTeamDto>? = null,
)

@Serializable
data class EspnBoxscoreTeamDto(
    @SerialName("statistics") val statistics: List<EspnStatItemDto>? = null,
)

@Serializable
data class EspnStatItemDto(
    @SerialName("name") val name: String? = null,
    @SerialName("label") val label: String? = null,
    @SerialName("displayValue") val displayValue: String? = null,
)
