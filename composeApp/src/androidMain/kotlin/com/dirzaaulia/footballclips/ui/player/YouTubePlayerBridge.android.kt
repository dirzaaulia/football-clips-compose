package com.dirzaaulia.footballclips.ui.player

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

internal class YouTubePlayerBridge(
    private val onReady: () -> Unit,
    private val onStateChange: (Int) -> Unit,
    private val onError: (Int) -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun postReady() {
        mainHandler.post { onReady() }
    }

    @JavascriptInterface
    fun postStateChange(state: Int) {
        mainHandler.post { onStateChange(state) }
    }

    @JavascriptInterface
    fun postError(errorCode: Int) {
        mainHandler.post { onError(errorCode) }
    }
}
