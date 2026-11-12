package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.MatchEvent
import com.dirzaaulia.footballclips.domain.model.PitchPlay
import com.dirzaaulia.footballclips.ui.score.companion.PitchRadarDeck
import com.dirzaaulia.footballclips.util.toLegibleColor

@Composable
internal fun MatchRadarTab(
    match: Match,
    isLive: Boolean,
    modifier: Modifier = Modifier,
) {
    val plays = remember(match.events, match.status) {
        buildPitchPlays(match = match, isLive = isLive)
    }

    val homeColor = match.homeColor.toLegibleColor(Color(0xFF7098F8))
    val awayColor = match.awayColor.toLegibleColor(Color(0xFF4FC3F7))

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PitchRadarDeck(
            plays = plays,
            homeColor = homeColor,
            awayColor = awayColor,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun buildPitchPlays(match: Match, isLive: Boolean): List<PitchPlay> {
    val events = match.events
    if (!events.isNullOrEmpty()) {
        return events.map { it.toPitchPlay(match.homeTeamName, match.awayTeamName) }
    }
    return if (isLive) {
        listOf(
            PitchPlay(
                id = "live_status",
                clock = match.displayClock ?: "LIVE",
                type = "match_in_play",
                typeText = "Match in Play",
                text = "${match.homeTeamName} vs ${match.awayTeamName} is underway!",
                xPercent = 50f,
                yPercent = 50f,
            ),
        )
    } else {
        emptyList()
    }
}

private fun MatchEvent.toPitchPlay(homeTeamName: String, awayTeamName: String): PitchPlay {
    val isHome = side.equals("home", ignoreCase = true)
    val team = if (isHome) homeTeamName else if (side.equals("away", ignoreCase = true)) awayTeamName else null
    val isGoalPlay = scoringPlay || type?.contains("goal", ignoreCase = true) == true
    val isCornerPlay = type?.contains("corner", ignoreCase = true) == true
    val isShotPlay = type?.contains("shot", ignoreCase = true) == true

    val xPos = xCoord ?: when {
        isGoalPlay -> if (isHome) 88f else 12f
        isShotPlay -> if (isHome) 78f else 22f
        isCornerPlay -> if (isHome) 98f else 2f
        else -> 50f
    }
    val yPos = yCoord ?: if (isCornerPlay) 6f else 50f

    return PitchPlay(
        id = id ?: "${clock}_${type}_${player}",
        clock = clock ?: "0'",
        period = period ?: 1,
        type = type ?: "event",
        typeText = typeText ?: (type ?: "Event"),
        text = text.ifBlank { "${player} - ${typeText ?: type}" },
        teamName = team,
        player = player,
        xPercent = xPos,
        yPercent = yPos,
        targetXPercent = targetXCoord ?: if (isShotPlay) (if (isHome) 100f else 0f) else null,
        targetYPercent = targetYCoord ?: if (isShotPlay) 50f else null,
    )
}
