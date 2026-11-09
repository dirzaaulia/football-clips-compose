package com.dirzaaulia.footballclips.ui.components.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.domain.model.Match

@Composable
internal fun VideoPlayerSheetScrollBody(
    videoId: String?,
    currentItem: HighlightUiItem?,
    currentMatchObj: Match?,
    effectiveHighlights: List<HighlightUiItem>,
    relatedFixturesFinished: List<HighlightUiItem>,
    relatedFixturesUpcoming: List<HighlightUiItem>,
    isSpoilerFree: Boolean,
    isMerchandiseEnabled: Boolean,
    isLive: Boolean,
    isRefreshing: Boolean,
    isSideSheet: Boolean,
    onRefreshMatch: () -> Unit,
    onItemClick: ((HighlightUiItem) -> Unit)?,
    onMinimize: (() -> Unit)?,
    onDismiss: (Boolean) -> Unit,
) {
    var isScoreRevealed by remember(isSpoilerFree, currentItem?.uniqueId) { mutableStateOf(!isSpoilerFree) }

    key(videoId) {
        Box(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState()),
            ) {
                MatchCentreTopBar(
                    isSideSheet = isSideSheet,
                    isLive = isLive,
                    currentMatchObj = currentMatchObj,
                    isRefreshing = isRefreshing,
                    onRefreshMatch = onRefreshMatch,
                    videoId = videoId,
                    onMinimize = onMinimize,
                    onDismiss = { onDismiss(false) },
                )

                VideoPlayerSheetHeroAndVideo(
                    videoId = videoId,
                    currentItem = currentItem,
                    isSpoilerFree = isSpoilerFree,
                    isScoreRevealed = isScoreRevealed,
                    onRevealClick = { isScoreRevealed = true },
                    onDismiss = onDismiss,
                )

                if (currentItem != null) {
                    VideoMetadata(
                        item = currentItem,
                        isSpoilerFree = isSpoilerFree,
                        isScoreRevealed = isScoreRevealed,
                        onRevealClick = { isScoreRevealed = true },
                        isMerchandiseEnabled = isMerchandiseEnabled,
                        hasVideo = videoId != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }

                PlayerRelatedContent(
                    effectiveHighlights = effectiveHighlights,
                    relatedFixturesFinished = relatedFixturesFinished,
                    relatedFixturesUpcoming = relatedFixturesUpcoming,
                    onItemClick = onItemClick,
                )

                Button(
                    onClick = { onDismiss(false) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                ) {
                    Text(if (isSideSheet) "Close Player" else "Back to Matches", fontWeight = FontWeight.Bold)
                }

                Spacer(
                    modifier = if (!isSideSheet) {
                        Modifier.navigationBarsPadding().height(16.dp)
                    } else {
                        Modifier.height(16.dp)
                    },
                )
            }
        }
    }
}

@Composable
internal fun VideoPlayerSheetHeroAndVideo(
    videoId: String?,
    currentItem: HighlightUiItem?,
    isSpoilerFree: Boolean,
    isScoreRevealed: Boolean,
    onRevealClick: () -> Unit,
    onDismiss: (Boolean) -> Unit,
) {
    if (videoId != null) {
        CatalogVideoPlayer(currentItem, videoId, onDismiss)
    } else if (currentItem is HighlightUiItem.OracleMatch) {
        MatchCentreHero(
            match = currentItem.match,
            isSpoilerFree = isSpoilerFree,
            isScoreRevealed = isScoreRevealed,
            onRevealClick = onRevealClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        )
    }

    if (isSpoilerFree && !isScoreRevealed && (currentItem as? HighlightUiItem.OracleMatch)?.match?.isFinished == true) {
        MatchSpoilerNoticeCard(
            onRevealClick = onRevealClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        )
    }
}
