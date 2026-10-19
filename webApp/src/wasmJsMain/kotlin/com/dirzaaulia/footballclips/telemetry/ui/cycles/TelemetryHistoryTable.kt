package com.dirzaaulia.footballclips.telemetry.ui.cycles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.CycleHistoryItem
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary

@Composable
fun TelemetryHistoryTable(
    history: List<CycleHistoryItem>,
    onInspect: (CycleHistoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Table Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.History, null, tint = StitchSecondary, modifier = Modifier.size(15.dp))
                Text(
                    text = "Recent Cycle Run History",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text("Click row to inspect \u2022 Last 24 Hours", fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
        }

        // Table Column Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TIMESTAMP (WIB)", fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.4f))
            Text("EXECUTION MODE", fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("INGESTED MATCHES", fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("HIGHLIGHTS HARVESTED", fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f))
            Text("API QUOTA COST", fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
            Text("DB STATUS", fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
            Text("ERRORS", fontSize = 8.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
            Spacer(Modifier.width(16.dp))
        }

        // Table Rows
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            history.forEach { item ->
                HistoryRow(item = item, onClick = { onInspect(item) })
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: CycleHistoryItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(StitchSurfaceContainer)
            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timestamp
        Row(
            modifier = Modifier.weight(1.4f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(StitchSuccess))
            Text(item.timestamp, fontSize = 9.sp, color = StitchTextPrimary, fontFamily = FontFamily.Monospace, maxLines = 1)
        }

        // Execution Mode
        Box(modifier = Modifier.weight(1f)) {
            val isFull = item.executionMode.contains("FULL", true)
            val bg = if (isFull) StitchSecondary.copy(alpha = 0.15f) else StitchSurface
            val color = if (isFull) StitchSecondary else StitchTextSecondary
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(bg)
                    .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(item.executionMode, fontSize = 8.sp, color = color, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        // Ingested Matches
        Text(
            text = "${item.ingestedMatches} matches",
            fontSize = 9.sp,
            color = StitchPrimary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )

        // Highlights Harvested
        Text(
            text = "${item.highlightsHarvested} clips",
            fontSize = 9.sp,
            color = StitchSuccess,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1.1f)
        )

        // API Quota Cost
        Text(
            text = "${item.apiQuotaCost} units",
            fontSize = 9.sp,
            color = StitchTextSecondary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(0.9f)
        )

        // DB Status
        Text(
            text = item.dbStatus,
            fontSize = 8.sp,
            color = StitchSuccess,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1.2f)
        )

        // Exceptions
        Text(
            text = "${item.exceptions} err",
            fontSize = 9.sp,
            color = if (item.exceptions > 0) StitchError else StitchTextMuted,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(0.6f)
        )

        // Open Inspector Chevron Icon
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Inspect", tint = StitchTextMuted, modifier = Modifier.size(14.dp))
    }
}
