package com.dirzaaulia.footballclips.telemetry.ui.pipelines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.ParsedMatch
import com.dirzaaulia.footballclips.telemetry.ui.catalog.TelemetryMatchAccordion
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
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning

data class CycleMatchGroup(
    val cycleId: Long,
    val cycleName: String,
    val matches: List<ParsedMatch>
)

@Composable
fun TelemetryHighlightPipelineView(
    matches: List<ParsedMatch>,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    var expandedMatchIndices by remember {
        mutableStateOf(matches.filter { it.isExpanded }.map { it.index }.toSet())
    }

    val isAllExpanded = matches.isNotEmpty() && expandedMatchIndices.size == matches.size

    val filteredMatches = remember(matches, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) matches
        else matches.filter { m ->
            m.title.lowercase().contains(q) ||
            m.events.any { it.eventText.lowercase().contains(q) }
        }
    }

    // Group matches by Cycle ID (extracted from events or chunked by 10 items)
    val cycleGroups = remember(filteredMatches) {
        filteredMatches.groupBy { match ->
            match.events.firstOrNull()?.cycleId?.takeIf { it > 0 } ?: ((match.index / 10).toLong() + 1)
        }.map { (cycleId, matchGroup) ->
            CycleMatchGroup(
                cycleId = cycleId,
                cycleName = "Background Cycle #$cycleId",
                matches = matchGroup
            )
        }
    }

    var expandedCycleIds by remember {
        mutableStateOf(cycleGroups.map { it.cycleId }.toSet())
    }

    val successCount = matches.count { it.statusType == "success" }
    val failCount = matches.count { it.statusType == "fail" }
    val errorCount = matches.count { it.statusType == "error" }
    val skipCount = matches.count { it.statusType == "skip" }
    val inProgressCount = matches.count { it.statusType == "process" }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchSuccess.copy(alpha = 0.15f))
                        .border(1.dp, StitchSuccess.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("$successCount Linked", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchSuccess, fontFamily = FontFamily.Monospace)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StitchWarning.copy(alpha = 0.15f))
                        .border(1.dp, StitchWarning.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("$failCount Not Found", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchWarning, fontFamily = FontFamily.Monospace)
                }
                if (errorCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StitchError.copy(alpha = 0.15f))
                            .border(1.dp, StitchError.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("$errorCount Error(s)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchError, fontFamily = FontFamily.Monospace)
                    }
                }
                if (skipCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StitchSurfaceContainerHigh)
                            .border(1.dp, StitchBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("$skipCount Skipped", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
                if (inProgressCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StitchSecondary.copy(alpha = 0.15f))
                            .border(1.dp, StitchSecondary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("$inProgressCount In Progress", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = StitchSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchSurfaceContainerHigh)
                    .border(0.5.dp, StitchBorder, RoundedCornerShape(4.dp))
                    .clickable {
                        if (isAllExpanded) {
                            expandedMatchIndices = emptySet()
                            expandedCycleIds = emptySet()
                        } else {
                            expandedMatchIndices = matches.map { it.index }.toSet()
                            expandedCycleIds = cycleGroups.map { it.cycleId }.toSet()
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isAllExpanded) "Collapse All" else "Expand All",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = StitchPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Cycle Grouped Accordion List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(560.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cycleGroups, key = { it.cycleId }) { group ->
                val isCycleOpen = expandedCycleIds.contains(group.cycleId)
                val groupSuccess = group.matches.count { it.statusType == "success" }
                val groupTotal = group.matches.size

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StitchSurfaceContainer)
                        .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
                ) {
                    // Top Cycle Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedCycleIds = if (isCycleOpen) expandedCycleIds - group.cycleId else expandedCycleIds + group.cycleId
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isCycleOpen) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                contentDescription = if (isCycleOpen) "Collapse Cycle" else "Expand Cycle",
                                tint = StitchTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.Loop,
                                contentDescription = "Cycle",
                                tint = StitchPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = group.cycleName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StitchPrimary.copy(alpha = 0.15f))
                                    .border(1.dp, StitchPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$groupSuccess / $groupTotal Linked",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Nested Matches Accordions inside Cycle
                    if (isCycleOpen) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StitchSurface)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            group.matches.forEach { match ->
                                val isOpen = expandedMatchIndices.contains(match.index)
                                TelemetryMatchAccordion(
                                    match = match,
                                    isExpanded = isOpen,
                                    onToggle = {
                                        expandedMatchIndices = if (isOpen) {
                                            expandedMatchIndices - match.index
                                        } else {
                                            expandedMatchIndices + match.index
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (filteredMatches.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (matches.isEmpty()) {
                                    "No highlight scraping activity recorded in recent cycles."
                                } else if (searchQuery.isNotBlank()) {
                                    "No highlight activity matching \"$searchQuery\"."
                                } else {
                                    "No matches found matching filter."
                                },
                                color = StitchTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            if (searchQuery.isNotBlank()) {
                                Text(
                                    text = "Check spelling or search for another club, channel handle, or fixture.",
                                    color = StitchTextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
