package com.dirzaaulia.footballclips.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun WebViewScreen(
    url: String,
    isAdsRemoved: Boolean = false,
    modifier: Modifier = Modifier
)
