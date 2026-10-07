package com.dirzaaulia.footballclips.data.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StandingDto(
    @SerialName("position") val position: Int = 0,
    @SerialName("team_id") val teamId: Long = 0,
    @SerialName("team_name") val teamName: String = "",
    @SerialName("crest_url") val teamCrest: String? = null,
    @SerialName("played_games") val playedGames: Int = 0,
    @SerialName("won") val won: Int = 0,
    @SerialName("draw") val draw: Int = 0,
    @SerialName("lost") val lost: Int = 0,
    @SerialName("points") val points: Int = 0,
    @SerialName("goals_for") val goalsFor: Int = 0,
    @SerialName("goals_against") val goalsAgainst: Int = 0,
    @SerialName("goal_difference") val goalDifference: Int = 0,
    @SerialName("competition_code") val competitionId: String = "",
    @SerialName("team_short_name") val teamShortName: String? = null,
    @SerialName("form") val form: String? = null
)
