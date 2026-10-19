package com.dirzaaulia.footballclips.telemetry.ui.pipelines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainerHigh
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning

@Composable
fun TelemetryHighlightPipelineView(
    matches: List<ParsedMatch>,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    var expandedIndices by remember {
        mutableStateOf(matches.filter { it.isExpanded }.map { it.index }.toSet())
    }

    val isAllExpanded = matches.isNotEmpty() && expandedIndices.size == matches.size

    val filteredMatches = remember(matches, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) matches
        else matches.filter { m ->
            m.title.lowercase().contains(q) ||
            m.events.any { it.eventText.lowercase().contains(q) }
        }
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
                        expandedIndices = if (isAllExpanded) emptySet() else matches.map { it.index }.toSet()
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

        // Accordion List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(560.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredMatches, key = { it.index }) { match ->
                val isOpen = expandedIndices.contains(match.index)
                TelemetryMatchAccordion(
                    match = match,
                    isExpanded = isOpen,
                    onToggle = {
                        expandedIndices = if (isOpen) expandedIndices - match.index else expandedIndices + match.index
                    }
                )
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
