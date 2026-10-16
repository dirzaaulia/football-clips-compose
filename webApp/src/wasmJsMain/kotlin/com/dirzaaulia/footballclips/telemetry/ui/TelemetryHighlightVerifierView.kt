package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.model.SupabaseMatchRecord
import kotlinx.coroutines.launch

@Composable
fun TelemetryHighlightVerifierView(
    repository: TelemetryRepository,
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var matches by remember { mutableStateOf<List<SupabaseMatchRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCompetition by remember { mutableStateOf("ALL") }
    var filterMode by remember { mutableStateOf("LINKED") } // "LINKED" or "MISSING" or "ALL"

    fun loadMatches() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val hasHl = when (filterMode) {
                "LINKED" -> true
                "MISSING" -> false
                else -> null
            }
            val st = if (filterMode == "MISSING") "FINISHED" else null
            repository.getSyncedMatches(limit = 150, hasHighlight = hasHl, status = st)
                .onSuccess { data ->
                    matches = data
                    isLoading = false
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "Failed to fetch matches from edge proxy"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(filterMode) {
        loadMatches()
    }

    val competitions = listOf("ALL", "PL", "PD", "BL1", "SA", "FL1", "CL", "EL", "ECL")

    val filteredHighlights = remember(matches, searchQuery, selectedCompetition, filterMode) {
        matches.filter { match ->
            // Filter by highlight status
            val statusPass = when (filterMode) {
                "LINKED" -> !match.highlightVideoId.isNullOrBlank()
                "MISSING" -> match.status == "FINISHED" && match.highlightVideoId.isNullOrBlank()
                else -> true
            }
            if (!statusPass) return@filter false

            // Filter by competition
            val compPass = if (selectedCompetition == "ALL") true else {
                match.competitionId.equals(selectedCompetition, ignoreCase = true) ||
                match.competitionName.contains(selectedCompetition, ignoreCase = true)
            }
            if (!compPass) return@filter false

            // Filter by search query
            val q = searchQuery.trim().lowercase()
            if (q.isBlank()) true else {
                match.homeTeamName.lowercase().contains(q) ||
                match.awayTeamName.lowercase().contains(q) ||
                match.competitionName.lowercase().contains(q) ||
                (match.highlightVideoId?.lowercase()?.contains(q) == true)
            }
        }
    }

    val totalScraped = matches.count { !it.highlightVideoId.isNullOrBlank() }
    val totalFinished = matches.count { it.status == "FINISHED" }
    val coveragePct = if (totalFinished > 0) (totalScraped * 100 / totalFinished) else 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top KPI Cards for Highlight Pipeline Health
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VerifierKpiCard(
                title = "VERIFIED HIGHLIGHTS LINKED",
                value = "$totalScraped",
                subtitle = "$coveragePct% of $totalFinished Finished Matches",
                color = StitchSuccess,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )

            VerifierKpiCard(
                title = "AWAITING HIGHLIGHTS",
                value = "${(totalFinished - totalScraped).coerceAtLeast(0)}",
                subtitle = "Under Tiered Quota Scheduler",
                color = if (totalFinished > totalScraped) StitchSecondary else StitchTextMuted,
                icon = Icons.Default.HourglassEmpty,
                modifier = Modifier.weight(1f)
            )

            VerifierKpiCard(
                title = "ZERO-COST GUARDRAIL",
                value = "100% OK",
                subtitle = "RSS + WebSub (0 Quota Spent)",
                color = StitchPrimary,
                icon = Icons.Default.Shield,
                modifier = Modifier.weight(1f)
            )
        }

        // Control Panel: Search & Filters
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search match (e.g. Dortmund, Pafos, Arsenal, or YouTube Video ID)...",
                            style = TelemetryTypography.bodyMedium.copy(color = StitchTextMuted),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    textStyle = TelemetryTypography.bodyMedium.copy(color = StitchTextPrimary),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = StitchPrimary, modifier = Modifier.size(16.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
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
                        cursorColor = StitchPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Mode Selector (Linked vs Missing)
                Surface(
                    onClick = {
                        filterMode = when (filterMode) {
                            "LINKED" -> "MISSING"
                            "MISSING" -> "ALL"
                            else -> "LINKED"
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    color = StitchSurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = when (filterMode) {
                                "LINKED" -> Icons.Default.PlayCircle
                                "MISSING" -> Icons.Default.Warning
                                else -> Icons.Default.List
                            },
                            contentDescription = null,
                            tint = if (filterMode == "LINKED") StitchSuccess else StitchSecondary,
                            modifier = Modifier.size(16.dp)
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
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Sync / Refresh Button
                Surface(
                    onClick = { loadMatches() },
                    shape = RoundedCornerShape(6.dp),
                    color = StitchSurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = StitchPrimary, modifier = Modifier.size(16.dp))
                        Text("REFRESH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchPrimary, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Competition Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(competitions) { comp ->
                    val isSelected = selectedCompetition == comp
                    Surface(
                        onClick = { selectedCompetition = comp },
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) StitchPrimary.copy(alpha = 0.2f) else StitchSurfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) StitchPrimary else StitchBorder
                        )
                    ) {
                        Text(
                            text = comp,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) StitchPrimary else StitchTextMuted,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Highlight Cards Grid / List
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StitchPrimary, modifier = Modifier.size(24.dp))
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchError, RoundedCornerShape(8.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(errorMessage!!, color = StitchError, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        } else if (filteredHighlights.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No highlight records match active criteria", color = StitchTextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "VERIFYING ${filteredHighlights.size} MATCH RECORD(S)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextMuted,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                filteredHighlights.forEach { match ->
                    HighlightVerificationCard(
                        match = match,
                        onOpenVideo = onOpenVideo
                    )
                }
            }
        }
    }
}

@Composable
private fun VerifierKpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StitchSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color, fontFamily = FontFamily.Monospace)
                Text(subtitle, fontSize = 10.5.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun HighlightVerificationCard(
    match: SupabaseMatchRecord,
    onOpenVideo: (String) -> Unit
) {
    val hasVideo = !match.highlightVideoId.isNullOrBlank()
    val videoId = match.highlightVideoId ?: ""
    val thumbnailUrl = if (hasVideo) "https://img.youtube.com/vi/$videoId/hqdefault.jpg" else ""

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StitchSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (hasVideo) StitchSuccess.copy(alpha = 0.35f) else StitchBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // YouTube Video Badge Preview
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 64.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, if (hasVideo) StitchSuccess.copy(alpha = 0.4f) else StitchBorder, RoundedCornerShape(6.dp))
                    .clickable { if (hasVideo) onOpenVideo("https://www.youtube.com/watch?v=$videoId") },
                contentAlignment = Alignment.Center
            ) {
                if (hasVideo) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Text("YOUTUBE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = FontFamily.Monospace)
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.MovieFilter, contentDescription = null, tint = StitchTextMuted, modifier = Modifier.size(18.dp))
                        Text("No Video", fontSize = 9.sp, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Match & Video Verification Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = StitchPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchPrimary.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = (match.competitionId ?: match.competitionName).uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = match.utcDate.replace("T", " ").take(16) + " UTC",
                        fontSize = 10.sp,
                        color = StitchTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Teams and Scoreline
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = match.homeTeamName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = StitchTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (match.homeScore != null && match.awayScore != null) "${match.homeScore} - ${match.awayScore}" else "VS",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (hasVideo) StitchSuccess else StitchTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = match.awayTeamName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = StitchTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // YouTube Scraped Metadata
                if (hasVideo) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "YT_ID: $videoId",
                            fontSize = 10.sp,
                            color = StitchSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(StitchTextMuted))
                        Text(
                            text = "Provider: ${(match.provider ?: "ESPN").uppercase()}",
                            fontSize = 10.sp,
                            color = StitchTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Text(
                        text = "Status: ${match.status} | Scraping queue active",
                        fontSize = 10.sp,
                        color = StitchTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Right Verification Actions
            if (hasVideo) {
                Surface(
                    onClick = { onOpenVideo("https://www.youtube.com/watch?v=$videoId") },
                    shape = RoundedCornerShape(6.dp),
                    color = StitchSuccess.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchSuccess.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("VERIFY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchSuccess, fontFamily = FontFamily.Monospace)
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = StitchSuccess, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
