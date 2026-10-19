package com.dirzaaulia.footballclips.telemetry.ui.verifier

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainer
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTypography

@Composable
fun TelemetryVerifierFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterMode: String,
    onToggleFilterMode: () -> Unit,
    selectedCompetition: String,
    onCompetitionSelect: (String) -> Unit,
    competitions: List<String>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        "Search match (e.g. Dortmund, Pafos, Arsenal, or YouTube Video ID)...",
                        style = TelemetryTypography.bodyMedium.copy(color = StitchTextMuted),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                textStyle = TelemetryTypography.bodyMedium.copy(color = StitchTextPrimary),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = StitchPrimary, modifier = Modifier.size(16.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = StitchTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = StitchSurfaceContainer,
                    unfocusedContainerColor = StitchSurfaceContainer,
                    focusedBorderColor = StitchPrimary,
                    unfocusedBorderColor = StitchBorder,
                    focusedTextColor = StitchTextPrimary,
                    unfocusedTextColor = StitchTextPrimary,
                    cursorColor = StitchPrimary,
                ),
                modifier = Modifier.weight(1f),
            )

            Surface(
                onClick = onToggleFilterMode,
                shape = RoundedCornerShape(6.dp),
                color = StitchSurfaceContainer,
                border = BorderStroke(1.dp, StitchBorder),
                modifier = Modifier.height(40.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = when (filterMode) {
                            "LINKED" -> Icons.Default.PlayCircle
                            "MISSING" -> Icons.Default.Warning
                            else -> Icons.AutoMirrored.Filled.List
                        },
                        contentDescription = null,
                        tint = if (filterMode == "LINKED") StitchSuccess else StitchSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = when (filterMode) {
                            "LINKED" -> "FILTER: LINKED HIGHLIGHTS"
                            "MISSING" -> "FILTER: MISSING CLIPS"
                            else -> "FILTER: ALL MATCHES"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            Surface(
                onClick = onRefresh,
                shape = RoundedCornerShape(6.dp),
                color = StitchSurfaceContainer,
                border = BorderStroke(1.dp, StitchBorder),
                modifier = Modifier.height(40.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.Refresh, "Refresh", tint = StitchPrimary, modifier = Modifier.size(16.dp))
                    Text("REFRESH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchPrimary, fontFamily = FontFamily.Monospace)
                }
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(competitions, key = { it }) { comp ->
                val isSelected = selectedCompetition == comp
                Surface(
                    onClick = { onCompetitionSelect(comp) },
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) StitchPrimary.copy(alpha = 0.2f) else StitchSurfaceContainer,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) StitchPrimary else StitchBorder,
                    ),
                ) {
                    Text(
                        text = comp,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) StitchPrimary else StitchTextMuted,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
