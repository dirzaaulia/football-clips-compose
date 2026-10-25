package com.dirzaaulia.footballclips.data.model.remote

import com.dirzaaulia.footballclips.util.normalizeLeagueName
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

private fun extractTeamsFromTitle(itemTitle: String?): Pair<String, String> {
    if (itemTitle.isNullOrBlank()) return Pair("Official", "Highlight")
    val clean = itemTitle.replace(Regex("^[^:]+:\\s*"), "").replace(Regex("\\s*Highlights.*$", RegexOption.IGNORE_CASE), "")
    val parts = clean.split(Regex("\\s+vs\\.?\\s+", RegexOption.IGNORE_CASE))
    if (parts.size >= 2) {
        return Pair(parts[0].trim(), parts[1].trim())
    }
    val hyphenParts = clean.split(Regex("\\s+-\\s+"))
    if (hyphenParts.size >= 2) {
        return Pair(hyphenParts[0].trim(), hyphenParts[1].trim())
    }
    return Pair(clean.trim(), "")
}

fun HighlightItemResponse.toUiModel(): HighlightUiModel? {
    val highlightId = id ?: return null
    val matchDetails = match

    val fallbackTeams = extractTeamsFromTitle(title)
    val homeName = matchDetails?.homeTeam?.name ?: fallbackTeams.first
    val awayName = matchDetails?.awayTeam?.name ?: fallbackTeams.second
    val rawLeague = matchDetails?.league?.name ?: channel ?: "Official Highlight"

    val formattedDate = try {
        val dateString = matchDetails?.date ?: ""
        if (dateString.isNotEmpty()) {
            val instant = kotlin.time.Instant.parse(dateString)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val dateTimeFormat = LocalDateTime.Format {
                day()
                char(' ')
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                char(',')
                char(' ')
                hour()
                char(':')
                minute()
            }
            dateTimeFormat.format(localDateTime)
        } else ""
    } catch (_: Exception) {
        matchDetails?.date ?: ""
    }

    return HighlightUiModel(
        id = highlightId,
        title = title ?: "",
        homeTeam = homeName,
        homeTeamLogo = matchDetails?.homeTeam?.logo ?: "",
        awayTeam = awayName,
        awayTeamLogo = matchDetails?.awayTeam?.logo ?: "",
        leagueName = rawLeague.normalizeLeagueName(),
        leagueLogo = matchDetails?.league?.logo ?: "",
        countryName = matchDetails?.country?.name ?: "International",
        countryLogo = matchDetails?.country?.logo ?: "",
        date = formattedDate,
        thumbnail = imgUrl ?: "",
        embedHtml = embedUrl ?: "",
        videoUrl = url ?: "",
        source = source ?: ""
    )
}
