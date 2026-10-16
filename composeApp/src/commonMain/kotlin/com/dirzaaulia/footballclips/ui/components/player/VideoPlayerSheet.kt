package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.common.ModalSideSheet
import com.dirzaaulia.footballclips.ui.player.YouTubePlayerView
import com.dirzaaulia.footballclips.util.extractVideoId

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.util.isWasmTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerSheet(
    html: String,
    item: HighlightUiItem?,
    relatedHighlights: List<HighlightUiItem> = emptyList(),
    relatedFixturesFinished: List<HighlightUiItem> = emptyList(),
    relatedFixturesUpcoming: List<HighlightUiItem> = emptyList(),
    relatedItems: List<HighlightUiItem> = emptyList(),
    onItemClick: ((HighlightUiItem) -> Unit)? = null,
    onDismiss: (Boolean) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val videoId = extractVideoId(html)
    val isBigScreen = LocalIsBigScreen.current
    val shouldUseSideSheet = isWasmTarget && isBigScreen

    if (shouldUseSideSheet) {
        ModalSideSheet(
            onDismissRequest = { onDismiss(false) },
            sheetWidth = 580.dp
        ) {
            VideoPlayerSheetContent(
                videoId = videoId,
                item = item,
                relatedHighlights = relatedHighlights,
                relatedFixturesFinished = relatedFixturesFinished,
                relatedFixturesUpcoming = relatedFixturesUpcoming,
                relatedItems = relatedItems,
                onItemClick = onItemClick,
                isSideSheet = true,
                onDismiss = onDismiss
            )
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = { onDismiss(false) },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            VideoPlayerSheetContent(
                videoId = videoId,
                item = item,
                relatedHighlights = relatedHighlights,
                relatedFixturesFinished = relatedFixturesFinished,
                relatedFixturesUpcoming = relatedFixturesUpcoming,
                relatedItems = relatedItems,
                onItemClick = onItemClick,
                isSideSheet = false,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun VideoPlayerSheetContent(
    videoId: String?,
    item: HighlightUiItem?,
    relatedHighlights: List<HighlightUiItem>,
    relatedFixturesFinished: List<HighlightUiItem>,
    relatedFixturesUpcoming: List<HighlightUiItem>,
    relatedItems: List<HighlightUiItem>,
    onItemClick: ((HighlightUiItem) -> Unit)?,
    isSideSheet: Boolean,
    onDismiss: (Boolean) -> Unit
) {
    val effectiveHighlights = if (relatedHighlights.isNotEmpty()) relatedHighlights else relatedItems
    val totalFixturesCount = relatedFixturesFinished.size + relatedFixturesUpcoming.size
    var selectedTab by remember { mutableStateOf(0) }
    var fixtureSubTab by remember { mutableStateOf(0) } // 0: Results, 1: Upcoming

    key(videoId) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState())
                    .then(if (!isSideSheet) Modifier.navigationBarsPadding() else Modifier)
                    .padding(bottom = 16.dp)
            ) {
                if (isSideSheet) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Match Highlight",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { onDismiss(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                if (videoId != null) {
                    YouTubePlayerView(
                        videoId = videoId,
                        onDismiss = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    )
                }

                if (item != null) {
                    VideoMetadata(
                        item = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }

                if (effectiveHighlights.isNotEmpty() || totalFixturesCount > 0) {
                    val tabs = listOf(
                        "HIGHLIGHTS (${effectiveHighlights.size})",
                        "FIXTURES ($totalFixturesCount)"
                    )

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
                                            "RESULTS (${relatedFixturesFinished.size})",
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
                                            "UPCOMING (${relatedFixturesUpcoming.size})",
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
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = { onDismiss(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text("Close Player", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
