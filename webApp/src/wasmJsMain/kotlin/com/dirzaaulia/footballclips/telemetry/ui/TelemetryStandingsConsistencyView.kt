package com.dirzaaulia.footballclips.telemetry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.model.TelemetryStandingRecord
import kotlinx.coroutines.launch

@Composable
fun TelemetryStandingsConsistencyView(
    repository: TelemetryRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedComp by remember { mutableStateOf("PL") }
    var standings by remember { mutableStateOf<List<TelemetryStandingRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val competitions = listOf(
        "PL" to "Premier League",
        "CL" to "Champions League",
        "PD" to "La Liga",
        "SA" to "Serie A",
        "BL1" to "Bundesliga",
        "FL1" to "Ligue 1"
    )

    fun loadStandings() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            repository.getStandings(selectedComp)
                .onSuccess { data ->
                    standings = data
                    isLoading = false
                }
                .onFailure { err ->
                    errorMessage = err.message ?: "Failed to fetch standings from edge proxy"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(selectedComp) {
        loadStandings()
    }

    // Mathematical Consistency Audit: Check if Points == (3 * W) + D and GD == GF - GA
    val discrepancies = remember(standings) {
        standings.filter { team ->
            val expectedPts = (team.won * 3) + team.draw
            val expectedGd = team.goalsFor - team.goalsAgainst
            // Allow 1-2 points deduction if official penalty, but flag if large discrepancy
            team.points != expectedPts || team.goalDifference != expectedGd
        }
    }

    val isConsistent = discrepancies.isEmpty()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status & Consistency Overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StitchSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isConsistent) StitchSuccess.copy(alpha = 0.5f) else StitchError
                ),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isConsistent) StitchSuccess.copy(alpha = 0.15f) else StitchError.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isConsistent) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isConsistent) StitchSuccess else StitchError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text("MATHEMATICAL CONSISTENCY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                        Text(
                            text = if (isConsistent) "100% VERIFIED" else "${discrepancies.size} DISCREPANCY DETECTED",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isConsistent) StitchSuccess else StitchError,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (isConsistent) "Points = (3W + D) & GD = GF - GA" else "Points/GD mismatch in table",
                            fontSize = 10.5.sp,
                            color = StitchTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StitchSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(StitchPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = StitchPrimary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("TRACKED TABLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace)
                        Text("${standings.size} CLUBS", fontSize = 18.sp, fontWeight = FontWeight.Black, color = StitchPrimary, fontFamily = FontFamily.Monospace)
                        Text("Synced with PostgREST / Supabase", fontSize = 10.5.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // League Switcher Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(StitchSurface)
                .border(1.dp, StitchBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(competitions) { (code, name) ->
                    val isSelected = selectedComp == code
                    Surface(
                        onClick = { selectedComp = code },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) StitchPrimary.copy(alpha = 0.2f) else StitchSurfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) StitchPrimary else StitchBorder
                        )
                    ) {
                        Text(
                            text = "$name ($code)",
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) StitchPrimary else StitchTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Surface(
                onClick = { loadStandings() },
                shape = RoundedCornerShape(6.dp),
                color = StitchSurfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = StitchPrimary, modifier = Modifier.size(14.dp))
                    Text("AUDIT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchPrimary, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Standings Data Table
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
        } else if (standings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StitchSurface)
                    .border(1.dp, StitchBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No standings records found for this league", color = StitchTextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        } else {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StitchSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StitchSurfaceContainerHigh)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("POS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp))
                        Text("CLUB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        Text("P", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                        Text("W", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                        Text("D", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                        Text("L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                        Text("GD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchTextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(38.dp))
                        Text("PTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StitchPrimary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(42.dp))
                    }

                    // Table Rows
                    standings.forEachIndexed { index, team ->
                        val isEven = index % 2 == 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isEven) StitchSurface else StitchSurfaceContainer.copy(alpha = 0.3f))
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${team.position}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp))

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(StitchPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = team.teamName.take(2).uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StitchPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(team.teamName, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = StitchTextPrimary, fontFamily = FontFamily.Monospace)
                            }

                            Text("${team.playedGames}", fontSize = 12.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                            Text("${team.won}", fontSize = 12.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                            Text("${team.draw}", fontSize = 12.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                            Text("${team.lost}", fontSize = 12.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(32.dp))
                            Text("${if (team.goalDifference > 0) "+${team.goalDifference}" else "${team.goalDifference}"}", fontSize = 12.sp, color = StitchTextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(38.dp))
                            Text("${team.points}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = StitchPrimary, fontFamily = FontFamily.Monospace, modifier = Modifier.width(42.dp))
                        }
                    }
                }
            }
        }
    }
}
