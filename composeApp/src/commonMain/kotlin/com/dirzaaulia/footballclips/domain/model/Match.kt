package com.dirzaaulia.footballclips.domain.model

import com.dirzaaulia.footballclips.data.model.remote.MatchDto
import com.dirzaaulia.footballclips.util.DateTimeUtils
import com.dirzaaulia.footballclips.util.normalizeLeagueName
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

data class Match(
    val id: Long,
    val competitionId: String,
    val competitionName: String,
    val utcDate: String,
    val status: String,
    val matchday: Int?,
    val homeTeamName: String,
    val homeTeamCrest: String?,
    val awayTeamName: String,
    val awayTeamCrest: String?,
    val homeScore: Int?,
    val awayScore: Int?,
    val highlightVideoId: String?,
    val backupHighlightVideoId: String? = null,
    val recapAnalysis: String? = null,
    val roundName: String? = null,
    val displayClock: String? = null,
    val season: Int = 2026,
    val formations: Formations? = null,
    val statistics: Map<String, Map<String, String>>? = null,
    val editorialHeadline: String? = null,
    val editorialStory: String? = null,
    val editorialSource: String? = null,
    val events: List<MatchEvent>? = null,
    val youtubeRegionInfo: RegionRestriction? = null,
    val backupYoutubeRegionInfo: RegionRestriction? = null,
    val playbackMode: String? = null,
    val viewerAvailability: String? = null,
    val externalVideoId: String? = null,
) {
    val displayRound: String
        get() = roundName ?: matchday?.let { "Matchweek $it" } ?: competitionName

    val leagueEmblemUrl: String
        get() = when (competitionId.uppercase()) {
            "CL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2.png"
            "EL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2487.png"
            "ECL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/20296.png"
            "PL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/23.png"
            "PD" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/15.png"
            "SA" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/12.png"
            "BL1" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/10.png"
            "FL1" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/9.png"
            "IDN" -> "https://fc.dirzaaulia.com/logos/competitions/idn-2026.png"
            "SPL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2488.png"
            "MLS" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/19.png"
            "DED" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/11.png"
            "PPL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/14.png"
            "FAC" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/40.png"
            "EFL" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/41.png"
            "CDR" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/80.png"
            "DFB" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2061.png"
            "CIT" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/270.png"
            "LIB" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/58.png"
            "SUD" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/1208.png"
            "REC" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/2335.png"
            "BRA" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/85.png"
            "ARG" -> "https://a.espncdn.com/i/leaguelogos/soccer/500/1.png"
            else -> "https://a.espncdn.com/i/leaguelogos/soccer/500/23.png"
        }

    private val localDateTime by lazy {
        try {
            val instant = Instant.parse(utcDate)
            val timeZone = DateTimeUtils.safeTimeZone
            try {
                instant.toLocalDateTime(timeZone)
            } catch (t: Throwable) {
                // If the safe time zone still causes issues with toLocalDateTime, fall back to UTC
                instant.toLocalDateTime(TimeZone.UTC)
            }
        } catch (t: Throwable) {
            null
        }
    }

    val formattedLocalKickoff: String
        get() = localDateTime?.let {
            val day = it.day.toString().padStart(2, '0')
            val month = it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
            val hour = it.hour.toString().padStart(2, '0')
            val minute = it.minute.toString().padStart(2, '0')
            "$day $month ${it.year}, $hour:$minute"
        } ?: utcDate

    val dateOnly: String
        get() = localDateTime?.let {
            val month = it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
            "${it.day} $month ${it.year}"
        } ?: ""

    val dateOnlyISO: String
        get() = localDateTime?.let {
            "${it.year}-${(it.month.ordinal + 1).toString().padStart(2, '0')}-${it.day.toString().padStart(2, '0')}"
        } ?: ""

    val kickoffTime: String
        get() = localDateTime?.let {
            val hour = it.hour.toString().padStart(2, '0')
            val minute = it.minute.toString().padStart(2, '0')
            "$hour:$minute"
        } ?: ""

    val isLive: Boolean
        get() {
            val normalizedStatus = status.uppercase().replace(" ", "_")
            return normalizedStatus in listOf("IN_PLAY", "PAUSED", "LIVE")
        }

    val isFinished: Boolean
        get() {
            val normalizedStatus = status.uppercase().replace(" ", "_")
            return normalizedStatus == "FINISHED"
        }

    val isScheduled: Boolean
        get() {
            val normalizedStatus = status.uppercase().replace(" ", "_")
            return normalizedStatus in listOf("TIMED", "SCHEDULED")
        }

    val hasHighlight: Boolean
        get() = !highlightVideoId.isNullOrBlank()
}

