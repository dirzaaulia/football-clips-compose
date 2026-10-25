package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.data.repository.OracleVmRepository
import com.dirzaaulia.footballclips.domain.model.toMatch
import com.dirzaaulia.footballclips.ui.adaptive.LocalIsBigScreen
import com.dirzaaulia.footballclips.ui.components.common.ModalSideSheet
import com.dirzaaulia.footballclips.util.extractVideoId
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
    isMerchandiseEnabled: Boolean = true,
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
                isMerchandiseEnabled = isMerchandiseEnabled,
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
                    isMerchandiseEnabled = isMerchandiseEnabled,
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
    isMerchandiseEnabled: Boolean,
    onItemClick: ((HighlightUiItem) -> Unit)?,
    onMinimize: (() -> Unit)?,
    isSideSheet: Boolean,
    onDismiss: (Boolean) -> Unit
) {
    val effectiveHighlights = if (relatedHighlights.isNotEmpty()) relatedHighlights else relatedItems
    val totalFixturesCount = relatedFixturesFinished.size + relatedFixturesUpcoming.size
    var selectedTab by remember { mutableStateOf(0) }
    var fixtureSubTab by remember { mutableStateOf(0) } // 0: Results, 1: Upcoming

    val oracleVmRepository: OracleVmRepository = koinInject()
    var currentItem by remember(item) { mutableStateOf(item) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val refreshMatch: () -> Unit = {
        val currentMatch = (currentItem as? HighlightUiItem.OracleMatch)?.match
        if (currentMatch != null && !isRefreshing) {
            coroutineScope.launch {
                isRefreshing = true
                when (val res = oracleVmRepository.getMatch(currentMatch.id)) {
                    is NetworkResult.Success -> {
                        val updatedMatch = res.data.toMatch()
                        currentItem = HighlightUiItem.OracleMatch(match = updatedMatch)
                    }
                    else -> {}
                }
                isRefreshing = false
            }
        }
    }

    // Auto-refresh interval (20s) for live in-play matches
    val currentMatchObj = (currentItem as? HighlightUiItem.OracleMatch)?.match
    val isLive = currentMatchObj?.isLive == true
    LaunchedEffect(currentItem?.uniqueId, isLive) {
        if (isLive) {
            while (isActive) {
                delay(20_000)
                refreshMatch()
            }
        }
    }

    var isScoreRevealed by remember(isSpoilerFree, currentItem?.uniqueId) { mutableStateOf(!isSpoilerFree) }

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
                MatchCentreTopBar(
                    isSideSheet = isSideSheet,
                    isLive = isLive,
                    currentMatchObj = currentMatchObj,
                    isRefreshing = isRefreshing,
                    onRefreshMatch = refreshMatch,
                    videoId = videoId,
                    onMinimize = onMinimize,
                    onDismiss = { onDismiss(false) },
                )

                if (videoId != null) {
                    CatalogVideoPlayer(currentItem, videoId, onDismiss)

                } else if (currentItem is HighlightUiItem.OracleMatch) {
                    MatchCentreHero(
                        match = (currentItem as HighlightUiItem.OracleMatch).match,
                        isSpoilerFree = isSpoilerFree,
                        isScoreRevealed = isScoreRevealed,
                        onRevealClick = { isScoreRevealed = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                if (isSpoilerFree && !isScoreRevealed && (currentItem as? HighlightUiItem.OracleMatch)?.match?.isFinished == true) {
                    MatchSpoilerNoticeCard(
                        onRevealClick = { isScoreRevealed = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }

                if (currentItem != null) {
                    VideoMetadata(
                        item = currentItem!!,
                        isSpoilerFree = isSpoilerFree,
                        isScoreRevealed = isScoreRevealed,
                        onRevealClick = { isScoreRevealed = true },
                        isMerchandiseEnabled = isMerchandiseEnabled,
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
