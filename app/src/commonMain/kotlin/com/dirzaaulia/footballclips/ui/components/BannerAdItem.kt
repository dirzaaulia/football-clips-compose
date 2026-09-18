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

    val cardHeight = when (style) {
        NativeAdStyle.HERO -> 240.dp
        NativeAdStyle.MATCH -> 210.dp
        NativeAdStyle.HIGHLIGHT -> 145.dp
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
