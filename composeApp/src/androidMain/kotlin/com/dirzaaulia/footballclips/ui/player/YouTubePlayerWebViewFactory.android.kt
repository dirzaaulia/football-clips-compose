package com.dirzaaulia.footballclips.ui.player

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

internal fun createConfiguredWebView(
    context: Context,
    currentVideoId: String,
    backupVideoId: String?,
    hasSwitchedToBackup: Boolean,
    isBackupAllowed: Boolean,
    onLoadingState: (Boolean) -> Unit,
    onErrorState: (PlayerErrorState) -> Unit,
    onSwitchedToBackup: (String) -> Unit,
    onCustomViewChange: (View?, WebChromeClient.CustomViewCallback?) -> Unit,
    onInstanceCreated: (WebView) -> Unit,
    onPlaybackStarted: () -> Unit,
    isShort: Boolean,
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setBackgroundColor(Color.BLACK)

        val bridge = YouTubePlayerBridge(
            onReady = { onLoadingState(false) },
            onStateChange = { state ->
                if (state == 1) {
                    onLoadingState(false)
                    onPlaybackStarted()
                } else if (state == 2) {
                    onLoadingState(false)
                }
            },
            onError = { code ->
                onPlaybackStarted()
                handlePlayerErrorCode(
                    code = code,
                    webView = this,
                    currentVideoId = currentVideoId,
                    backupVideoId = backupVideoId,
                    hasSwitchedToBackup = hasSwitchedToBackup,
                    isBackupAllowed = isBackupAllowed,
                    isShort = isShort,
                    onLoadingState = onLoadingState,
                    onErrorState = onErrorState,
                    onSwitchedToBackup = onSwitchedToBackup,
                )
            },
        )
        addJavascriptInterface(bridge, "FCBridge")

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString().orEmpty()
                if (url.startsWith("fc-state://")) {
                    val state = url.removePrefix("fc-state://").toIntOrNull()
                    if (state == 1) {
                        onLoadingState(false)
                        onPlaybackStarted()
                    } else if (state == 2) {
                        onLoadingState(false)
                    }
                    return true
                }
                if (url.startsWith("fc-ready://")) {
                    onLoadingState(false)
                    return true
                }
                if (url.startsWith("fc-error://")) {
                    onPlaybackStarted()
                    val code = url.removePrefix("fc-error://").toIntOrNull() ?: 150
                    handlePlayerErrorCode(
                        code = code,
                        webView = this@apply,
                        currentVideoId = currentVideoId,
                        backupVideoId = backupVideoId,
                        hasSwitchedToBackup = hasSwitchedToBackup,
                        isBackupAllowed = isBackupAllowed,
                        isShort = isShort,
                        onLoadingState = onLoadingState,
                        onErrorState = onErrorState,
                        onSwitchedToBackup = onSwitchedToBackup,
                    )
                    return true
                }
                if (url.startsWith("fc-") || !url.startsWith("http")) {
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                val failingUrl = request?.url?.toString().orEmpty()
                if (request?.isForMainFrame == true && !failingUrl.startsWith("fc-")) {
                    onLoadingState(false)
                    onErrorState(PlayerErrorState.USER_SIDE_ERROR)
                }
            }
        }
        webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) { onCustomViewChange(view, callback) }
            override fun onHideCustomView() { onCustomViewChange(null, null) }
        }
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            useWideViewPort = true
            loadWithOverviewMode = true
            if (!isShort) {
                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
            }
        }
        loadDataWithBaseURL("https://dirzaaulia.com", buildYouTubeHtml(currentVideoId, isShort), "text/html", "UTF-8", null)
        onInstanceCreated(this)
    }
}

private fun handlePlayerErrorCode(
    code: Int,
    webView: WebView,
    currentVideoId: String,
    backupVideoId: String?,
    hasSwitchedToBackup: Boolean,
    isBackupAllowed: Boolean,
    isShort: Boolean,
    onLoadingState: (Boolean) -> Unit,
    onErrorState: (PlayerErrorState) -> Unit,
    onSwitchedToBackup: (String) -> Unit,
) {
    onLoadingState(false)
    val isEmbedRestricted = code == 101 || code == 150 || code == 100
    if (isEmbedRestricted && backupVideoId != null && !hasSwitchedToBackup && backupVideoId != currentVideoId && isBackupAllowed) {
        onSwitchedToBackup(backupVideoId)
        webView.loadDataWithBaseURL("https://dirzaaulia.com", buildYouTubeHtml(backupVideoId, isShort), "text/html", "UTF-8", null)
    } else if (isEmbedRestricted) {
        onErrorState(PlayerErrorState.NOT_EMBEDDABLE)
    } else {
        onErrorState(PlayerErrorState.USER_SIDE_ERROR)
    }
}
