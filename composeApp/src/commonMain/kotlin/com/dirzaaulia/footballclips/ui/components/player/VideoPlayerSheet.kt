package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val videoId = html?.let { extractVideoId(it) }
    val isBigScreen = LocalIsBigScreen.current
    val shouldUseSideSheet = isWasmTarget && isBigScreen

    if (shouldUseSideSheet) {
        ModalSideSheet(
            onDismissRequest = { onDismiss(false) },
            sheetWidth = 580.dp,
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
                onDismiss = onDismiss,
            )
        }
    } else {
        Dialog(
            onDismissRequest = { onDismiss(false) },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                decorFitsSystemWindows = false,
            ),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
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
                    onDismiss = onDismiss,
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
    onDismiss: (Boolean) -> Unit,
) {
    val effectiveHighlights = if (relatedHighlights.isNotEmpty()) relatedHighlights else relatedItems
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
                        currentItem = HighlightUiItem.OracleMatch(match = res.data.toMatch())
                    }
                    else -> {}
                }
                isRefreshing = false
            }
        }
    }

    val currentMatchObj = (currentItem as? HighlightUiItem.OracleMatch)?.match
    val isLive = currentMatchObj?.isLive == true
    LaunchedEffect(currentItem?.uniqueId, isLive) {
        if (currentMatchObj != null && (currentMatchObj.formations == null || currentMatchObj.statistics == null)) {
            refreshMatch()
        }
        if (isLive) {
            while (isActive) {
                delay(20_000)
                refreshMatch()
            }
        }
    }

    VideoPlayerSheetScrollBody(
        videoId = videoId,
        currentItem = currentItem,
        currentMatchObj = currentMatchObj,
        effectiveHighlights = effectiveHighlights,
        relatedFixturesFinished = relatedFixturesFinished,
        relatedFixturesUpcoming = relatedFixturesUpcoming,
        isSpoilerFree = isSpoilerFree,
        isMerchandiseEnabled = isMerchandiseEnabled,
        isLive = isLive,
        isRefreshing = isRefreshing,
        isSideSheet = isSideSheet,
        onRefreshMatch = refreshMatch,
        onItemClick = onItemClick,
        onMinimize = onMinimize,
        onDismiss = onDismiss,
    )
}
