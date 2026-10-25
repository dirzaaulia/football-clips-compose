package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.components.common.shimmerEffect
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun HeroLiveMatchCard(
    match: Match,
    isFavorite: Boolean,
    onWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onWatchClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2227)),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SubcomposeAsyncImage(
                        model = match.leagueEmblemUrl.toProxyUrl(),
                        contentDescription = match.competitionName,
                        loading = { Box(modifier = Modifier.fillMaxSize().shimmerEffect()) },
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = match.competitionName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "KO ${match.kickoffTime}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    ) {
                        Text(
                            text = "LIVE ${match.displayClock ?: "In-Play"}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SubcomposeAsyncImage(
                        model = match.homeTeamCrest?.toProxyUrl(),
                        contentDescription = match.homeTeamName,
                        loading = { Box(modifier = Modifier.fillMaxSize().shimmerEffect()) },
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.homeTeamName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Text(
                    text = "${match.homeScore ?: 0} - ${match.awayScore ?: 0}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF6B6B),
                    letterSpacing = 2.sp,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SubcomposeAsyncImage(
                        model = match.awayTeamCrest?.toProxyUrl(),
                        contentDescription = match.awayTeamName,
                        loading = { Box(modifier = Modifier.fillMaxSize().shimmerEffect()) },
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.awayTeamName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            if (!match.highlightVideoId.isNullOrEmpty()) {
                Surface(
                    onClick = onWatchClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF7098F8).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF7098F8).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF7098F8),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Watch Live Highlights",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7098F8),
                        )
                    }
                }
            }
        }
    }
}
