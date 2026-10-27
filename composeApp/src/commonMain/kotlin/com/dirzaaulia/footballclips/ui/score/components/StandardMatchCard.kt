package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.Match

@Composable
fun StandardMatchCard(
    match: Match,
    modifier: Modifier = Modifier,
    showStatus: Boolean = true,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
    isFavorite: Boolean = false,
    showTeamName: Boolean = true,
    onRevealClick: (() -> Unit)? = null,
    onWatchHighlightClick: ((String) -> Unit)? = null
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isLandscape = maxWidth < 300.dp || maxHeight < 300.dp
        val shouldShowName = showTeamName && !isLandscape

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!match.highlightVideoId.isNullOrEmpty() && onWatchHighlightClick != null) {
                        Modifier.clickable { onWatchHighlightClick(match.highlightVideoId) }
                    } else Modifier
                ),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = if (isFavorite) 1.5.dp else 1.dp,
                color = if (isFavorite) Color(0xFFFFB300).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            tonalElevation = 0.dp,
            shadowElevation = 2.dp
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LeagueHeader(
                        competitionName = match.competitionName,
                        competitionId = match.competitionId,
                        isDark = false
                    )
                    if (isFavorite) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                if (showStatus) {
                    MatchStatusBadge(match = match)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TeamItem(
                    name = match.homeTeamName,
                    crest = match.homeTeamCrest,
                    showName = shouldShowName,
                    modifier = Modifier.weight(1f)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (match.isLive || match.isFinished) {
                        // Live matches never hide score; spoiler shielding is strictly for finished matches
                        if (isSpoilerFree && !isRevealed && !match.isLive) {
                            Text(
                                text = "? - ?",
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .then(if (onRevealClick != null) Modifier.clickable { onRevealClick() } else Modifier),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 2.sp
                            )
                        } else {
                            Text(
                                text = "${match.homeScore ?: 0} - ${match.awayScore ?: 0}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (match.isLive) Color.Red else MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 2.sp
                            )
                            if (match.isLive) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "KO ${match.kickoffTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "VS",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = match.formattedLocalKickoff,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                TeamItem(
                    name = match.awayTeamName,
                    crest = match.awayTeamCrest,
                    showName = shouldShowName,
                    modifier = Modifier.weight(1f)
                )
            }

            StandardMatchBottomAction(match = match, onWatchHighlightClick = onWatchHighlightClick)
        }
    }
}
}

@Composable
private fun StandardMatchBottomAction(
    match: Match,
    onWatchHighlightClick: ((String) -> Unit)?,
) {
    val availability = match.viewerAvailability
    val isPlayable = !match.highlightVideoId.isNullOrEmpty()
    val isUnverified = false

    if (isPlayable && onWatchHighlightClick != null) {
        Spacer(modifier = Modifier.height(14.dp))
        FilledTonalButton(
            onClick = { onWatchHighlightClick(match.highlightVideoId.orEmpty()) },
            modifier = Modifier.fillMaxWidth().height(38.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Watch Highlight",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
            )
        }
    } else if (isUnverified && onWatchHighlightClick != null) {
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(
            onClick = { onWatchHighlightClick(match.externalVideoId.orEmpty()) },
            modifier = Modifier.fillMaxWidth().height(38.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        ) {
            Text(
                text = "Availability not verified · YouTube ↗",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    } else if (availability != null && availability != "available" && match.isFinished) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            val label = when (availability) {
                "country_blocked" -> "Unavailable in your country"
                "not_found" -> "Highlight not found"
                "unavailable" -> "Video unavailable"
                else -> "Availability not verified"
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium,
            )
        }
    } else {
        Spacer(modifier = Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = match.dateOnly,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
