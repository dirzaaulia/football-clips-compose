package com.dirzaaulia.footballclips.ui.score.companion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.PitchPlay

enum class PitchViewMode { REPLAY, SHOTS, HEATMAP }

@Composable
fun PitchRadarDeck(
    plays: List<PitchPlay>,
    homeColor: Color? = null,
    awayColor: Color? = null,
    modifier: Modifier = Modifier,
    onPlaySelected: ((PitchPlay) -> Unit)? = null,
) {
    var selectedMode by remember { mutableStateOf(PitchViewMode.REPLAY) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    val filteredPlays = remember(plays, selectedCategory) {
        filterPlaysByCategory(plays, selectedCategory)
    }
    var selectedPlay by remember(filteredPlays) { mutableStateOf(filteredPlays.lastOrNull()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PitchModeSelector(
            selectedMode = selectedMode,
            onSelectMode = { selectedMode = it },
        )

        if (selectedMode == PitchViewMode.REPLAY) {
            PitchCategoryFilters(
                selectedCategory = selectedCategory,
                onSelect = { selectedCategory = it },
            )
        }

        PitchCanvas(
            plays = filteredPlays,
            selectedPlay = selectedPlay,
            homeColor = homeColor,
            awayColor = awayColor,
            onSelectPlay = {
                selectedPlay = it
                onPlaySelected?.invoke(it)
            },
            viewMode = selectedMode,
            modifier = Modifier.fillMaxWidth().aspectRatio(1.54f),
        )

        selectedPlay?.let { play ->
            PitchActionBanner(play = play)
        }

        if (selectedMode == PitchViewMode.REPLAY) {
            PitchEventChips(
                plays = filteredPlays,
                selectedPlay = selectedPlay,
                onSelect = {
                    selectedPlay = it
                    onPlaySelected?.invoke(it)
                },
            )
        }
    }
}

@Composable
private fun PitchModeSelector(
    selectedMode: PitchViewMode,
    onSelectMode: (PitchViewMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PitchViewMode.entries.forEach { mode ->
            val label = when (mode) {
                PitchViewMode.REPLAY -> "2D Replay"
                PitchViewMode.SHOTS -> "Shot Map"
                PitchViewMode.HEATMAP -> "Heatmap"
            }
            val isSelected = mode == selectedMode
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
                modifier = Modifier.weight(1f).clickable { onSelectMode(mode) },
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
        }
    }
}

private fun filterPlaysByCategory(plays: List<PitchPlay>, cat: String): List<PitchPlay> {
    if (cat == "ALL") return plays
    return plays.filter { p ->
        when (cat) {
            "GOALS" -> p.isGoal
            "SHOTS" -> p.isShot
            "CARDS" -> p.isCard
            "CORNERS" -> p.isCorner
            "SUBS" -> p.isSub
            else -> true
        }
    }
}

@Composable
private fun PitchCategoryFilters(
    selectedCategory: String,
    onSelect: (String) -> Unit,
) {
    val categories = listOf("ALL", "GOALS", "SHOTS", "CARDS", "CORNERS", "SUBS")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 2.dp),
    ) {
        items(categories) { cat ->
            val isSelected = cat == selectedCategory
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
                modifier = Modifier.clickable { onSelect(cat) },
            ) {
                Text(
                    text = cat,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun PitchActionBanner(play: PitchPlay) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = play.clock,
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = play.text.ifBlank { "${play.teamName.orEmpty()} ${play.typeText}" },
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun PitchEventChips(
    plays: List<PitchPlay>,
    selectedPlay: PitchPlay?,
    onSelect: (PitchPlay) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(plays.takeLast(12).reversed()) { play ->
            val isSelected = play.id == selectedPlay?.id
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
                modifier = Modifier.clickable { onSelect(play) },
            ) {
                Text(
                    text = "${play.clock} ${play.typeText.take(6)}",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}
