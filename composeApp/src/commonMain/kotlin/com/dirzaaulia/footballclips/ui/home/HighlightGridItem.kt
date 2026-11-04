package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.ui.components.card.WebHighlightCard

@Composable
internal fun HighlightGridItem(
    item: HighlightUiItem,
    revealedItemIds: Set<String>,
    isSpoilerFreeMode: Boolean,
    showExternalHighlights: Boolean,
    viewModel: HomeViewModel,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
) {
    val isRevealed = revealedItemIds.contains(item.uniqueId)
    val handleVideoClick: () -> Unit = {
        onVideoClick(item) { isJumping ->
            viewModel.setPendingInterstitial(true)
            if (!isJumping) viewModel.consumePendingInterstitial()
        }
    }

    when (item) {
        is HighlightUiItem.OracleMatch, is HighlightUiItem.Highlight -> {
            WebHighlightCard(
                item = item,
                isSpoilerFree = isSpoilerFreeMode,
                isRevealed = isRevealed,
                onRevealClick = handleVideoClick,
                onClick = { handleVideoClick() },
            )
        }

        is HighlightUiItem.BannerAd -> {
            BannerAdItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f),
                style = if (showExternalHighlights) NativeAdStyle.HIGHLIGHT else NativeAdStyle.MATCH,
            )
        }
    }
}
