package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.player.FloatingMiniPlayer
import com.dirzaaulia.footballclips.ui.components.player.VideoPlayerSheet
import com.dirzaaulia.footballclips.ui.home.HomeState
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.home.selectVideo
import com.dirzaaulia.footballclips.ui.score.ScoreState
import com.dirzaaulia.footballclips.ui.score.ScoreViewModel
import com.dirzaaulia.footballclips.util.extractVideoId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BoxScope.AdaptiveVideoOverlay(
    videoState: MobileVideoPlayerState,
    homeViewModel: HomeViewModel,
    scoreViewModel: ScoreViewModel,
    isLandscape: Boolean,
    bottomPadding: Dp,
) {
    if (!videoState.showVideoPlayer || videoState.selectedItem == null) return

    val isSpoilerFreeMode by scoreViewModel.isSpoilerFreeMode.collectAsState()
    val isMerchandiseEnabled by homeViewModel.isMerchandiseEnabled.collectAsState()

    if (videoState.isMinimized && videoState.selectedVideoHtml != null) {
        val miniVideoId = extractVideoId(videoState.selectedVideoHtml!!)
        if (miniVideoId != null) {
            FloatingMiniPlayer(
                item = videoState.selectedItem,
                videoId = miniVideoId,
                isSpoilerFree = isSpoilerFreeMode,
                onRestore = { videoState.restore() },
                onClose = { videoState.onDismiss(false) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = if (isLandscape) 16.dp else bottomPadding + 80.dp,
                    ),
            )
        }
    } else {
        val selectedItem = videoState.selectedItem
        val relatedHighlights by homeViewModel.relatedHighlights.collectAsState()
        val relatedFixturesFinished by homeViewModel.relatedFixturesFinished.collectAsState()
        val relatedFixturesUpcoming by homeViewModel.relatedFixturesUpcoming.collectAsState()

        val relatedItems = remember(selectedItem) {
            if (selectedItem == null) return@remember emptyList()
            val leagueName = when (selectedItem) {
                is HighlightUiItem.OracleMatch -> selectedItem.match.competitionName
                is HighlightUiItem.Highlight -> selectedItem.highlight.leagueName
                else -> ""
            }
            val h = (homeViewModel.uiState.value as? HomeState.Success)?.items ?: emptyList()
            val s = (scoreViewModel.uiState.value as? ScoreState.Success)?.matches ?: emptyList()
            val allItems: List<HighlightUiItem> = h + s
            allItems.distinctBy {
                when (it) {
                    is HighlightUiItem.OracleMatch -> it.match.id.toString()
                    is HighlightUiItem.Highlight -> it.highlight.title
                    is HighlightUiItem.BannerAd -> it.hashCode().toString()
                }
            }.filter {
                it != selectedItem &&
                    (it is HighlightUiItem.OracleMatch && it.match.competitionName == leagueName ||
                        it is HighlightUiItem.Highlight && it.highlight.leagueName == leagueName) &&
                    (it is HighlightUiItem.OracleMatch && it.match.highlightVideoId != null ||
                        it is HighlightUiItem.Highlight && it.highlight.embedHtml.isNotEmpty())
            }.take(10)
        }

        VideoPlayerSheet(
            item = selectedItem,
            html = videoState.selectedVideoHtml,
            relatedHighlights = relatedHighlights,
            relatedFixturesFinished = relatedFixturesFinished,
            relatedFixturesUpcoming = relatedFixturesUpcoming,
            relatedItems = relatedItems,
            isSpoilerFree = isSpoilerFreeMode,
            isMerchandiseEnabled = isMerchandiseEnabled,
            onItemClick = { item ->
                homeViewModel.selectVideo(item)
                videoState.onVideoClick(item) {}
            },
            onMinimize = { videoState.minimize() },
            onDismiss = { isExternal -> videoState.onDismiss(isExternal) },
        )
    }
}
