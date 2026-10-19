package com.dirzaaulia.footballclips.telemetry.ui.standings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.TelemetryStandingRecord
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary

@Composable
fun StandingsMetricCards(
    isConsistent: Boolean,
    discrepanciesCount: Int,
    standingsCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StitchSurface,
            border = BorderStroke(
                1.dp,
                if (isConsistent) StitchSuccess.copy(alpha = 0.5f) else StitchError,
            ),
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isConsistent) StitchSuccess.copy(alpha = 0.15f) else StitchError.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isConsistent) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isConsistent) StitchSuccess else StitchError,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text("MATHEMATICAL CONSISTENCY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                    Text(
                        text = if (isConsistent) "100% VERIFIED" else "$discrepanciesCount DISCREPANCY DETECTED",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isConsistent) StitchSuccess else StitchError,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        text = if (isConsistent) "Points = (3W + D) & GD = GF - GA" else "Points/GD mismatch in table",
                        fontSize = 10.5.sp,
                        color = StitchTextSecondary,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = StitchSurface,
            border = BorderStroke(1.dp, StitchBorder),
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(StitchPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = StitchPrimary, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("TRACKED TABLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                    Text("$standingsCount CLUBS", fontSize = 18.sp, fontWeight = FontWeight.Black, color = StitchPrimary, fontFamily = FontFamily.Monospace)
                    Text("Synced with PostgREST / PostgreSQL 16", fontSize = 10.5.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun StandingsFilterAndAuditBar(
    competitions: List<Pair<String, String>>,
    selectedComp: String,
    onCompSelect: (String) -> Unit,
    selectedSeason: Int,
    onSeasonToggle: () -> Unit,
    onAuditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(competitions, key = { it.first }) { (code, name) ->
                val isSelected = selectedComp == code
                Surface(
                    onClick = { onCompSelect(code) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) StitchPrimary.copy(alpha = 0.2f) else StitchSurfaceContainer,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) StitchPrimary else StitchBorder,
                    ),
                ) {
                    Text(
                        text = "$name ($code)",
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) StitchPrimary else StitchTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onSeasonToggle,
                shape = RoundedCornerShape(6.dp),
                color = StitchSurfaceContainer,
                border = BorderStroke(1.dp, StitchPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.height(34.dp),
            ) {
                Text(
                    text = if (selectedSeason == 2026) "SEASON: 2025/26" else "SEASON: 2024/25",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchPrimary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                )
            }

            Surface(
                onClick = onAuditClick,
                shape = RoundedCornerShape(6.dp),
                color = StitchSurfaceContainer,
                border = BorderStroke(1.dp, StitchBorder),
                modifier = Modifier.height(34.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Default.Refresh, null, tint = StitchPrimary, modifier = Modifier.size(14.dp))
                    Text("AUDIT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchPrimary, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