data class StarterPlayer(
    val name: String,
    val jersey: String = "",
    val position: String = ""
)

data class TeamFormation(
    val formation: String? = null,
    val starters: List<StarterPlayer> = emptyList()
)

data class Formations(
    val home: TeamFormation? = null,
    val away: TeamFormation? = null
)

fun com.dirzaaulia.footballclips.data.model.remote.StarterPlayerDto.toDomain() = StarterPlayer(
    name = name,
    jersey = jersey,
    position = position
)

fun com.dirzaaulia.footballclips.data.model.remote.TeamFormationDto.toDomain() = TeamFormation(
    formation = formation,
    starters = starters.map { it.toDomain() }
)

fun com.dirzaaulia.footballclips.data.model.remote.FormationsDto.toDomain() = Formations(
    home = home?.toDomain(),
    away = away?.toDomain()
)

data class MatchEvent(
    val id: String?,
    val type: String?,
    val typeText: String?,
    val clock: String?,
    val period: Int?,
    val side: String?,
    val scoringPlay: Boolean,
    val player: String,
    val assist: String?,
    val text: String
)

fun com.dirzaaulia.footballclips.data.model.remote.MatchEventDto.toDomain() = MatchEvent(
    id = id,
    type = type,
    typeText = typeText,
    clock = clock,
    period = period,
    side = side,
    scoringPlay = scoringPlay,
    player = player,
    assist = assist,
    text = text
)

fun MatchDto.toMatch(): Match {
    return Match(
        id = id,
        competitionId = competitionId,
        competitionName = competitionName.normalizeLeagueName(),
        utcDate = utcDate,
        status = status,
        matchday = matchday,
        homeTeamName = homeTeamName,
        homeTeamCrest = homeTeamCrest,
        awayTeamName = awayTeamName,
        awayTeamCrest = awayTeamCrest,
        homeScore = homeScore,
        awayScore = awayScore,
        highlightVideoId = highlightVideoId,
        playbackMode = playbackMode,
        backupHighlightVideoId = backupHighlightVideoId,
        recapAnalysis = recapAnalysis,
        roundName = roundName,
        displayClock = displayClock,
        season = season,
        formations = formations?.toDomain(),
        statistics = statistics,
        editorialHeadline = editorialHeadline,
        editorialStory = editorialStory,
        editorialSource = editorialSource,
        events = events?.map { it.toDomain() },
        youtubeRegionInfo = youtubeRegionInfo?.let { RegionRestriction(it.allowed, it.blocked) },
        backupYoutubeRegionInfo = backupYoutubeRegionInfo?.let { RegionRestriction(it.allowed, it.blocked) },
        viewerAvailability = viewerAvailability,
        externalVideoId = externalVideoId,
    )
}

data class RegionRestriction(
    val allowed: List<String>? = null,
    val blocked: List<String>? = null
) {
    fun isSupportedIn(countryCode: String?): Boolean {
        if (countryCode.isNullOrBlank()) return true
        val upper = countryCode.uppercase().trim()
        val all = allowed
        if (!all.isNullOrEmpty()) {
            return all.any { it.equals(upper, ignoreCase = true) }
        }
        val blk = blocked
        if (!blk.isNullOrEmpty()) {
            return !blk.any { it.equals(upper, ignoreCase = true) }
        }
        return true
    }
}

