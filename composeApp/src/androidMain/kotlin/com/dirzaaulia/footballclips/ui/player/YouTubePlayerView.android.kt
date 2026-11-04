package com.dirzaaulia.footballclips.ui.player

import android.content.pm.ActivityInfo
import android.telephony.TelephonyManager
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.dirzaaulia.footballclips.domain.model.RegionRestriction
import com.dirzaaulia.footballclips.ui.home.findActivity
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
actual fun YouTubePlayerView(
    videoId: String,
    backupVideoId: String?,
    regionInfo: RegionRestriction?,
    backupRegionInfo: RegionRestriction?,
    modifier: Modifier,
    onDismiss: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val userCountry = remember { resolveUserCountry(context) }

    val isPrimaryAllowed = remember(videoId, userCountry) { regionInfo?.isSupportedIn(userCountry) ?: true }
    val isBackupAllowed = remember(backupVideoId, userCountry) { backupRegionInfo?.isSupportedIn(userCountry) ?: true }

    val initialVideoId = remember(videoId, backupVideoId, isPrimaryAllowed, isBackupAllowed) {
        if (!isPrimaryAllowed && isBackupAllowed && !backupVideoId.isNullOrBlank()) backupVideoId else videoId
    }
    val initialSwitched = (initialVideoId == backupVideoId && backupVideoId != videoId)
    val initialError = if (!isPrimaryAllowed && !isBackupAllowed) PlayerErrorState.REGION_BLOCKED else PlayerErrorState.NONE

    var currentVideoId by remember(initialVideoId) { mutableStateOf(initialVideoId) }
    var hasSwitchedToBackup by remember(initialVideoId) { mutableStateOf(initialSwitched) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var errorState by remember(initialError) { mutableStateOf(initialError) }
    var isLoading by remember(currentVideoId) { mutableStateOf(true) }
    var retryNonce by remember { mutableStateOf(0) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // 7-second watchdog timer for stream connection stalls
    LaunchedEffect(currentVideoId, isLoading, errorState, retryNonce) {
        if (isLoading && errorState == PlayerErrorState.NONE) {
            delay(7000)
            if (isLoading && errorState == PlayerErrorState.NONE) {
                if (backupVideoId != null && !hasSwitchedToBackup && backupVideoId != currentVideoId && isBackupAllowed) {
                    hasSwitchedToBackup = true
                    currentVideoId = backupVideoId
                    isLoading = true
                    webViewInstance?.loadDataWithBaseURL("https://dirzaaulia.com", buildYouTubeHtml(backupVideoId), "text/html", "UTF-8", null)
                } else {
                    errorState = PlayerErrorState.NETWORK_TIMEOUT
                    isLoading = false
                }
            }
        }
    }

    SetupLifecycleAndOrientation(activity = activity, customView = customView, webViewInstance = webViewInstance)

    Box(modifier = modifier.background(Color.Black)) {
        if (errorState == PlayerErrorState.NONE) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    createConfiguredWebView(
                        context = ctx,
                        currentVideoId = currentVideoId,
                        backupVideoId = backupVideoId,
                        hasSwitchedToBackup = hasSwitchedToBackup,
                        isBackupAllowed = isBackupAllowed,
                        onLoadingState = { isLoading = it },
                        onErrorState = { errorState = it },
                        onSwitchedToBackup = {
                            hasSwitchedToBackup = true
                            currentVideoId = it
                            isLoading = true
                        },
                        onCustomViewChange = { v, cb -> customView = v; customViewCallback = cb },
                        onInstanceCreated = { webViewInstance = it },
                    )
                },
                onRelease = { wv -> cleanupWebView(wv) },
            )
        }

        if (isLoading && errorState == PlayerErrorState.NONE) {
            PlayerLoadingOverlay()
        }

        PlayerErrorOverlay(
            errorState = errorState,
            userCountry = userCountry,
            currentVideoId = currentVideoId,
            context = context,
            onRetry = {
                errorState = PlayerErrorState.NONE
                isLoading = true
                retryNonce++
                webViewInstance?.loadDataWithBaseURL("https://dirzaaulia.com", buildYouTubeHtml(currentVideoId), "text/html", "UTF-8", null)
            },
            onDismiss = onDismiss,
        )

        if (hasSwitchedToBackup && errorState == PlayerErrorState.NONE) {
            PlayerBackupPill()
        }

        customView?.let { FullscreenPlayerOverlay(customView = it) }
    }
}

