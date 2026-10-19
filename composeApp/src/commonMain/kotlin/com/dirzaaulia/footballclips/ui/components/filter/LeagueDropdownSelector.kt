package com.dirzaaulia.footballclips.ui.components.filter

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.constants.BigLeague
import com.dirzaaulia.footballclips.data.constants.BigLeaguesConstants
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
fun LeagueDropdownSelector(
    selectedLeagueId: Int? = null,
    selectedCompetitionId: String? = null,
    onLeagueSelected: (Int?, String?) -> Unit,
    modifier: Modifier = Modifier,
    dynamicCompetitions: List<BigLeague>? = null,
    showAll: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    val leagues = remember(dynamicCompetitions, showAll) {
        if (dynamicCompetitions != null && dynamicCompetitions.isNotEmpty()) {
            val list = mutableListOf<BigLeague>()
            if (showAll && dynamicCompetitions.none { it.leagueId == null && it.competitionId == null }) {
                list.add(
                    BigLeague(
                        name = "All Leagues",
                        leagueId = null,
                        countryCode = null,
                        competitionId = null,
                        logo = ""
                    )
                )
            }
            list.addAll(dynamicCompetitions.filter { if (showAll) true else (it.leagueId != null || it.competitionId != null) })
            list
        } else {
            if (showAll) BigLeaguesConstants.leagues
            else BigLeaguesConstants.leagues.filter { it.leagueId != null || it.competitionId != null }
        }
    }

    val currentLeague = remember(leagues, selectedLeagueId, selectedCompetitionId) {
        leagues.find { league ->
            if (selectedLeagueId != null) {
                league.leagueId == selectedLeagueId
            } else if (selectedCompetitionId != null) {
                league.competitionId == selectedCompetitionId
            } else {
                league.leagueId == null && league.competitionId == null
            }
        } ?: leagues.firstOrNull()
    }

    val arrowRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Box(modifier = modifier) {
        // Trigger Chip / Button
        Surface(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(20.dp),
            color = if (currentLeague?.competitionId != null || currentLeague?.leagueId != null) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (currentLeague?.competitionId != null || currentLeague?.leagueId != null) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }
            ),
            modifier = Modifier.height(36.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (currentLeague != null && currentLeague.logo.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = currentLeague.logo.toProxyUrl(),
                            contentDescription = currentLeague.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Icon(
                        imageVector = if (showAll) Icons.Default.Public else Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (currentLeague?.competitionId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = currentLeague?.name ?: if (showAll) "All Leagues" else "Select League",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (currentLeague?.competitionId != null || currentLeague?.leagueId != null) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand leagues",
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dropdown Menu with all leagues
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = 220.dp, max = 280.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            leagues.forEach { league ->
                val isSelected = if (selectedLeagueId != null) {
                    league.leagueId == selectedLeagueId
                } else if (selectedCompetitionId != null) {
                    league.competitionId == selectedCompetitionId
                } else {
                    league.leagueId == null && league.competitionId == null
                }

                DropdownMenuItem(
                    leadingIcon = {
                        if (league.logo.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = league.logo.toProxyUrl(),
                                    contentDescription = league.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (league.leagueId == null && league.competitionId == null) Icons.Default.Public else Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    text = {
                        Text(
                            text = league.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    onClick = {
                        onLeagueSelected(league.leagueId, league.competitionId)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
