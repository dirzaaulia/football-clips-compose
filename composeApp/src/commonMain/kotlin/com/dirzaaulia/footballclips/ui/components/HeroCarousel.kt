package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.util.isWasmTarget
import kotlinx.coroutines.delay

@Composable
fun HeroCarousel(
    items: List<HighlightUiItem>,
    isAdsRemoved: Boolean,
    onVideoClick: (HighlightUiItem) -> Unit,
    modifier: Modifier = Modifier,
    isCinematic: Boolean = false,
    isSpoilerFreeMode: Boolean = false,
    revealedItemIds: Set<String> = emptySet(),
    onRevealItem: ((String) -> Unit)? = null
) {
    if (items.isEmpty()) return

    val isWasmWeb = isWasmTarget
    
    val carouselPages = remember(items, isAdsRemoved, isWasmWeb) {
        val featuredItem = items.firstOrNull { it !is HighlightUiItem.BannerAd }
        if (featuredItem == null) {
            emptyList()
        } else if (isAdsRemoved || isWasmWeb) {
            listOf(featuredItem)
        } else {
            listOf(featuredItem, HighlightUiItem.BannerAd("hero-ad-0"))
        }
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isWasmWeb && !isAdsRemoved) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val featuredItem = carouselPages.firstOrNull() ?: return@Row
                
                Box(modifier = Modifier.weight(0.6f)) {
                    val itemUniqueId = featuredItem.uniqueId
                    HeroCard(
                        item = featuredItem,
                        onClick = { onVideoClick(featuredItem) },
                        isCinematic = isCinematic,
                        isSpoilerFree = isSpoilerFreeMode,
                        isRevealed = revealedItemIds.contains(itemUniqueId),
                        onRevealClick = { onRevealItem?.invoke(itemUniqueId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Box(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
                    HeroAdCard(
                        isCinematic = isCinematic,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = 16.dp
            ) { page ->
                val pageItem = carouselPages[page]
                when (pageItem) {
                    is HighlightUiItem.BannerAd -> {
                        HeroAdCard(
                            isCinematic = isCinematic,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        val itemUniqueId = pageItem.uniqueId
                        HeroCard(
                            item = pageItem,
                            onClick = { onVideoClick(pageItem) },
                            isCinematic = isCinematic,
                            isSpoilerFree = isSpoilerFreeMode,
                            isRevealed = revealedItemIds.contains(itemUniqueId),
                            onRevealClick = { onRevealItem?.invoke(itemUniqueId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (carouselPages.size > 1) {
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(carouselPages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val activeColor = Color(0xFFD4AF37)
                        val inactiveColor = Color.White.copy(alpha = 0.2f)

                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 20.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) activeColor else inactiveColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroAdCard(
    modifier: Modifier = Modifier,
    isCinematic: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)

    BoxWithConstraints(modifier = modifier) {
        val isLandscape = maxHeight < 300.dp && maxHeight != Dp.Infinity
        val isFillMax = maxHeight != Dp.Infinity && maxHeight > 200.dp
        
        val cardMod = if (isFillMax) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(if (isCinematic) 360.dp else if (isLandscape) 180.dp else 240.dp)

        Card(
            modifier = cardMod,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
            border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BannerAdView(
                    onAdLoaded = {},
                    onAdFailed = {},
                    style = NativeAdStyle.HERO,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                )
            }
        }
    }
}
