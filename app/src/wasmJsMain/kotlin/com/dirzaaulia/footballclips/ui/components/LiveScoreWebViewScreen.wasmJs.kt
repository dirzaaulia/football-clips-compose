package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun LiveScoreWebViewScreen(
    url: String,
    isAdsRemoved: Boolean,
    modifier: Modifier,
    contentPadding: PaddingValues
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("LiveScore WebView not supported on Wasm target yet: $url")
    }
}
