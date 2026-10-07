package com.dirzaaulia.footballclips.data.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchDto(
    @SerialName("id") val id: Long,
    @SerialName("competition_id") val competitionId: String,
    @SerialName("competition_name") val competitionName: String,
    @SerialName("utc_date") val utcDate: String,
    @SerialName("status") val status: String,
    @SerialName("matchday") val matchday: Int? = null,
    @SerialName("home_team_id") val homeTeamId: Long? = null,
    @SerialName("home_team_name") val homeTeamName: String,
    @SerialName("home_team_crest") val homeTeamCrest: String? = null,
    @SerialName("away_team_id") val awayTeamId: Long? = null,
    @SerialName("away_team_name") val awayTeamName: String,
    @SerialName("away_team_crest") val awayTeamCrest: String? = null,
    @SerialName("home_score") val homeScore: Int? = null,
    @SerialName("away_score") val awayScore: Int? = null,
    @SerialName("highlight_video_id") val highlightVideoId: String? = null
)
