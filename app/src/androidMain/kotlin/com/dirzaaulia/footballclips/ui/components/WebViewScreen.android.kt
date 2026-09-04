package com.dirzaaulia.footballclips.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun WebViewScreen(
    url: String,
    isAdsRemoved: Boolean,
    modifier: Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadUrl(url)
                }
            },
            update = { webView ->
                webView.loadUrl(url)
            },
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        
        // Menampilkan Banner Ad sederhana di bawah WebView jika belum beli remove ads
        if (!isAdsRemoved) {
            SmallBannerAdItem(modifier = Modifier.fillMaxWidth())
        }
    }
}
