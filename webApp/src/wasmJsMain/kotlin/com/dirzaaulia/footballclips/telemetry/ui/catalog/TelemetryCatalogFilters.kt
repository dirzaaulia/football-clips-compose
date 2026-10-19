package com.dirzaaulia.footballclips.telemetry.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
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

@Composable
fun TelemetryCatalogFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCompetition: String,
    onCompetitionSelect: (String) -> Unit,
    competitions: List<String>,
    selectedStatus: String,
    onStatusSelect: (String) -> Unit,
    statuses: List<String>,
    highlightsOnly: Boolean,
    onHighlightsOnlyToggle: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = StitchTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                ),
                cursorBrush = SolidColor(StitchPrimary),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSurfaceContainer)
                    .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = StitchPrimary, modifier = Modifier.size(16.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Search match, club, competition, video ID or match ID...",
                                    fontSize = 12.sp,
                                    color = StitchTextMuted,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = StitchTextMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onSearchQueryChange("") },
                            )
                        }
                    }
                },
            )

            Surface(
                onClick = onHighlightsOnlyToggle,
                shape = RoundedCornerShape(6.dp),
                color = if (highlightsOnly) StitchSuccess.copy(alpha = 0.15f) else StitchSurfaceContainer,
                border = BorderStroke(
                    1.dp,
                    if (highlightsOnly) StitchSuccess else StitchBorder,
                ),
                modifier = Modifier.height(42.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (highlightsOnly) StitchSuccess else StitchTextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "HIGHLIGHTS ONLY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (highlightsOnly) StitchSuccess else StitchTextMuted,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            Surface(
                onClick = onRefresh,
                shape = RoundedCornerShape(6.dp),
                color = StitchSurfaceContainer,
                border = BorderStroke(1.dp, StitchBorder),
                modifier = Modifier.height(42.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = StitchPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "SYNC NOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
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

            Spacer(Modifier.width(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                statuses.forEach { st ->
                    val isSelected = selectedStatus == st
                    Surface(
                        onClick = { onStatusSelect(st) },
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) StitchSecondary.copy(alpha = 0.2f) else StitchSurfaceContainer,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) StitchSecondary else StitchBorder,
                        ),
                    ) {
                        Text(
                            text = st,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) StitchSecondary else StitchTextMuted,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
