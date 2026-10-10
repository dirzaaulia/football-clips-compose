package com.dirzaaulia.footballclips.ui.components.ad

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class NativeAdStyle {
    HERO,
    MATCH,
    HIGHLIGHT
}

@Composable
expect fun BannerAdView(
    onAdLoaded: () -> Unit,
    onAdFailed: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: NativeAdStyle = NativeAdStyle.MATCH
)
