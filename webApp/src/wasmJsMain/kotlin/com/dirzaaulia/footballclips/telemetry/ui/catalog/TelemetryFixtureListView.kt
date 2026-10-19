package com.dirzaaulia.footballclips.telemetry.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.LogStreamItem
import com.dirzaaulia.footballclips.telemetry.ui.pipelines.DateBadgePill
import com.dirzaaulia.footballclips.telemetry.ui.pipelines.LeagueBadgePill
import com.dirzaaulia.footballclips.telemetry.ui.pipelines.TelemetryPipelineMapper
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchCyan
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSuccess
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchWarning

enum class FixtureLogCategory(val label: String) {
    ALL("All Updates"),
    NEW_FIXTURES("New Fixtures"),
    LIVE_SCORES("Live Scores"),
    MATCH_EVENTS("Kickoff & FT")
}

@Composable
fun TelemetryFixtureListView(
    logs: List<LogStreamItem>,
    searchQuery: String,
    selectedLeague: String,
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(FixtureLogCategory.ALL) }

    // Filter only match/fixture sync related logs
    val fixtureLogs = remember(logs) {
        logs.filter { item ->
            item.subsystem == "MATCH_SYNC" ||
            item.cleanMessage.contains("FIXTURE", ignoreCase = true) ||
            item.cleanMessage.contains("KICKOFF", ignoreCase = true) ||
            item.cleanMessage.contains("FULL TIME", ignoreCase = true) ||
            item.cleanMessage.contains("LIVE SCORE", ignoreCase = true) ||
            item.cleanMessage.contains("SKOR", ignoreCase = true) ||
            item.cleanMessage.contains("STATUS", ignoreCase = true) ||
            item.cleanMessage.contains("fixture", ignoreCase = true)
        }
    }

    val filteredLogs = remember(fixtureLogs, selectedCategory, selectedLeague, searchQuery) {
        fixtureLogs.filter { item ->
            val msg = item.cleanMessage.uppercase()
            val matchesCategory = when (selectedCategory) {
                FixtureLogCategory.ALL -> true
                FixtureLogCategory.NEW_FIXTURES -> msg.contains("NEW FIXTURE") || msg.contains("INSERTED")
                FixtureLogCategory.LIVE_SCORES -> msg.contains("LIVE SCORE") || msg.contains("SKOR") || msg.contains("GOAL") || item.isGoal
                FixtureLogCategory.MATCH_EVENTS -> msg.contains("KICKOFF") || msg.contains("FULL TIME") || msg.contains("STATUS")
            }

            val leagueName = TelemetryPipelineMapper.extractLeague(item.cleanMessage) ?: "ALL"
            val matchesLeague = selectedLeague == "ALL" || leagueName.equals(selectedLeague, ignoreCase = true)

            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isBlank() ||
                item.cleanMessage.lowercase().contains(q) ||
                item.timestamp.lowercase().contains(q)

            matchesCategory && matchesLeague && matchesSearch
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Sub-category filters row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FixtureLogCategory.entries.forEach { cat ->
                    val isSel = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) StitchPrimary.copy(alpha = 0.15f) else StitchSurface)
                            .border(1.dp, if (isSel) StitchPrimary else StitchBorder, RoundedCornerShape(4.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(cat.label, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) StitchPrimary else StitchTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            Text("${filteredLogs.size} Logged Operations", fontSize = 10.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
        }

        // List of fixture insert & update log entries
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(560.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredLogs, key = { it.id }) { item ->
                FixtureLogCard(item = item, onOpenVideo = onOpenVideo)
            }
            if (filteredLogs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (searchQuery.isNotBlank()) {
                                Icon(Icons.Default.Search, null, tint = StitchTextMuted, modifier = Modifier.size(24.dp))
                                Text("No fixture activity matching \"$searchQuery\"", color = StitchTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("Try searching with different club names, leagues, or keywords.", color = StitchTextMuted, fontSize = 9.sp)
                            } else {
                                Text("No fixture insert/update activity recorded matching filters.", color = StitchTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("The worker logs upcoming fixture inserts, live score goals, kickoffs, and full time results in real-time.", color = StitchTextMuted, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FixtureLogCard(
    item: LogStreamItem,
    onOpenVideo: (String) -> Unit
) {
    val msg = item.cleanMessage
    val upper = msg.uppercase()

    val (badgeText, badgeColor, icon) = when {
        upper.contains("LIVE SCORE") || upper.contains("SKOR") || upper.contains("GOAL") || item.isGoal ->
            Triple("LIVE SCORE", StitchWarning, Icons.Default.SportsSoccer)
        upper.contains("KICKOFF") ->
            Triple("KICKOFF", StitchCyan, Icons.Default.PlayCircle)
        upper.contains("FULL TIME") ->
            Triple("FULL TIME", StitchSuccess, Icons.Default.CheckCircle)
        upper.contains("NEW FIXTURE") || upper.contains("INSERTED") ->
            Triple("NEW FIXTURE", StitchPrimary, Icons.Default.AddCircleOutline)
        upper.contains("STATUS") ->
            Triple("STATUS UPDATE", StitchSecondary, Icons.Default.Sync)
        else ->
            Triple("FIXTURE SYNC", StitchTextSecondary, Icons.Default.Schedule)
    }

    val league = TelemetryPipelineMapper.extractLeague(msg) ?: "European Football"
    val date = TelemetryPipelineMapper.extractDate(msg) ?: item.timestamp.take(10)
    val fixtureName = TelemetryPipelineMapper.extractFixture(msg)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, if (badgeColor == StitchWarning) StitchWarning.copy(alpha = 0.4f) else StitchBorder, RoundedCornerShape(6.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(icon, null, tint = badgeColor, modifier = Modifier.size(10.dp))
                        Text(badgeText, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = badgeColor, fontFamily = FontFamily.Monospace)
                    }
                }
                LeagueBadgePill(league)
                DateBadgePill(date)
                Text(item.timestamp, fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
            }

            if (fixtureName != null) {
                Text(fixtureName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchTextPrimary)
            }
            Text(msg, fontSize = 11.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
        }

        item.extractedUrl?.let { url ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StitchPrimary.copy(alpha = 0.15f))
                    .border(1.dp, StitchPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .clickable { onOpenVideo(url) }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Watch", fontSize = 9.sp, color = StitchPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = StitchPrimary, modifier = Modifier.size(11.dp))
                }
            }
        }
    }
}
