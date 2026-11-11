package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.domain.model.MatchEvent
import com.dirzaaulia.footballclips.domain.model.PitchPlay
import com.dirzaaulia.footballclips.ui.score.companion.PitchRadarDeck
import com.dirzaaulia.footballclips.util.VoiceCommentaryManager
import com.dirzaaulia.footballclips.util.toLegibleColor
import org.koin.compose.koinInject

@Composable
internal fun MatchRadarTab(
    match: Match,
    isLive: Boolean,
    modifier: Modifier = Modifier,
) {
    val voiceCommentaryManager: VoiceCommentaryManager = koinInject()
    var isVoiceEnabled by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            voiceCommentaryManager.stop()
        }
    }

    val plays = remember(match.events, match.status) {
        buildPitchPlays(match, isLive)
    }

    LaunchedEffect(plays.size, isVoiceEnabled) {
        if (isVoiceEnabled && isLive && plays.isNotEmpty()) {
            val latest = plays.last()
            voiceCommentaryManager.speakPlay(latest.clock, latest.text, isGoal = latest.isGoal)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        VoiceCommentaryControlCard(
            isVoiceEnabled = isVoiceEnabled,
            isLive = isLive,
            onToggle = { enabled ->
                isVoiceEnabled = enabled
                voiceCommentaryManager.isEnabled = enabled
                if (!enabled) voiceCommentaryManager.stop()
            },
        )

        val homeColor = match.homeColor.toLegibleColor(Color(0xFF3B82F6))
        val awayColor = match.awayColor.toLegibleColor(Color(0xFFEC4899))

        PitchRadarDeck(
            plays = plays,
            homeColor = homeColor,
            awayColor = awayColor,
            modifier = Modifier.fillMaxWidth(),
            onPlaySelected = { play ->
                if (isVoiceEnabled) {
                    voiceCommentaryManager.speakPlay(play.clock, play.text, isGoal = play.isGoal)
                }
            },
        )
    }
}

@Composable
private fun VoiceCommentaryControlCard(
    isVoiceEnabled: Boolean,
    isLive: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = if (isVoiceEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = null,
                    tint = if (isVoiceEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
                Column {
                    Text(
                        text = "Voice Commentary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (isLive) "Real-time AI audio commentary" else "Interactive replay commentary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Switch(
                checked = isVoiceEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier.height(28.dp),
            )
        }
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
