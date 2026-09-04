package com.dirzaaulia.footballclips.data.model.remote

import com.dirzaaulia.footballclips.util.normalizeLeagueName
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class HighlightResponse(
    val status: String? = null,
    val totalVerified: Int? = null,
    val pagination: PaginationResponse? = null,
    val data: List<HighlightItemResponse>? = null
)

@Serializable
data class PaginationResponse(
    val totalCount: Int? = null,
    val offset: Int? = null,
    val limit: Int? = null
)

@Serializable
data class HighlightItemResponse(
    val id: Int? = null,
    val type: String? = null,
    val imgUrl: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val embedUrl: String? = null,
    val channel: String? = null,
    val source: String? = null,
    val category: String? = null,
    val match: MatchDetailsResponse? = null
)

@Serializable
data class MatchDetailsResponse(
    val id: Long? = null,
    val round: String? = null,
    val date: String? = null,
    val country: CountryResponse? = null,
    val awayTeam: TeamResponse? = null,
    val homeTeam: TeamResponse? = null,
    val league: LeagueResponse? = null
)

@Serializable
data class CountryResponse(
    val code: String? = null,
    val name: String? = null,
    val logo: String? = null
)

@Serializable
data class TeamResponse(
    val id: Long? = null,
    val logo: String? = null,
    val name: String? = null
)

@Serializable
data class LeagueResponse(
    val id: Long? = null,
    val logo: String? = null,
    val name: String? = null,
    val season: Int? = null
)

data class HighlightUiModel(
    val id: Int,
    val title: String,
    val homeTeam: String,
    val homeTeamLogo: String,
    val awayTeam: String,
    val awayTeamLogo: String,
    val leagueName: String,
    val leagueLogo: String,
    val countryName: String,
    val countryLogo: String,
    val date: String,
    val thumbnail: String,
    val embedHtml: String,
    val videoUrl: String,
    val source: String
)

fun HighlightItemResponse.toUiModel(): HighlightUiModel? {
    val highlightId = id ?: return null
    val matchDetails = match ?: return null
    
    // Parse ISO date and format it for the UI (e.g., "23 Aug, 15:00")
    val formattedDate = try {
        val dateString = matchDetails.date ?: ""
        val instant = Instant.parse(dateString)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val dateTimeFormat = LocalDateTime.Format {
            dayOfMonth()
            char(' ')
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(',')
            char(' ')
            hour()
            char(':')
            minute()
        }
        dateTimeFormat.format(localDateTime)
    } catch (_: Exception) {
        matchDetails.date ?: ""
    }

    return HighlightUiModel(
        id = highlightId,
        title = title ?: "",
        homeTeam = matchDetails.homeTeam?.name ?: "Unknown",
        homeTeamLogo = matchDetails.homeTeam?.logo ?: "",
        awayTeam = matchDetails.awayTeam?.name ?: "Unknown",
        awayTeamLogo = matchDetails.awayTeam?.logo ?: "",
        leagueName = (matchDetails.league?.name ?: "Unknown").normalizeLeagueName(),
        leagueLogo = matchDetails.league?.logo ?: "",
        countryName = matchDetails.country?.name ?: "Unknown",
        countryLogo = matchDetails.country?.logo ?: "",
        date = formattedDate,
        thumbnail = imgUrl ?: "",
        embedHtml = embedUrl ?: "",
        videoUrl = url ?: "",
        source = source ?: ""
    )
}
