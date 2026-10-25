package com.dirzaaulia.footballclips.data.model.remote

import com.dirzaaulia.footballclips.domain.model.Match
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupportedClub(
    val id: String = "",
    @SerialName("team_id") val teamId: Int? = null,
    val name: String = "",
    @SerialName("short_name") val shortName: String? = null,
    @SerialName("competition_code") val competitionCode: String? = null,
    @SerialName("competition_name") val competitionName: String? = null,
    @SerialName("crest_url") val crestUrl: String? = null,
    val aliases: List<String> = emptyList(),
) {
    // Adapter properties to match previous usage
    val leagueCode: String
        get() = competitionCode ?: ""

    val leagueName: String
        get() = competitionName ?: ""
}

// Extension to dynamically check if a match involves any of the user's favorite clubs
fun Match.isMatchForFavoriteClubs(favoriteClubIds: Set<String>, supportedClubs: List<SupportedClub>): Boolean {
    if (favoriteClubIds.isEmpty() || supportedClubs.isEmpty()) return false
    val matchHome = homeTeamName.lowercase()
    val matchAway = awayTeamName.lowercase()

    val favorites = supportedClubs.filter { it.id in favoriteClubIds }
    return favorites.any { club ->
        val clubName = club.name.lowercase()
        val clubShortName = club.shortName?.lowercase() ?: ""

        val matchesHome = matchHome.contains(clubName) || (clubShortName.isNotEmpty() && matchHome.contains(clubShortName)) || club.aliases.any { matchHome.contains(it.lowercase()) }
        val matchesAway = matchAway.contains(clubName) || (clubShortName.isNotEmpty() && matchAway.contains(clubShortName)) || club.aliases.any { matchAway.contains(it.lowercase()) }

        matchesHome || matchesAway
    }
}
