package com.dirzaaulia.footballclips.data.model.remote.espn

import com.dirzaaulia.footballclips.data.model.remote.FormationsDto
import com.dirzaaulia.footballclips.data.model.remote.StarterPlayerDto
import com.dirzaaulia.footballclips.data.model.remote.TeamFormationDto
import com.dirzaaulia.footballclips.domain.model.PitchPlay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EspnSummaryResponseDto(
    @SerialName("rosters") val rosters: List<EspnRosterDto>? = null,
    @SerialName("boxscore") val boxscore: EspnBoxscoreDto? = null,
    @SerialName("commentary") val commentary: List<EspnCommentaryDto>? = null,
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

    fun toPitchPlays(homeTeamName: String, awayTeamName: String): List<PitchPlay> {
        val commentaryList = commentary ?: return emptyList()
        return commentaryList.mapNotNull { it.toPitchPlay(homeTeamName, awayTeamName) }
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

@Serializable
data class EspnCommentaryDto(
    @SerialName("sequence") val sequence: Int? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("time") val time: EspnClockDto? = null,
    @SerialName("play") val play: EspnPlayDto? = null,
)

@Serializable
data class EspnClockDto(
    @SerialName("displayValue") val displayValue: String? = null,
)

@Serializable
data class EspnPlayDto(
    @SerialName("id") val id: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("clock") val clock: EspnClockDto? = null,
    @SerialName("period") val period: EspnPeriodDto? = null,
    @SerialName("type") val type: EspnPlayTypeDto? = null,
    @SerialName("team") val team: EspnTeamRefDto? = null,
    @SerialName("participants") val participants: List<EspnRosterPlayerDto>? = null,
    @SerialName("fieldPositionX") val fieldPositionX: Float? = null,
    @SerialName("fieldPositionY") val fieldPositionY: Float? = null,
    @SerialName("fieldPosition2X") val fieldPosition2X: Float? = null,
    @SerialName("fieldPosition2Y") val fieldPosition2Y: Float? = null,
)

@Serializable
data class EspnPeriodDto(
    @SerialName("number") val number: Int? = null,
)

@Serializable
data class EspnPlayTypeDto(
    @SerialName("id") val id: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("type") val type: String? = null,
)

@Serializable
data class EspnTeamRefDto(
    @SerialName("displayName") val displayName: String? = null,
)

private fun EspnCommentaryDto.toPitchPlay(homeTeamName: String, awayTeamName: String): PitchPlay? {
    val p = play ?: return null
    val pType = (p.type?.type ?: p.type?.text ?: "").lowercase()
    val isShot = pType.contains("shot") || pType.contains("attempt")
    val isCorner = pType.contains("corner")
    val isGoal = pType.contains("goal")
    val isCard = pType.contains("card")
    val isSub = pType.contains("sub")
    if (!isShot && !isCorner && !isGoal && !isCard && !isSub) return null

    val isHome = p.team?.displayName?.equals(homeTeamName, ignoreCase = true) == true
    val sideName = if (isHome) homeTeamName else awayTeamName
    val clockStr = p.clock?.displayValue ?: time?.displayValue ?: "0'"
    val (fx, fy) = resolvePlayCoords(p, isHome, isShot, isCorner, isGoal, isCard)

    return PitchPlay(
        id = p.id ?: "${clockStr}_${pType}",
        clock = clockStr,
        period = p.period?.number ?: 1,
        type = if (isShot) "shot" else if (isCorner) "corner" else if (isGoal) "goal" else pType,
        typeText = p.type?.text ?: pType.replaceFirstChar { it.uppercase() },
        text = text ?: p.text ?: "$sideName $pType",
        teamName = sideName,
        player = p.participants?.firstOrNull()?.athlete?.displayName,
        xPercent = fx,
        yPercent = fy,
        targetXPercent = p.fieldPosition2X?.times(100f) ?: if (isShot) (if (isHome) 100f else 0f) else null,
        targetYPercent = p.fieldPosition2Y?.times(100f) ?: if (isShot) 50f else null,
    )
}

private fun resolvePlayCoords(
    p: EspnPlayDto,
    isHome: Boolean,
    isShot: Boolean,
    isCorner: Boolean,
    isGoal: Boolean,
    isCard: Boolean,
): Pair<Float, Float> {
    val rawX = p.fieldPositionX?.times(100f)
    val rawY = p.fieldPositionY?.times(100f)
    if (rawX != null && rawY != null) return Pair(rawX, rawY)

    val defX = when {
        isGoal -> if (isHome) 92f else 8f
        isShot -> if (isHome) 80f else 20f
        isCorner -> if (isHome) 98f else 2f
        isCard -> if (isHome) 58f else 42f
        else -> 50f
    }
    val defY = if (isCorner) 6f else 50f
    return Pair(defX, defY)
}
