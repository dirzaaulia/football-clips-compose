package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.TelemetryFilterLevel

@Composable
fun TelemetryFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedLevel: TelemetryFilterLevel,
    onLevelSelected: (TelemetryFilterLevel) -> Unit,
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    errorCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Input Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StitchTerminalBg)
                .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = StitchPrimary,
                modifier = Modifier.size(18.dp)
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search text / club name / competition / log level (e.g., Arsenal, INFO, MATCHED)...",
                        style = TelemetryTypography.bodyMedium.copy(color = StitchTextSecondary),
                        maxLines = 1
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TelemetryTypography.bodyMedium.copy(color = StitchTextPrimary),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(StitchPrimary)
                )
            }
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = StitchTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Filter Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Filter Category Pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterPill(
                    label = "All Events & Logs",
                    dotColor = StitchSecondary,
                    isSelected = selectedLevel == TelemetryFilterLevel.ALL,
                    onClick = { onLevelSelected(TelemetryFilterLevel.ALL) }
                )
                FilterPill(
                    label = "Highlights & Clips",
                    dotColor = StitchSuccess,
                    isSelected = selectedLevel == TelemetryFilterLevel.HIGHLIGHT,
                    onClick = { onLevelSelected(TelemetryFilterLevel.HIGHLIGHT) }
                )
                FilterPill(
                    label = "Fixtures & Database",
                    dotColor = StitchPrimary,
                    isSelected = selectedLevel == TelemetryFilterLevel.SYNC,
                    onClick = { onLevelSelected(TelemetryFilterLevel.SYNC) }
                )
                FilterPill(
                    label = "Goals & Live Scores",
                    dotColor = StitchWarning,
                    isSelected = selectedLevel == TelemetryFilterLevel.SCORE,
                    onClick = { onLevelSelected(TelemetryFilterLevel.SCORE) }
                )
                FilterPill(
                    label = "Warnings & Quotas",
                    dotColor = StitchWarning,
                    isSelected = selectedLevel == TelemetryFilterLevel.WARN,
                    onClick = { onLevelSelected(TelemetryFilterLevel.WARN) }
                )
                FilterPill(
                    label = "Errors ($errorCount)",
                    dotColor = StitchError,
                    isSelected = selectedLevel == TelemetryFilterLevel.ERROR,
                    onClick = { onLevelSelected(TelemetryFilterLevel.ERROR) }
                )
            }

            // Secondary Quick Actions (Date filter & Reset)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSurfaceContainer)
                        .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = StitchTextSecondary, modifier = Modifier.size(12.dp))
                    Text(selectedDate ?: "Today (10 Oct 2026)", fontSize = 11.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSurfaceContainer)
                        .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                        .clickable {
                            onSearchChange("")
                            onLevelSelected(TelemetryFilterLevel.ALL)
                            onDateSelected(null)
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = StitchTextSecondary, modifier = Modifier.size(12.dp))
                    Text("Reset Filters", fontSize = 11.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    dotColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) StitchPrimary.copy(alpha = 0.15f) else StitchTerminalBg
    val border = if (isSelected) StitchPrimary.copy(alpha = 0.45f) else StitchBorder
    val textColor = if (isSelected) StitchTextPrimary else StitchTextSecondary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dotColor))
        Text(
            text = label,
            style = TelemetryTypography.labelMedium.copy(
                color = textColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}
