package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId

@Composable
internal fun PlayerRelatedContent(
    effectiveHighlights: List<HighlightUiItem>,
    relatedFixturesFinished: List<HighlightUiItem>,
    relatedFixturesUpcoming: List<HighlightUiItem>,
    onItemClick: ((HighlightUiItem) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val totalFixturesCount = relatedFixturesFinished.size + relatedFixturesUpcoming.size
    if (effectiveHighlights.isEmpty() && totalFixturesCount == 0) {
        Spacer(modifier = Modifier.height(16.dp))
        return
    }

    var selectedTab by remember { mutableStateOf(0) }
    var fixtureSubTab by remember { mutableStateOf(0) } // 0: Results, 1: Upcoming

    val tabs = listOf("HIGHLIGHTS", "FIXTURES")

    Column(modifier = modifier.fillMaxWidth()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            tabs.forEachIndexed { index, titleText ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            titleText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            if (effectiveHighlights.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    items(
                        items = effectiveHighlights,
                        key = { it.uniqueId }
                    ) { relatedItem ->
                        RelatedHighlightItem(
                            item = relatedItem,
                            onClick = { onItemClick?.invoke(relatedItem) }
                        )
                    }
                }
            } else {
                Text(
                    text = "No other highlights found.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = fixtureSubTab == 0,
                        onClick = { fixtureSubTab = 0 },
                        label = {
                            Text(
                                "RESULTS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    FilterChip(
                        selected = fixtureSubTab == 1,
                        onClick = { fixtureSubTab = 1 },
                        label = {
                            Text(
                                "UPCOMING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }

                val currentFixtures = if (fixtureSubTab == 0) relatedFixturesFinished else relatedFixturesUpcoming
                if (currentFixtures.isEmpty()) {
                    Text(
                        text = if (fixtureSubTab == 0) "No finished results found." else "No scheduled fixtures found.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        items(
                            items = currentFixtures,
                            key = { it.uniqueId }
                        ) { fixtureItem ->
                            if (fixtureItem is HighlightUiItem.SupabaseMatch) {
                                RelatedFixtureItem(
                                    item = fixtureItem,
                                    onClick = {
                                        if (!fixtureItem.match.highlightVideoId.isNullOrBlank()) {
                                            onItemClick?.invoke(fixtureItem)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