@Composable
private fun SetupLifecycleAndOrientation(
    activity: android.app.Activity?,
    customView: View?,
    webViewInstance: WebView?,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        webViewInstance?.evaluateJavascript("if (typeof player !== 'undefined' && player.pauseVideo) { player.pauseVideo(); }", null)
        webViewInstance?.onPause()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        webViewInstance?.onResume()
    }
    DisposableEffect(customView) {
        activity?.requestedOrientation = if (customView != null) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }
}

@Composable
private fun FullscreenPlayerOverlay(customView: View) {
    AndroidView(
        factory = { ctx ->
            FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                (customView.parent as? ViewGroup)?.removeView(customView)
                addView(customView)
                setBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}

private fun createConfiguredWebView(
    context: android.content.Context,
    currentVideoId: String,
    backupVideoId: String?,
    hasSwitchedToBackup: Boolean,
    isBackupAllowed: Boolean,
    onLoadingState: (Boolean) -> Unit,
    onErrorState: (PlayerErrorState) -> Unit,
    onSwitchedToBackup: (String) -> Unit,
    onCustomViewChange: (View?, WebChromeClient.CustomViewCallback?) -> Unit,
    onInstanceCreated: (WebView) -> Unit,
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString().orEmpty()
                if (url.startsWith("fc-ready://") || url.startsWith("fc-state://1")) {
                    onLoadingState(false)
                    return true
                }
                if (url.startsWith("fc-error://")) {
                    onLoadingState(false)
                    val code = url.removePrefix("fc-error://").toIntOrNull() ?: 150
                    if ((code == 101 || code == 150 || code == 100) && backupVideoId != null && !hasSwitchedToBackup && backupVideoId != currentVideoId && isBackupAllowed) {
                        onSwitchedToBackup(backupVideoId)
                        loadDataWithBaseURL("https://dirzaaulia.com", buildYouTubeHtml(backupVideoId), "text/html", "UTF-8", null)
                    } else if (code == 101 || code == 150 || code == 100) {
                        onErrorState(PlayerErrorState.NOT_EMBEDDABLE)
                    } else {
                        onErrorState(PlayerErrorState.USER_SIDE_ERROR)
                    }
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    onLoadingState(false)
                    onErrorState(PlayerErrorState.USER_SIDE_ERROR)
                }
            }
        }
        webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) { onCustomViewChange(view, callback) }
            override fun onHideCustomView() { onCustomViewChange(null, null) }
        }
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            useWideViewPort = true
            loadWithOverviewMode = true
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
        }
        loadDataWithBaseURL("https://dirzaaulia.com", buildYouTubeHtml(currentVideoId), "text/html", "UTF-8", null)
        onInstanceCreated(this)
    }
}

private fun cleanupWebView(wv: WebView) {
    try {
        wv.stopLoading()
        wv.webViewClient = object : WebViewClient() {}
        wv.webChromeClient = null
        wv.loadUrl("about:blank")
        wv.clearHistory()
        wv.removeAllViews()
        (wv.parent as? ViewGroup)?.removeView(wv)
        wv.destroy()
    } catch (_: Throwable) {}
}

private fun resolveUserCountry(context: android.content.Context): String {
    return try {
        val tm = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as? TelephonyManager
        val sim = tm?.simCountryIso?.uppercase()
        val network = tm?.networkCountryIso?.uppercase()
        val locale = Locale.getDefault().country.uppercase()
        when {
            !sim.isNullOrBlank() -> sim
            !network.isNullOrBlank() -> network
            !locale.isNullOrBlank() -> locale
            else -> "US"
        }
    } catch (_: Throwable) {
        Locale.getDefault().country.uppercase().ifBlank { "US" }
    }
}
