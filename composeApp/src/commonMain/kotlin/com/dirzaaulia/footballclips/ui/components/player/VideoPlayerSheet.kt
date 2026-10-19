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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.common.ModalSideSheet
import com.dirzaaulia.footballclips.ui.player.YouTubePlayerView
import com.dirzaaulia.footballclips.util.extractVideoId

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.repository.ScoreRepository
import com.dirzaaulia.footballclips.domain.model.toMatch
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.util.isWasmTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerSheet(
    html: String? = null,
    item: HighlightUiItem?,
    relatedHighlights: List<HighlightUiItem> = emptyList(),
    relatedFixturesFinished: List<HighlightUiItem> = emptyList(),
    relatedFixturesUpcoming: List<HighlightUiItem> = emptyList(),
    relatedItems: List<HighlightUiItem> = emptyList(),
    isSpoilerFree: Boolean = false,
    onItemClick: ((HighlightUiItem) -> Unit)? = null,
    onMinimize: (() -> Unit)? = null,
    onDismiss: (Boolean) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val videoId = html?.let { extractVideoId(it) }
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
                isSpoilerFree = isSpoilerFree,
                onItemClick = onItemClick,
                onMinimize = onMinimize,
                isSideSheet = true,
                onDismiss = onDismiss
            )
        }
    } else {
        Dialog(
            onDismissRequest = { onDismiss(false) },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.background
            ) {
                VideoPlayerSheetContent(
                    videoId = videoId,
                    item = item,
                    relatedHighlights = relatedHighlights,
                    relatedFixturesFinished = relatedFixturesFinished,
                    relatedFixturesUpcoming = relatedFixturesUpcoming,
                    relatedItems = relatedItems,
                    isSpoilerFree = isSpoilerFree,
                    onItemClick = onItemClick,
                    onMinimize = onMinimize,
                    isSideSheet = false,
                    onDismiss = onDismiss
                )
            }
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
    isSpoilerFree: Boolean,
    onItemClick: ((HighlightUiItem) -> Unit)?,
    onMinimize: (() -> Unit)?,
    isSideSheet: Boolean,
    onDismiss: (Boolean) -> Unit
) {
    val effectiveHighlights = if (relatedHighlights.isNotEmpty()) relatedHighlights else relatedItems
    val totalFixturesCount = relatedFixturesFinished.size + relatedFixturesUpcoming.size
    var selectedTab by remember { mutableStateOf(0) }
    var fixtureSubTab by remember { mutableStateOf(0) } // 0: Results, 1: Upcoming

    val scoreRepository: ScoreRepository = koinInject()
    var currentItem by remember(item) { mutableStateOf(item) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val refreshMatch: () -> Unit = {
        val currentMatch = (currentItem as? HighlightUiItem.SupabaseMatch)?.match
        if (currentMatch != null && !isRefreshing) {
            coroutineScope.launch {
                isRefreshing = true
                when (val res = scoreRepository.getMatch(currentMatch.id)) {
                    is NetworkResult.Success -> {
                        val updatedMatch = res.data.toMatch()
                        currentItem = HighlightUiItem.SupabaseMatch(match = updatedMatch)
                    }
                    else -> {}
                }
                isRefreshing = false
            }
        }
    }

    // Auto-refresh interval (20s) for live in-play matches
    val currentMatchObj = (currentItem as? HighlightUiItem.SupabaseMatch)?.match
    val isLive = currentMatchObj?.isLive == true
    LaunchedEffect(currentItem?.uniqueId, isLive) {
        if (isLive) {
            while (isActive) {
                delay(20_000)
                refreshMatch()
            }
        }
    }

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isSideSheet) 20.dp else 12.dp, vertical = if (isSideSheet) 12.dp else 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isSideSheet) {
                            IconButton(onClick = { onDismiss(false) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                        Text(
                            "Match Centre",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isLive) {
                            Surface(
                                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(Color(0xFFEF4444))
                                    )
                                    Text(
                                        text = "LIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Manual Refresh Action Button
                        if (currentMatchObj != null) {
                            IconButton(
                                onClick = refreshMatch,
                                enabled = !isRefreshing
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Match Data",
                                    tint = if (isLive) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (onMinimize != null && videoId != null) {
                            IconButton(onClick = onMinimize) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Minimize Player",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (isSideSheet) {
                            IconButton(onClick = { onDismiss(false) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }
                }

                if (videoId != null) {
                    val backupVideoId = when (currentItem) {
                        is HighlightUiItem.SupabaseMatch -> (currentItem as HighlightUiItem.SupabaseMatch).match.backupHighlightVideoId
                        else -> null
                    }
                    val regionInfo = when (currentItem) {
                        is HighlightUiItem.SupabaseMatch -> (currentItem as HighlightUiItem.SupabaseMatch).match.youtubeRegionInfo
                        else -> null
                    }
                    val backupRegionInfo = when (currentItem) {
                        is HighlightUiItem.SupabaseMatch -> (currentItem as HighlightUiItem.SupabaseMatch).match.backupYoutubeRegionInfo
                        else -> null
                    }
                    YouTubePlayerView(
                        videoId = videoId,
                        backupVideoId = backupVideoId,
                        regionInfo = regionInfo,
                        backupRegionInfo = backupRegionInfo,
                        onDismiss = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    )
                } else if (currentItem is HighlightUiItem.SupabaseMatch) {
                    MatchCentreHero(
                        match = (currentItem as HighlightUiItem.SupabaseMatch).match,
                        isSpoilerFree = isSpoilerFree,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                if (currentItem != null) {
                    VideoMetadata(
                        item = currentItem!!,
                        isSpoilerFree = isSpoilerFree,
                        hasVideo = videoId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }

                PlayerRelatedContent(
                    effectiveHighlights = effectiveHighlights,
                    relatedFixturesFinished = relatedFixturesFinished,
                    relatedFixturesUpcoming = relatedFixturesUpcoming,
                    onItemClick = onItemClick
                )

                Button(
                    onClick = { onDismiss(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(if (isSideSheet) "Close Player" else "Back to Matches", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
