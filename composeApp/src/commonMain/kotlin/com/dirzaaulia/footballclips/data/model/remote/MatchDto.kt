package com.dirzaaulia.footballclips.data.model.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StarterPlayerDto(
    @SerialName("name") val name: String,
    @SerialName("jersey") val jersey: String = "",
    @SerialName("position") val position: String = ""
)

@Serializable
data class TeamFormationDto(
    @SerialName("formation") val formation: String? = null,
    @SerialName("starters") val starters: List<StarterPlayerDto> = emptyList()
)

@Serializable
data class FormationsDto(
    @SerialName("home") val home: TeamFormationDto? = null,
    @SerialName("away") val away: TeamFormationDto? = null
)

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
    @SerialName("highlight_video_id") val highlightVideoId: String? = null,
    @SerialName("recap_analysis") val recapAnalysis: String? = null,
    @SerialName("round_name") val roundName: String? = null,
    @SerialName("display_clock") val displayClock: String? = null,
    @SerialName("provider") val provider: String? = null,
    @SerialName("season") val season: Int = 2026,
    @SerialName("formations") val formations: FormationsDto? = null,
    @SerialName("statistics") val statistics: Map<String, Map<String, String>>? = null,
    @SerialName("editorial_headline") val editorialHeadline: String? = null,
    @SerialName("editorial_story") val editorialStory: String? = null,
    @SerialName("editorial_source") val editorialSource: String? = null
)

