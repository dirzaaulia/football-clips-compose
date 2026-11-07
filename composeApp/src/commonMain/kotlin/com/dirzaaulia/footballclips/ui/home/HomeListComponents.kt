package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.BigLeague
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.ui.components.card.WebHighlightCard
import com.dirzaaulia.footballclips.ui.components.filter.BigLeaguesQuickFilterBar
import com.dirzaaulia.footballclips.ui.score.components.MatchCard

@Composable
internal fun HighlightItemRow(
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
        is HighlightUiItem.OracleMatch -> {
            MatchCard(
                match = item.match,
                isSpoilerFree = isSpoilerFreeMode,
                isRevealed = isRevealed,
                onRevealClick = handleVideoClick,
                onWatchHighlightClick = { handleVideoClick() },
            )
        }

        is HighlightUiItem.Highlight -> {
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
                style = if (showExternalHighlights) NativeAdStyle.HIGHLIGHT else NativeAdStyle.MATCH,
            )
        }
    }
}

@Composable
internal fun HomeGridFilterBar(
    isAdsRemoved: Boolean,
    selectedLeagueId: Int?,
    competitions: List<BigLeague>,
    onLeagueSelected: (Int?, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isAdsRemoved) {
        BigLeaguesQuickFilterBar(
            selectedLeagueId = selectedLeagueId,
            dynamicCompetitions = competitions,
            onLeagueSelected = onLeagueSelected,
            modifier = modifier,
        )
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                BigLeaguesQuickFilterBar(
                    selectedLeagueId = selectedLeagueId,
                    dynamicCompetitions = competitions,
                    onLeagueSelected = onLeagueSelected,
                )
            }
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(140.dp),
                contentAlignment = Alignment.Center,
            ) {
                BannerAdItem(
                    style = NativeAdStyle.HIGHLIGHT,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
