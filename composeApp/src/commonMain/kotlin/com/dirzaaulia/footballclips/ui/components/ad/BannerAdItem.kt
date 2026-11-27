package com.dirzaaulia.footballclips.ui.components.ad

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator

@Composable
fun BannerAdItem(
    modifier: Modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    style: NativeAdStyle = NativeAdStyle.MATCH,
    onAdFailed: (() -> Unit)? = null,
) {
    var isLoading by remember { mutableStateOf(true) }
    var isAdFailed by remember { mutableStateOf(false) }

    if (isAdFailed) return

    BoxWithConstraints {
        val isLandscape = maxHeight < 300.dp
        val cardHeight = resolveAdHeight(style, isLandscape, maxHeight)
        val shape = resolveAdShape(style)
        val border = resolveAdBorder(style)

        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(cardHeight)
                .animateContentSize(),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            AdCardContent(
                cardHeight = cardHeight,
                isLoading = isLoading,
                style = style,
                onLoaded = {
                    isLoading = false
                    isAdFailed = false
                },
                onFailed = {
                    isLoading = false
                    isAdFailed = true
                    onAdFailed?.invoke()
                },
            )
        }
    }
}

@Composable
private fun AdCardContent(
    cardHeight: Dp,
    isLoading: Boolean,
    style: NativeAdStyle,
    onLoaded: () -> Unit,
    onFailed: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(cardHeight),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            ExpressiveLoadingIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
            )
        }

        BannerAdView(
            onAdLoaded = onLoaded,
            onAdFailed = { onFailed() },
            style = style,
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .clip(RoundedCornerShape(18.dp)),
        )
    }
}

private fun resolveAdHeight(style: NativeAdStyle, isLandscape: Boolean, maxHeight: Dp): Dp = when (style) {
    NativeAdStyle.HERO,
    NativeAdStyle.HERO_LANDSCAPE,
    -> if (isLandscape) 180.dp else 240.dp
    NativeAdStyle.MATCH -> if (isLandscape) 130.dp else 150.dp
    NativeAdStyle.HIGHLIGHT -> if (isLandscape) 180.dp else 275.dp
    NativeAdStyle.STANDINGS -> if (isLandscape) 115.dp else 132.dp
    NativeAdStyle.SHORTS -> if (isLandscape) 280.dp else (maxHeight - 116.dp).coerceIn(380.dp, 540.dp)
}

private fun resolveAdShape(style: NativeAdStyle): RoundedCornerShape = when (style) {
    NativeAdStyle.MATCH -> RoundedCornerShape(14.dp)
    NativeAdStyle.STANDINGS -> RoundedCornerShape(12.dp)
    NativeAdStyle.SHORTS -> RoundedCornerShape(20.dp)
    else -> RoundedCornerShape(16.dp)
}

@Composable
private fun resolveAdBorder(style: NativeAdStyle): BorderStroke = when (style) {
    NativeAdStyle.MATCH -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    NativeAdStyle.STANDINGS -> BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f))
    NativeAdStyle.SHORTS -> BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}
