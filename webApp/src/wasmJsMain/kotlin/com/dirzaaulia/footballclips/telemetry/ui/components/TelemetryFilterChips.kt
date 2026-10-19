package com.dirzaaulia.footballclips.telemetry.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.TelemetryFilterLevel
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchError
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTerminalBg
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning

@Composable
fun FilterCategoryChips(
    selectedLevel: TelemetryFilterLevel,
    onLevelSelected: (TelemetryFilterLevel) -> Unit,
    errorCount: Int,
    fontSize: TextUnit,
    paddingH: Dp,
    paddingV: Dp,
) {
    FilterPill(
        label = "All Logs",
        dotColor = StitchSecondary,
        isSelected = selectedLevel == TelemetryFilterLevel.ALL,
        fontSize = fontSize,
        paddingH = paddingH,
        paddingV = paddingV,
        onClick = { onLevelSelected(TelemetryFilterLevel.ALL) },
    )
    FilterPill(
        label = "Highlights",
        dotColor = StitchSuccess,
        isSelected = selectedLevel == TelemetryFilterLevel.HIGHLIGHT,
        fontSize = fontSize,
        paddingH = paddingH,
        paddingV = paddingV,
        onClick = { onLevelSelected(TelemetryFilterLevel.HIGHLIGHT) },
    )
    FilterPill(
        label = "Fixtures & Sync",
        dotColor = StitchPrimary,
        isSelected = selectedLevel == TelemetryFilterLevel.SYNC,
        fontSize = fontSize,
        paddingH = paddingH,
        paddingV = paddingV,
        onClick = { onLevelSelected(TelemetryFilterLevel.SYNC) },
    )
    FilterPill(
        label = "Live Scores",
        dotColor = StitchWarning,
        isSelected = selectedLevel == TelemetryFilterLevel.SCORE,
        fontSize = fontSize,
        paddingH = paddingH,
        paddingV = paddingV,
        onClick = { onLevelSelected(TelemetryFilterLevel.SCORE) },
    )
    FilterPill(
        label = "Warnings",
        dotColor = StitchWarning,
        isSelected = selectedLevel == TelemetryFilterLevel.WARN,
        fontSize = fontSize,
        paddingH = paddingH,
        paddingV = paddingV,
        onClick = { onLevelSelected(TelemetryFilterLevel.WARN) },
    )
    FilterPill(
        label = "Errors",
        dotColor = StitchError,
        isSelected = selectedLevel == TelemetryFilterLevel.ERROR,
        badge = if (errorCount > 0) "$errorCount" else null,
        badgeColor = StitchError,
        fontSize = fontSize,
        paddingH = paddingH,
        paddingV = paddingV,
        onClick = { onLevelSelected(TelemetryFilterLevel.ERROR) },
    )
}

@Composable
fun FilterActionChips(
    selectedDate: String?,
    onSearchChange: (String) -> Unit,
    onLevelSelected: (TelemetryFilterLevel) -> Unit,
    onDateSelected: (String?) -> Unit,
    fontSize: TextUnit,
    paddingH: Dp,
    height: Dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Date Indicator Chip
        Row(
            modifier = Modifier
                .height(height)
                .clip(RoundedCornerShape(8.dp))
                .background(StitchSurfaceContainer)
                .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = paddingH),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Date",
                tint = StitchTextSecondary,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = selectedDate ?: "Active Season",
                fontSize = fontSize,
                color = StitchTextSecondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
            )
        }

        // Reset Filters Button Chip
        Row(
            modifier = Modifier
                .height(height)
                .clip(RoundedCornerShape(8.dp))
                .background(StitchSurfaceContainer)
                .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
                .clickable {
                    onSearchChange("")
                    onLevelSelected(TelemetryFilterLevel.ALL)
                    onDateSelected(null)
                }
                .padding(horizontal = paddingH),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Reset Filters",
                tint = StitchTextSecondary,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = "Reset",
                fontSize = fontSize,
                color = StitchTextSecondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    dotColor: Color,
    isSelected: Boolean,
    badge: String? = null,
    badgeColor: Color = StitchError,
    fontSize: TextUnit = 12.sp,
    paddingH: Dp = 11.dp,
    paddingV: Dp = 7.dp,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) StitchPrimary.copy(alpha = 0.18f) else StitchTerminalBg
    val border = if (isSelected) StitchPrimary.copy(alpha = 0.6f) else StitchBorder
    val textColor = if (isSelected) StitchTextPrimary else StitchTextSecondary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = paddingH, vertical = paddingV),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            maxLines = 1,
        )
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = badgeColor.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
            ) {
                Text(
                    text = badge,
                    fontSize = (fontSize.value - 1.5f).sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                )
            }
        }
    }
}
