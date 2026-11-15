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

@Composable
internal fun CompetitionHighlightsRow(
    displayMatches: List<HighlightUiItem>,
    league: BigLeague,
    compName: String,
    emblem: String,
    isAdsRemoved: Boolean,
    selectedSeason: Int,
    viewModel: HomeViewModel,
    cardWidth: androidx.compose.ui.unit.Dp = 280.dp,
    modifier: Modifier = Modifier,
    renderItem: @Composable (HighlightUiItem) -> Unit,
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp),
    ) {
        if (displayMatches.isNotEmpty()) {
            displayMatches.forEachIndexed { mIdx, item ->
                item(key = item.uniqueId) {
                    Box(modifier = Modifier.width(cardWidth)) {
                        renderItem(item)
                    }
                }
                if (!isAdsRemoved && (mIdx + 1) % 4 == 0) {
                    item(key = "ad-${league.id}-$mIdx") {
                        Box(modifier = Modifier.width(cardWidth)) {
                            BannerAdItem(
                                modifier = Modifier.fillMaxWidth(),
                                style = NativeAdStyle.HIGHLIGHT,
                            )
                        }
                    }
                }
            }
            item(key = "see-all-${league.id}") {
                Box(modifier = Modifier.width(200.dp)) {
                    HomeSeeAllCompetitionCard(
                        competitionName = compName,
                        competitionEmblemUrl = emblem,
                        onSeeAll = {
                            if (league.leagueId != null) {
                                viewModel.selectLeague(league.leagueId, league.competitionId)
                            } else {
                                viewModel.selectCompetitionByName(compName)
                            }
                        },
                    )
                }
            }
        } else {
            item(key = "empty-${league.id}") {
                UpcomingCompetitionCard(
                    league = league,
                    selectedSeason = selectedSeason,
                    onSeeAll = {
                        if (league.leagueId != null) {
                            viewModel.selectLeague(league.leagueId, league.competitionId)
                        } else {
                            viewModel.selectCompetitionByName(compName)
                        }
                    },
                    onSwitchSeason = { viewModel.setSeason(it) },
                )
            }
        }
    }
}
