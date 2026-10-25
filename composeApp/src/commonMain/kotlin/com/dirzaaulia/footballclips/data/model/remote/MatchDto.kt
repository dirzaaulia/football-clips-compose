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
    @SerialName("playback_mode") val playbackMode: String? = null,
    @SerialName("backup_highlight_video_id") val backupHighlightVideoId: String? = null,
    @SerialName("recap_analysis") val recapAnalysis: String? = null,
    @SerialName("round_name") val roundName: String? = null,
    @SerialName("display_clock") val displayClock: String? = null,
    @SerialName("provider") val provider: String? = null,
    @SerialName("season") val season: Int = 2026,
    @SerialName("formations") val formations: FormationsDto? = null,
    @SerialName("statistics") val statistics: Map<String, Map<String, String>>? = null,
    @SerialName("editorial_headline") val editorialHeadline: String? = null,
    @SerialName("editorial_story") val editorialStory: String? = null,
    @SerialName("editorial_source") val editorialSource: String? = null,
    @SerialName("events") val events: List<MatchEventDto>? = null,
    @SerialName("youtube_region_info") val youtubeRegionInfo: RegionRestrictionDto? = null,
    @SerialName("backup_youtube_region_info") val backupYoutubeRegionInfo: RegionRestrictionDto? = null,
    @SerialName("viewer_availability") val viewerAvailability: String? = null,
    @SerialName("external_video_id") val externalVideoId: String? = null
)

@Serializable
data class MatchEventDto(
    @SerialName("id") val id: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("type_text") val typeText: String? = null,
    @SerialName("clock") val clock: String? = null,
    @SerialName("period") val period: Int? = null,
    @SerialName("side") val side: String? = null,
    @SerialName("scoring_play") val scoringPlay: Boolean = false,
    @SerialName("player") val player: String = "",
    @SerialName("assist") val assist: String? = null,
    @SerialName("text") val text: String = ""
)

@Serializable
data class RegionRestrictionDto(
    @SerialName("allowed") val allowed: List<String>? = null,
    @SerialName("blocked") val blocked: List<String>? = null
)

