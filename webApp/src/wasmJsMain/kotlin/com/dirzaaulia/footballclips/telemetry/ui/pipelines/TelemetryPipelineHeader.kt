package com.dirzaaulia.footballclips.telemetry.ui.pipelines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.telemetry.model.PipelineCategory
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchBorder
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSecondary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurface
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchSurfaceContainerHigh
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextMuted
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextPrimary
import com.dirzaaulia.footballclips.telemetry.ui.theme.StitchTextSecondary

@Composable
fun PipelineHeaderSection(
    eventCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Clip & Match Pipeline Execution Monitor",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StitchTextPrimary,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = "Granular step-by-step trace of YouTube scraping, AI reranking, fixture ingestion, and 30-day retention purges.",
                fontSize = 10.sp,
                color = StitchTextMuted,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(StitchPrimary.copy(alpha = 0.15f))
                .border(1.dp, StitchPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text("$eventCount Pipeline Events", fontSize = 9.sp, color = StitchPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun PipelineCategoryTabs(
    selectedCategory: PipelineCategory,
    onCategorySelect: (PipelineCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        PipelineCategory.entries.forEach { cat ->
            val isSelected = selectedCategory == cat
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) StitchPrimary.copy(alpha = 0.15f) else StitchSurface)
                    .border(1.dp, if (isSelected) StitchPrimary else StitchBorder, RoundedCornerShape(6.dp))
                    .clickable { onCategorySelect(cat) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = cat.label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) StitchPrimary else StitchTextSecondary,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
fun PipelineSearchField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(260.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(StitchSurface)
            .border(1.dp, StitchBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp),
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Filter fixture or club...", fontSize = 11.sp, color = StitchTextMuted) },
            singleLine = true,
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = StitchTextMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onSearchQueryChange("") },
                    )
                }
            } else null,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = StitchTextPrimary,
                unfocusedTextColor = StitchTextPrimary,
            ),
            textStyle = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
        )
    }
}

@Composable
fun PipelineLeagueFilterRow(
    leagues: List<String>,
    selectedLeague: String,
    onLeagueSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(leagues, key = { it }) { l ->
            val isSel = selectedLeague == l
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSel) StitchSecondary.copy(alpha = 0.15f) else StitchSurfaceContainerHigh)
                    .border(1.dp, if (isSel) StitchSecondary else StitchBorder, RoundedCornerShape(4.dp))
                    .clickable { onLeagueSelect(l) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(l, fontSize = 9.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) StitchSecondary else StitchTextMuted, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
