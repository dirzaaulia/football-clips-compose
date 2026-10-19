package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.player.YouTubePlayerView

@Composable
fun FloatingMiniPlayer(
    item: HighlightUiItem?,
    videoId: String,
    isSpoilerFree: Boolean,
    onRestore: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isScoreRevealed by remember(item) { mutableStateOf(false) }
    val isHidden = isSpoilerFree && !isScoreRevealed

    val homeName: String
    val awayName: String
    val compName: String
    val homeScore: Int?
    val awayScore: Int?

    when (item) {
        is HighlightUiItem.SupabaseMatch -> {
            homeName = item.match.homeTeamName
            awayName = item.match.awayTeamName
            compName = item.match.competitionName
            homeScore = item.match.homeScore
            awayScore = item.match.awayScore
        }
        is HighlightUiItem.Highlight -> {
            homeName = item.highlight.homeTeam
            awayName = item.highlight.awayTeam
            compName = item.highlight.leagueName
            homeScore = null
            awayScore = null
        }
        else -> {
            homeName = "Match"
            awayName = "Highlight"
            compName = "Football Clips"
            homeScore = null
            awayScore = null
        }
    }

    val hasScores = homeScore != null && awayScore != null

    Card(
        modifier = modifier
            .width(360.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101418)),
        border = BorderStroke(1.dp, Color(0xFF7098F8).copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color(0xFF7098F8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "$homeName vs $awayName",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRestore,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Expand Player",
                            tint = Color(0xFF7098F8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Player",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // 16:9 Video Player View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                val backupVideoId = when (item) {
                    is HighlightUiItem.SupabaseMatch -> item.match.backupHighlightVideoId
                    else -> null
                }
                val regionInfo = when (item) {
                    is HighlightUiItem.SupabaseMatch -> item.match.youtubeRegionInfo
                    else -> null
                }
                val backupRegionInfo = when (item) {
                    is HighlightUiItem.SupabaseMatch -> item.match.backupYoutubeRegionInfo
                    else -> null
                }
                YouTubePlayerView(
                    videoId = videoId,
                    backupVideoId = backupVideoId,
                    regionInfo = regionInfo,
                    backupRegionInfo = backupRegionInfo,
                    onDismiss = { onClose() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Footer info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = compName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7098F8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (hasScores) {
                    Spacer(Modifier.width(8.dp))
                    if (isHidden) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF181C20),
                            border = BorderStroke(1.dp, Color(0xFF7098F8).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { isScoreRevealed = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Reveal Score",
                                    tint = Color(0xFF4FC3F7),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "Score",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4FC3F7)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF181C20),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.clickable(enabled = isSpoilerFree) { isScoreRevealed = false }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$homeScore - $awayScore",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                if (isSpoilerFree) {
                                    Spacer(Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = "Hide Score",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
