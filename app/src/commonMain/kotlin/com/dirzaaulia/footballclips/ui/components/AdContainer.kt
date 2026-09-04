package com.dirzaaulia.footballclips.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdContainer(
    isAdsRemoved: Boolean,
    modifier: Modifier = Modifier,
    adContent: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = !isAdsRemoved,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        adContent()
    }
}
