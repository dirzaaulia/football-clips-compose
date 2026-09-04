package com.dirzaaulia.footballclips.domain.model

import com.dirzaaulia.footballclips.data.model.remote.MatchDto
import com.dirzaaulia.footballclips.util.DateTimeUtils
import com.dirzaaulia.footballclips.util.normalizeLeagueName
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    val highlightVideoId: String?
) {
    val leagueEmblemUrl: String
        get() = "https://crests.football-data.org/${competitionId.uppercase()}.png"

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
            val day = it.dayOfMonth.toString().padStart(2, '0')
            val month = it.month.name.take(3).lowercase().replaceFirstChar { c -> c.uppercase() }
            val hour = it.hour.toString().padStart(2, '0')
            val minute = it.minute.toString().padStart(2, '0')
            "$day $month, $hour:$minute"
        } ?: utcDate

    val dateOnly: String
        get() = localDateTime?.let {
            "${it.dayOfMonth} ${it.month.name.take(3)}"
        } ?: ""

    val dateOnlyISO: String
        get() = localDateTime?.let {
            "${it.year}-${it.monthNumber.toString().padStart(2, '0')}-${it.dayOfMonth.toString().padStart(2, '0')}"
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
}

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
        highlightVideoId = highlightVideoId
    )
}
