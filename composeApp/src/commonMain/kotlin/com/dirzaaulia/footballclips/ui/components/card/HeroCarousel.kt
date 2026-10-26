package com.dirzaaulia.footballclips.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdView
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.util.isWasmTarget
import kotlinx.coroutines.delay

@Composable
fun HeroCarousel(
    items: List<HighlightUiItem>,
    isAdsRemoved: Boolean,
    onVideoClick: (HighlightUiItem) -> Unit,
    modifier: Modifier = Modifier,
    isCinematic: Boolean = false,
    isLandscape: Boolean = false,
    isSpoilerFreeMode: Boolean = false,
    revealedItemIds: Set<String> = emptySet(),
    onRevealItem: ((String) -> Unit)? = null,
) {
    if (items.isEmpty()) return

    val isWasmWeb = isWasmTarget
    val carouselPages = remember(items, isAdsRemoved, isWasmWeb) {
        arrangeExcitementCarouselPages(items, isAdsRemoved, isWasmWeb)
    }

    if (carouselPages.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { carouselPages.size })

    LaunchedEffect(pagerState, carouselPages.size) {
        if (carouselPages.size > 1) {
            while (true) {
                delay(5000L)
                val nextPage = (pagerState.currentPage + 1) % carouselPages.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isWasmWeb && !isAdsRemoved) {
            HeroWasmDualPane(
                carouselPages = carouselPages,
                isCinematic = isCinematic,
                isSpoilerFreeMode = isSpoilerFreeMode,
                revealedItemIds = revealedItemIds,
                onVideoClick = onVideoClick,
            )
        } else {
            HeroCarouselPagerContent(
                pagerState = pagerState,
                carouselPages = carouselPages,
                isCinematic = isCinematic,
                isLandscape = isLandscape,
                isSpoilerFreeMode = isSpoilerFreeMode,
                revealedItemIds = revealedItemIds,
                onVideoClick = onVideoClick,
            )

            if (carouselPages.size > 1) {
                Spacer(Modifier.height(if (isLandscape) 4.dp else 10.dp))
                HeroCarouselIndicators(
                    pagerState = pagerState,
                    pageCount = carouselPages.size,
                    isLandscape = isLandscape,
                )
            }
        }
    }
}

@Composable
private fun HeroWasmDualPane(
    carouselPages: List<Pair<HighlightUiItem, String>>,
    isCinematic: Boolean,
    isSpoilerFreeMode: Boolean,
    revealedItemIds: Set<String>,
    onVideoClick: (HighlightUiItem) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val (featuredItem, featuredTag) = carouselPages.firstOrNull() ?: return@Row
        Box(modifier = Modifier.weight(0.6f)) {
            HeroCard(
                item = featuredItem,
                onClick = { onVideoClick(featuredItem) },
                isCinematic = isCinematic,
                isLandscape = false,
                isSpoilerFree = isSpoilerFreeMode,
                isRevealed = revealedItemIds.contains(featuredItem.uniqueId),
                onRevealClick = { onVideoClick(featuredItem) },
                excitementTag = featuredTag.ifEmpty { null },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
            HeroAdCard(
                isCinematic = isCinematic,
                isLandscape = false,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun HeroCarouselPagerContent(
    pagerState: PagerState,
    carouselPages: List<Pair<HighlightUiItem, String>>,
    isCinematic: Boolean,
    isLandscape: Boolean,
    isSpoilerFreeMode: Boolean,
    revealedItemIds: Set<String>,
    onVideoClick: (HighlightUiItem) -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        pageSpacing = 16.dp,
    ) { page ->
        val (pageItem, excitementTag) = carouselPages[page]
        when (pageItem) {
            is HighlightUiItem.BannerAd -> {
                HeroAdCard(
                    isCinematic = isCinematic,
                    isLandscape = isLandscape,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            else -> {
                HeroCard(
                    item = pageItem,
                    onClick = { onVideoClick(pageItem) },
                    isCinematic = isCinematic,
                    isLandscape = isLandscape,
                    isSpoilerFree = isSpoilerFreeMode,
                    isRevealed = revealedItemIds.contains(pageItem.uniqueId),
                    onRevealClick = { onVideoClick(pageItem) },
                    excitementTag = excitementTag.ifEmpty { null },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun HeroCarouselIndicators(
    pagerState: PagerState,
    pageCount: Int,
    isLandscape: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = pagerState.currentPage == index
            val activeColor = Color(0xFFD4AF37)
            val inactiveColor = Color.White.copy(alpha = 0.2f)
            val dotHeight = if (isLandscape) 4.dp else 6.dp
            val dotWidth = if (isSelected) (if (isLandscape) 14.dp else 20.dp) else (if (isLandscape) 4.dp else 6.dp)

            Box(
                modifier = Modifier
                    .height(dotHeight)
                    .width(dotWidth)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else inactiveColor),
            )
        }
    }
}

@Composable
fun HeroAdCard(
    modifier: Modifier = Modifier,
    isCinematic: Boolean = false,
    isLandscape: Boolean = false,
) {
    val shape = RoundedCornerShape(14.dp)

    BoxWithConstraints(modifier = modifier) {
        val landscapeMode = isLandscape || (maxHeight < 300.dp && maxHeight != Dp.Infinity)
        val isFillMax = maxHeight != Dp.Infinity && maxHeight > 200.dp
        val cardMod = if (isFillMax) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxWidth()
                .height(if (isCinematic) 360.dp else if (landscapeMode) 180.dp else 240.dp)
        }

        Card(
            modifier = cardMod,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
            border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BannerAdView(
                    onAdLoaded = {},
                    onAdFailed = {},
                    style = if (landscapeMode) NativeAdStyle.HERO_LANDSCAPE else NativeAdStyle.HERO,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape),
                )
            }
        }
    }
}
