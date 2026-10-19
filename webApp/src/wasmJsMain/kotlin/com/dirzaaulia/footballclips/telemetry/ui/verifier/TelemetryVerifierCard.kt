package com.dirzaaulia.footballclips.telemetry.ui.verifier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.DatabaseMatchRecord
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary

@Composable
fun HighlightVerificationCard(
    match: DatabaseMatchRecord,
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasVideo = !match.highlightVideoId.isNullOrBlank()
    val videoId = match.highlightVideoId ?: ""

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StitchSurface,
        border = BorderStroke(
            1.dp,
            if (hasVideo) StitchSuccess.copy(alpha = 0.35f) else StitchBorder,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, if (hasVideo) StitchSuccess.copy(alpha = 0.4f) else StitchBorder, RoundedCornerShape(6.dp))
                    .clickable { if (hasVideo) onOpenVideo("https://www.youtube.com/watch?v=$videoId") },
                contentAlignment = Alignment.Center,
            ) {
                if (hasVideo) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Text("YOUTUBE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = FontFamily.Monospace)
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(Icons.Default.MovieFilter, contentDescription = null, tint = StitchTextMuted, modifier = Modifier.size(18.dp))
                        Text("No Video", fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = StitchPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, StitchPrimary.copy(alpha = 0.35f)),
                    ) {
                        Text(
                            text = (match.competitionId ?: match.competitionName).uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
                    Text(
                        text = "${match.utcDate.replace("T", " ").take(16)} UTC",
                        fontSize = 10.sp,
                        color = StitchTextMuted,
                        fontFamily = FontFamily.Monospace,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = match.homeTeamName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = StitchTextPrimary,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        text = if (match.homeScore != null && match.awayScore != null) "${match.homeScore} - ${match.awayScore}" else "VS",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (hasVideo) StitchSuccess else StitchTextMuted,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        text = match.awayTeamName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = StitchTextPrimary,
                        fontFamily = FontFamily.Monospace,
                    )
                }

                if (hasVideo) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "YT_ID: $videoId",
                            fontSize = 10.sp,
                            color = StitchSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(StitchTextMuted))
                        Text(
                            text = "Provider: ${(match.provider ?: "ESPN").uppercase()}",
                            fontSize = 10.sp,
                            color = StitchTextMuted,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                } else {
                    Text(
                        text = "Status: ${match.status} | Scraping queue active",
                        fontSize = 10.sp,
                        color = StitchTextMuted,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            if (hasVideo) {
                Surface(
                    onClick = { onOpenVideo("https://www.youtube.com/watch?v=$videoId") },
                    shape = RoundedCornerShape(6.dp),
                    color = StitchSuccess.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, StitchSuccess.copy(alpha = 0.5f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("VERIFY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchSuccess, fontFamily = FontFamily.Monospace)
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
