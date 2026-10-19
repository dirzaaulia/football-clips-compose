package com.dirzaaulia.footballclips.telemetry.ui.pipelines

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
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
import com.dirzaaulia.footballclips.telemetry.model.PipelineStageStep
import com.dirzaaulia.footballclips.telemetry.model.PipelineStepStage
import com.dirzaaulia.footballclips.telemetry.model.RetentionPurgeSummary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchCyan
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPurple
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainerHigh
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning

@Composable
fun RetentionMonitorCard(
    summary: RetentionPurgeSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSuccess.copy(alpha = 0.15f))
                    .border(1.dp, StitchSuccess.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CleaningServices, null, tint = StitchSuccess, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("30-Day Retention Policy Monitor", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StitchSuccess.copy(alpha = 0.15f))
                            .border(1.dp, StitchSuccess.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(if (summary.isClean) "RETENTION NOMINAL" else "PURGE EXECUTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StitchSuccess, fontFamily = FontFamily.Monospace)
                    }
                }
                Text(
                    text = "Cutoff Window: Older than ${summary.retentionDays} days (Cutoff: ${summary.cutoffDate}) · Last sync: ${summary.lastVerified}",
                    fontSize = 10.sp,
                    color = StitchTextMuted
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text(if (summary.staleMatchesDeleted > 0) "${summary.staleMatchesDeleted} Purged" else "0 Stale Rows", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (summary.staleMatchesDeleted > 0) StitchWarning else StitchSuccess, fontFamily = FontFamily.Monospace)
                Text("PostgreSQL Storage Clean", fontSize = 9.sp, color = StitchTextMuted)
            }
        }
    }
}

@Composable
fun PipelineStepCard(
    step: PipelineStageStep,
    onInspect: () -> Unit,
    onOpenVideo: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val stageColor = when (step.stage) {
        PipelineStepStage.MATCHED, PipelineStepStage.LINKED, PipelineStepStage.LIVE_GOAL,
        PipelineStepStage.RETENTION_CLEAN, PipelineStepStage.RETENTION_PURGED -> StitchSuccess
        PipelineStepStage.AI_EVALUATION -> StitchPurple
        PipelineStepStage.SEARCHING, PipelineStepStage.CANDIDATES -> StitchSecondary
        PipelineStepStage.NOT_FOUND -> StitchWarning
        PipelineStepStage.QUOTA_ERROR -> StitchError
        else -> StitchCyan
    }

    val stageIcon = when (step.stage) {
        PipelineStepStage.SEARCHING -> Icons.Default.Search
        PipelineStepStage.CANDIDATES -> Icons.Default.FilterList
        PipelineStepStage.AI_EVALUATION -> Icons.Default.AutoAwesome
        PipelineStepStage.MATCHED, PipelineStepStage.LINKED -> Icons.Default.CheckCircle
        PipelineStepStage.NOT_FOUND -> Icons.Default.WarningAmber
        PipelineStepStage.QUOTA_ERROR -> Icons.Default.ErrorOutline
        PipelineStepStage.LIVE_GOAL -> Icons.Default.SportsSoccer
        PipelineStepStage.NEW_FIXTURE -> Icons.Default.AddCircleOutline
        PipelineStepStage.RETENTION_PURGED, PipelineStepStage.RETENTION_CLEAN -> Icons.Default.CleaningServices
        else -> Icons.Default.PlayCircle
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .clickable { onInspect() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Badges Header Row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Stage Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(stageColor.copy(alpha = 0.15f))
                        .border(1.dp, stageColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(stageIcon, null, tint = stageColor, modifier = Modifier.size(11.dp))
                        Text(step.stage.title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = stageColor, fontFamily = FontFamily.Monospace)
                    }
                }

                // League Pill
                LeagueBadgePill(step.league)

                // Match Date Pill
                DateBadgePill(step.matchDate)

                // Timestamp
                Text(step.timestamp.takeLast(13).take(8), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = StitchTextMuted)
            }

            // Fixture Title & Clean Detail
            Text(step.fixtureName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
            Text(step.detailMessage, fontSize = 11.sp, color = StitchTextSecondary, maxLines = 2)

            if (step.channelOrSource != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Tv, null, tint = StitchTextMuted, modifier = Modifier.size(11.dp))
                    Text(step.channelOrSource, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = StitchTextMuted)
                }
            }
        }

        // Action Buttons
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (step.videoUrl != null && onOpenVideo != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchPrimary.copy(alpha = 0.15f))
                        .border(1.dp, StitchPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .clickable { onOpenVideo(step.videoUrl) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Watch", fontSize = 9.sp, color = StitchPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = StitchPrimary, modifier = Modifier.size(11.dp))
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = StitchTextMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun LeagueBadgePill(league: String) {
    val color = when {
        league.contains("Premier", true) -> Color(0xFF38BDF8)
        league.contains("LaLiga", true) -> Color(0xFFF97316)
        league.contains("Serie A", true) -> Color(0xFF00E5FF)
        league.contains("Bundesliga", true) -> Color(0xFFEF4444)
        league.contains("Ligue 1", true) -> Color(0xFF84CC16)
        league.contains("Champions", true) -> Color(0xFFA855F7)
        else -> StitchTextMuted
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(league, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = color, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun DateBadgePill(dateStr: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(StitchSurfaceContainerHigh)
            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(dateStr, fontSize = 9.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
    }
}
