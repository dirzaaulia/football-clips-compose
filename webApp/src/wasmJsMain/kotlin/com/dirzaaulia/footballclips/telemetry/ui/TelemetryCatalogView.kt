package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.model.SupabaseMatchRecord
import kotlinx.coroutines.launch

@Composable
fun TelemetryCatalogView(
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
    var selectedStatus by remember { mutableStateOf("ALL") }
    var highlightsOnly by remember { mutableStateOf(false) }

    fun loadMatches() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            repository.getSyncedMatches(limit = 150)
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

    LaunchedEffect(Unit) {
        loadMatches()
    }

    val competitions = listOf("ALL", "PL", "PD", "BL1", "SA", "FL1", "CL", "EL", "ECL")
    val statuses = listOf("ALL", "FINISHED", "IN_PLAY", "TIMED", "SCHEDULED")

    val filteredMatches = remember(matches, searchQuery, selectedCompetition, selectedStatus, highlightsOnly) {
        matches.filter { match ->
            val compMatches = if (selectedCompetition == "ALL") true else {
                match.competitionId.equals(selectedCompetition, ignoreCase = true) ||
                match.competitionName.contains(selectedCompetition, ignoreCase = true)
            }

            val statusMatches = if (selectedStatus == "ALL") true else {
                match.status.equals(selectedStatus, ignoreCase = true) ||
                (selectedStatus == "IN_PLAY" && match.status in listOf("IN_PLAY", "PAUSED", "LIVE")) ||
                (selectedStatus in listOf("TIMED", "SCHEDULED") && match.status in listOf("TIMED", "SCHEDULED"))
            }

            val highlightMatches = if (!highlightsOnly) true else {
                !match.highlightVideoId.isNullOrBlank()
            }

            val q = searchQuery.trim().lowercase()
            val searchMatches = if (q.isBlank()) true else {
                match.homeTeamName.lowercase().contains(q) ||
                match.awayTeamName.lowercase().contains(q) ||
                match.competitionName.lowercase().contains(q) ||
                match.id.toString().contains(q) ||
                (match.highlightVideoId?.lowercase()?.contains(q) == true)
            }

            compMatches && statusMatches && highlightMatches && searchMatches
        }
    }

    val totalCount = matches.size
    val withHighlightCount = matches.count { !it.highlightVideoId.isNullOrBlank() }
    val inPlayCount = matches.count { it.status in listOf("IN_PLAY", "PAUSED", "LIVE") }
    val finishedCount = matches.count { it.status == "FINISHED" }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPI Overview Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CatalogKpiCard(
                title = "INDEXED MATCHES",
                value = "$totalCount",
                subtext = "Live Supabase Database",
                color = StitchPrimary,
                modifier = Modifier.weight(1f)
            )
            CatalogKpiCard(
                title = "LINKED HIGHLIGHTS",
                value = "$withHighlightCount",
                subtext = if (totalCount > 0) "${(withHighlightCount * 100 / totalCount)}% Coverage" else "0%",
                color = StitchSuccess,
                modifier = Modifier.weight(1f)
            )
            CatalogKpiCard(
                title = "FINISHED MATCHES",
                value = "$finishedCount",
                subtext = "Complete Full Time",
                color = StitchSecondary,
                modifier = Modifier.weight(1f)
            )
            CatalogKpiCard(
                title = "LIVE / IN PLAY",
                value = "$inPlayCount",
                subtext = "High Frequency Sync",
                color = if (inPlayCount > 0) StitchError else StitchTextMuted,
                modifier = Modifier.weight(1f)
            )
        }

        // Search & Filter Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search match club, competition, video ID or match ID...",
                            fontSize = 12.sp,
                            color = StitchTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    },
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
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = StitchSurfaceContainer,
                        unfocusedContainerColor = StitchSurfaceContainer,
                        focusedTextColor = StitchTextPrimary,
                        unfocusedTextColor = StitchTextPrimary,
                        cursorColor = StitchPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
                )

                // Highlight Toggle
                Surface(
                    onClick = { highlightsOnly = !highlightsOnly },
                    shape = RoundedCornerShape(6.dp),
                    color = if (highlightsOnly) StitchSuccess.copy(alpha = 0.15f) else StitchSurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (highlightsOnly) StitchSuccess else StitchBorder
                    ),
                    modifier = Modifier.height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (highlightsOnly) StitchSuccess else StitchTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "HIGHLIGHTS ONLY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (highlightsOnly) StitchSuccess else StitchTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Refresh Button
                Surface(
                    onClick = { loadMatches() },
                    shape = RoundedCornerShape(6.dp),
                    color = StitchSurfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                    modifier = Modifier.height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = StitchPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "SYNC NOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Competition & Status Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Competitions Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
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

                Spacer(Modifier.width(12.dp))

                // Status Row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    statuses.forEach { st ->
                        val isSelected = selectedStatus == st
                        Surface(
                            onClick = { selectedStatus = st },
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) StitchSecondary.copy(alpha = 0.2f) else StitchSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) StitchSecondary else StitchBorder
                            )
                        ) {
                            Text(
                                text = st,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) StitchSecondary else StitchTextMuted,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Catalog Content Table / Cards
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = StitchPrimary
                    )
                    Text(
                        "Connecting to Cloudflare Edge Proxy & Fetching Catalog...",
                        color = StitchTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchError.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Database Catalog Error",
                        color = StitchError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        errorMessage!!,
                        color = StitchTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Button(
                        onClick = { loadMatches() },
                        colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Retry Connection", color = StitchBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (filteredMatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = StitchTextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "No matches found matching active filters",
                        color = StitchTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHOWING ${filteredMatches.size} RECORD(S)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "EDGE PROXY: ACTIVE (200 OK)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchSuccess,
                        fontFamily = FontFamily.Monospace
                    )
                }

                filteredMatches.forEach { match ->
                    MatchCatalogItemCard(
                        match = match,
                        onOpenVideo = onOpenVideo
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchCatalogItemCard(
    match: SupabaseMatchRecord,
    onOpenVideo: (String) -> Unit
) {
    val isFinished = match.status.equals("FINISHED", ignoreCase = true)
    val isLive = match.status in listOf("IN_PLAY", "PAUSED", "LIVE")
    val hasHighlight = !match.highlightVideoId.isNullOrBlank()

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StitchSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLive) StitchError.copy(alpha = 0.5f) else StitchBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Competition, Kickoff, Status, Provider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = StitchPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = (match.competitionId ?: match.competitionName).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StitchPrimary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = match.utcDate.replace("T", " ").take(16) + " UTC",
                        fontSize = 11.sp,
                        color = StitchTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Status Badge
                    val statusColor = when {
                        isLive -> StitchError
                        isFinished -> StitchSuccess
                        else -> StitchTextMuted
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isLive) "● LIVE ${match.displayClock ?: ""}" else match.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Provider Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = StitchSurfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                    ) {
                        Text(
                            text = (match.provider ?: "ESPN").uppercase(),
                            fontSize = 10.sp,
                            color = StitchSecondary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Teams & Score Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(StitchSurfaceContainer)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Text(
                    text = match.homeTeamName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Score / VS
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isFinished || isLive) StitchPrimary.copy(alpha = 0.15f) else StitchSurfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isFinished || isLive) StitchPrimary.copy(alpha = 0.3f) else StitchBorder
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    val scoreText = if (match.homeScore != null && match.awayScore != null) {
                        "${match.homeScore} - ${match.awayScore}"
                    } else "VS"
                    Text(
                        text = scoreText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isFinished || isLive) StitchPrimary else StitchTextMuted,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Away Team
                Text(
                    text = match.awayTeamName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }

            // Footer Row: Highlight Attachment & Meta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasHighlight) {
                    Surface(
                        onClick = { onOpenVideo("https://www.youtube.com/watch?v=${match.highlightVideoId}") },
                        shape = RoundedCornerShape(4.dp),
                        color = StitchSuccess.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchSuccess.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = StitchSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "YOUTUBE: ${match.highlightVideoId}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchSuccess,
                                fontFamily = FontFamily.Monospace
                            )
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = StitchSuccess,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StitchTextMuted.copy(alpha = 0.4f)))
                        Text(
                            text = if (isFinished) "No Highlight Attached (Pending Scraping)" else "Awaiting Match Kickoff",
                            fontSize = 11.sp,
                            color = StitchTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = "ID: ${match.id}",
                    fontSize = 10.sp,
                    color = StitchTextMuted.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun CatalogKpiCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StitchSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = StitchTextMuted,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = StitchTextMuted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
