package com.dirzaaulia.footballclips.ui.score

import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import com.dirzaaulia.footballclips.domain.model.Match

internal fun deduplicateMatches(matches: List<Match>): List<Match> {
    if (matches.size <= 1) return matches

    val result = mutableListOf<Match>()
    for (match in matches) {
        val existingIndex = result.indexOfFirst { areMatchesDuplicate(it, match) }
        if (existingIndex < 0) {
            result.add(match)
        } else {
            val existing = result[existingIndex]
            val existingScore = scoreMatchQuality(existing)
            val newScore = scoreMatchQuality(match)
            if (newScore > existingScore) {
                result[existingIndex] = match
            }
        }
    }
    return result
}

private fun areMatchesDuplicate(m1: Match, m2: Match): Boolean {
    val h1 = normalizeTeamNameForDeduplication(m1.homeTeamName)
    val h2 = normalizeTeamNameForDeduplication(m2.homeTeamName)
    val a1 = normalizeTeamNameForDeduplication(m1.awayTeamName)
    val a2 = normalizeTeamNameForDeduplication(m2.awayTeamName)

    val homeMatches = h1 == h2 || h1.contains(h2) || h2.contains(h1)
    val awayMatches = a1 == a2 || a1.contains(a2) || a2.contains(a1)
    if (!homeMatches || !awayMatches) return false

    val d1 = m1.dateOnlyISO.ifEmpty { m1.utcDate.take(10) }
    val d2 = m2.dateOnlyISO.ifEmpty { m2.utcDate.take(10) }
    return d1 == d2 || m1.isLive || m2.isLive
}

private fun scoreMatchQuality(match: Match): Int {
    var score = 0
    if (match.isLive) {
        score += 1000
    } else if (match.isFinished) {
        score += 500
    } else if (match.isScheduled) {
        score += 100
    }

    if (match.id > 100_000_000L) score += 100
    if (!match.highlightVideoId.isNullOrBlank()) score += 50
    if (match.homeScore != null && match.awayScore != null) score += 25
    if (match.formations != null) score += 10
    if (match.statistics != null) score += 10
    return score
}

internal fun normalizeTeamNameForDeduplication(name: String): String {
    return name.lowercase()
        .replace("fc", "")
        .replace("bc", "")
        .replace("cf", "")
        .replace("afc", "")
        .replace("sc", "")
        .replace("us", "")
        .replace("ac", "")
        .replace("1.", "")
        .replace("balompié", "")
        .replace("balompie", "")
        .replace("calcio", "")
        .replace("de barcelona", "")
        .replace("münchen", "munich")
        .replace("manchester", "man")
        .replace("tottenham hotspur", "spurs")
        .replace("tottenham", "spurs")
        .replace("wolverhampton wanderers", "wolves")
        .replace("wolverhampton", "wolves")
        .replace("saint-germain", "sg")
        .replace("saint germain", "sg")
        .replace("atletico de madrid", "atletico")
        .replace("atlético de madrid", "atletico")
        .replace("atletico madrid", "atletico")
        .replace("atlético madrid", "atletico")
        .replace("athletic club", "bilbao")
        .replace("athletic bilbao", "bilbao")
        .replace("internazionale", "inter")
        .replace("inter milan", "inter")
        .filter { it.isLetterOrDigit() }
        .trim()
}

internal fun deduplicateStandings(standings: List<StandingDto>): List<StandingDto> {
    if (standings.size <= 1) return standings

    val byTeam = standings
        .groupBy { normalizeTeamNameForDeduplication(it.teamName) }
        .values
        .map { duplicates ->
            if (duplicates.size == 1) {
                duplicates.first()
            } else {
                duplicates.maxByOrNull { item ->
                    var score = 0
                    if (item.playedGames > 0) score += 50
                    if (!item.teamCrest.isNullOrBlank()) score += 20
                    if (item.points > 0) score += 10
                    score
                } ?: duplicates.first()
            }
        }

    return byTeam
        .groupBy { it.position }
        .values
        .map { duplicates ->
            if (duplicates.size == 1) {
                duplicates.first()
            } else {
                duplicates.maxByOrNull { item ->
                    var score = 0
                    if (item.playedGames > 0) score += 50
                    if (!item.teamCrest.isNullOrBlank()) score += 20
                    score
                } ?: duplicates.first()
            }
        }
        .sortedBy { it.position }
}
