package com.dirzaaulia.footballclips.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BannerAdItem(
    modifier: Modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    style: NativeAdStyle = NativeAdStyle.MATCH
) {
    var isLoading by remember { mutableStateOf(true) }
    var isAdFailed by remember { mutableStateOf(false) }

    if (isAdFailed) return

    BoxWithConstraints {
        val isLandscape = maxHeight < 300.dp
        val cardHeight = when (style) {
            NativeAdStyle.HERO -> if (isLandscape) 180.dp else 240.dp
            NativeAdStyle.MATCH -> if (isLandscape) 180.dp else 250.dp
            NativeAdStyle.HIGHLIGHT -> if (isLandscape) 120.dp else 145.dp
        }

    val shape = if (style == NativeAdStyle.MATCH) RoundedCornerShape(14.dp) else RoundedCornerShape(14.dp)
    val border = if (style == NativeAdStyle.MATCH) BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.35f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .animateContentSize(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                ExpressiveLoadingIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
            }

            BannerAdView(
                onAdLoaded = { 
                    isLoading = false
                    isAdFailed = false
                },
                onAdFailed = { 
                    isLoading = false
                    isAdFailed = true
                },
                style = style,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .alpha(if (isLoading) 0f else 1f)
                    .clip(RoundedCornerShape(18.dp))
            )
        }
    }
}
}
