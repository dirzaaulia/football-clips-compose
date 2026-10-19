package com.dirzaaulia.footballclips.telemetry.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.TelemetryFilterLevel
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTerminalBg
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TelemetryFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedLevel: TelemetryFilterLevel,
    onLevelSelected: (TelemetryFilterLevel) -> Unit,
    selectedDate: String?,
    onDateSelected: (String?) -> Unit,
    errorCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val containerWidth = maxWidth
        val isUltraWide = containerWidth >= 1400.dp
        val isWide = containerWidth in 1050.dp..1399.dp
        val isMedium = containerWidth in 750.dp..1049.dp
        val isCompact = containerWidth < 750.dp

        val fontSize = when {
            isUltraWide -> 12.sp
            isWide -> 11.5.sp
            isMedium -> 11.sp
            else -> 10.5.sp
        }
        val pillPaddingHorizontal = when {
            isUltraWide -> 12.dp
            isWide -> 10.dp
            isMedium -> 9.dp
            else -> 8.dp
        }
        val pillPaddingVertical = when {
            isUltraWide -> 7.dp
            isWide -> 6.dp
            else -> 5.dp
        }
        val searchHeight = if (isCompact) 40.dp else 44.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(10.dp))
                .padding(if (isCompact) 10.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 1. Search Bar (Left) with Date and Reset buttons on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Search Input Field
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(searchHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StitchTerminalBg)
                        .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = StitchPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = when {
                                    isUltraWide -> "Search logs, clubs, competitions, or log levels (e.g. Arsenal, HIGHLIGHT, SYNC, ERROR)..."
                                    isWide -> "Search logs, clubs, competitions, or log levels..."
                                    isMedium -> "Search logs, club, competition, or level..."
                                    else -> "Search logs..."
                                },
                                style = TelemetryTypography.bodyMedium.copy(
                                    color = StitchTextSecondary,
                                    fontSize = fontSize,
                                ),
                                maxLines = 1,
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TelemetryTypography.bodyMedium.copy(
                                color = StitchTextPrimary,
                                fontSize = fontSize,
                            ),
                            cursorBrush = SolidColor(StitchPrimary),
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchChange("") },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = StitchTextSecondary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                // Date and Reset Buttons: Horizontally aligned on the right side of the search bar
                FilterActionChips(
                    selectedDate = selectedDate,
                    onSearchChange = onSearchChange,
                    onLevelSelected = onLevelSelected,
                    onDateSelected = onDateSelected,
                    fontSize = fontSize,
                    paddingH = pillPaddingHorizontal,
                    height = searchHeight,
                )
            }

            // 2. Filter Category Pills Row (All Logs, Highlights, Fixtures & Sync, Live Scores, Warnings, Errors)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterCategoryChips(
                    selectedLevel = selectedLevel,
                    onLevelSelected = onLevelSelected,
                    errorCount = errorCount,
                    fontSize = fontSize,
                    paddingH = pillPaddingHorizontal,
                    paddingV = pillPaddingVertical,
                )
            }
        }
    }
}
