package com.dirzaaulia.footballclips.ui.components.filter

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
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
        val comps = dynamicCompetitions ?: emptyList()
        buildList {
            if (showAll && comps.none { it.leagueId == null && it.competitionId.isEmpty() }) {
                add(BigLeague(id = "", name = "All Competitions"))
            }
            addAll(comps.filter { showAll || it.leagueId != null || it.competitionId.isNotEmpty() })
        }
    }

    val currentLeague = remember(leagues, selectedLeagueId, selectedCompetitionId) {
        leagues.find {
            when {
                selectedLeagueId != null -> it.leagueId == selectedLeagueId
                selectedCompetitionId != null -> it.competitionId.equals(selectedCompetitionId, ignoreCase = true)
                else -> it.leagueId == null && it.competitionId.isEmpty()
            }
        } ?: leagues.firstOrNull()
    }

    val arrowRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    val isFullWidth = modifier != Modifier
    val hasSelection = currentLeague?.competitionId?.isNotEmpty() == true || currentLeague?.leagueId != null

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(20.dp),
            color = if (hasSelection) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, if (hasSelection) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.height(36.dp).then(if (isFullWidth) Modifier.fillMaxWidth() else Modifier),
        ) {
            Row(
                modifier = Modifier
                    .then(if (isFullWidth) Modifier.fillMaxWidth() else Modifier)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isFullWidth) Arrangement.SpaceBetween else Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = if (isFullWidth) Modifier.weight(1f, fill = false) else Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (currentLeague != null && currentLeague.logo.isNotEmpty()) {
                        GlassLogoBadge(
                            modifier = Modifier.size(22.dp),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            AsyncImage(
                                model = currentLeague.logo.toProxyUrl(),
                                contentDescription = currentLeague.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (showAll) Icons.Default.Public else Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (hasSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = currentLeague?.name ?: if (showAll) "All Competitions" else "Select League",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (hasSelection) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand leagues",
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (expanded) {
            CompetitionFilterSheet(
                leagues = leagues,
                selectedLeagueId = selectedLeagueId,
                selectedCompetitionId = selectedCompetitionId,
                onLeagueSelected = onLeagueSelected,
                onDismiss = { expanded = false },
                showAll = showAll,
            )
        }
    }
}
