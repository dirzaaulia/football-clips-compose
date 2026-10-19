package com.dirzaaulia.footballclips.telemetry.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.DatabaseMatchRecord
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainerHigh
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography

@Composable
fun MatchCatalogTableRow(
    match: DatabaseMatchRecord,
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFinished = match.status.equals("FINISHED", ignoreCase = true)
    val isLive = match.status in listOf("IN_PLAY", "PAUSED", "LIVE")
    val hasHighlight = !match.highlightVideoId.isNullOrBlank()

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isLive) StitchError.copy(alpha = 0.05f) else StitchSurface,
        border = BorderStroke(
            1.dp,
            if (isLive) StitchError.copy(alpha = 0.4f) else StitchBorder,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = match.utcDate.replace("T", " ").take(16),
                style = TelemetryTypography.codeStream.copy(color = StitchTextMuted, fontSize = 11.sp),
                modifier = Modifier.width(105.dp),
            )

            Surface(
                shape = RoundedCornerShape(3.dp),
                color = StitchPrimary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, StitchPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.width(42.dp),
            ) {
                Text(
                    text = (match.competitionId ?: match.competitionName).take(4).uppercase(),
                    style = TelemetryTypography.labelSmall.copy(
                        color = StitchPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = match.homeTeamName,
                    style = TelemetryTypography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = StitchTextPrimary,
                        textAlign = TextAlign.End,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isFinished || isLive) StitchSurfaceContainerHigh else StitchBorder.copy(alpha = 0.3f),
                    border = BorderStroke(
                        1.dp,
                        if (isLive) StitchError.copy(alpha = 0.5f) else StitchBorder,
                    ),
                ) {
                    val scoreText = if (match.homeScore != null && match.awayScore != null) {
                        "${match.homeScore} - ${match.awayScore}"
                    } else {
                        "VS"
                    }
                    Text(
                        text = scoreText,
                        style = TelemetryTypography.codeStream.copy(
                            color = if (isLive) StitchError else if (isFinished) StitchTextPrimary else StitchTextMuted,
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }

                Text(
                    text = match.awayTeamName,
                    style = TelemetryTypography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = StitchTextPrimary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            val statusColor = when {
                isLive -> StitchError
                isFinished -> StitchSuccess
                else -> StitchTextMuted
            }
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = statusColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
                modifier = Modifier.width(90.dp),
            ) {
                Text(
                    text = if (isLive) "LIVE ${match.displayClock ?: ""}".trim() else match.status,
                    style = TelemetryTypography.labelSmall.copy(
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }

            if (hasHighlight) {
                Surface(
                    onClick = { onOpenVideo("https://www.youtube.com/watch?v=${match.highlightVideoId}") },
                    shape = RoundedCornerShape(3.dp),
                    color = StitchSuccess.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, StitchSuccess.copy(alpha = 0.4f)),
                    modifier = Modifier.width(140.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = StitchSuccess, modifier = Modifier.size(13.dp))
                        Text(
                            text = match.highlightVideoId,
                            style = TelemetryTypography.codeStream.copy(color = StitchSuccess, fontSize = 10.5.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = StitchSuccess, modifier = Modifier.size(11.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier.width(140.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isFinished) "Pending Scraper" else "Upcoming",
                        style = TelemetryTypography.labelSmall.copy(color = StitchTextMuted),
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(3.dp),
                color = StitchSurfaceContainer,
                border = BorderStroke(1.dp, StitchBorder),
                modifier = Modifier.width(48.dp),
            ) {
                Text(
                    text = (match.provider ?: "ESPN").take(4).uppercase(),
                    style = TelemetryTypography.labelSmall.copy(
                        color = StitchSecondary,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}
