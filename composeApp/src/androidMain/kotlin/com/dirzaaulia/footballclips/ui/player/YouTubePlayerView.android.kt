package com.dirzaaulia.footballclips.ui.player

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.dirzaaulia.footballclips.ui.home.findActivity

enum class PlayerErrorState {
    NONE,
    REGION_BLOCKED,
    NOT_EMBEDDABLE,
    USER_SIDE_ERROR
}

@Composable
actual fun YouTubePlayerView(
    videoId: String,
    backupVideoId: String?,
    regionInfo: com.dirzaaulia.footballclips.domain.model.RegionRestriction?,
    backupRegionInfo: com.dirzaaulia.footballclips.domain.model.RegionRestriction?,
    modifier: Modifier,
    onDismiss: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    
    val userCountry = remember {
        try {
            val tm = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
            val sim = tm?.simCountryIso?.uppercase()
            val network = tm?.networkCountryIso?.uppercase()
            val locale = java.util.Locale.getDefault().country.uppercase()
            when {
                !sim.isNullOrBlank() -> sim
                !network.isNullOrBlank() -> network
                !locale.isNullOrBlank() -> locale
                else -> "US"
            }
        } catch (_: Throwable) {
            java.util.Locale.getDefault().country.uppercase().ifBlank { "US" }
        }
    }

    val isPrimaryAllowed = remember(videoId, userCountry) {
        regionInfo?.isSupportedIn(userCountry) ?: true
    }
    val isBackupAllowed = remember(backupVideoId, userCountry) {
        backupRegionInfo?.isSupportedIn(userCountry) ?: true
    }

    // Smart region pre-selection
    val initialVideoId = remember(videoId, backupVideoId, isPrimaryAllowed, isBackupAllowed) {
        if (!isPrimaryAllowed && isBackupAllowed && !backupVideoId.isNullOrBlank()) {
            backupVideoId
        } else {
            videoId
        }
    }
    val initialSwitched = (initialVideoId == backupVideoId && backupVideoId != videoId)
    val initialError = if (!isPrimaryAllowed && !isBackupAllowed) PlayerErrorState.REGION_BLOCKED else PlayerErrorState.NONE

    var currentVideoId by remember(initialVideoId) { mutableStateOf(initialVideoId) }
    var hasSwitchedToBackup by remember(initialVideoId) { mutableStateOf(initialSwitched) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var errorState by remember(initialError) { mutableStateOf(initialError) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    fun handlePlaybackError(url: String?) {
        val code = url?.removePrefix("fc-error://")?.toIntOrNull() ?: 150
        if (code == 101 || code == 150 || code == 100) {
            if (backupVideoId != null && !hasSwitchedToBackup && backupVideoId != currentVideoId && isBackupAllowed) {
                android.util.Log.d("YouTubePlayerView", "Primary video $currentVideoId restricted (code $code). Auto-failing over to backup $backupVideoId")
                hasSwitchedToBackup = true
                currentVideoId = backupVideoId
                errorState = PlayerErrorState.NONE
                val newHtml = buildYouTubeHtml(backupVideoId)
                webViewInstance?.loadDataWithBaseURL("https://dirzaaulia.com", newHtml, "text/html", "UTF-8", null)
            } else {
                errorState = PlayerErrorState.NOT_EMBEDDABLE
            }
        } else if (code == 2 || code == 5) {
            errorState = PlayerErrorState.USER_SIDE_ERROR
        } else {
            errorState = PlayerErrorState.USER_SIDE_ERROR
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        webViewInstance?.evaluateJavascript("if (typeof player !== 'undefined' && player.pauseVideo) { player.pauseVideo(); }", null)
        webViewInstance?.onPause()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        webViewInstance?.onResume()
    }

    DisposableEffect(customView) {
        if (customView != null) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    Box(modifier = modifier.background(Color.Black)) {
        if (errorState == PlayerErrorState.NONE) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                if (url?.startsWith("fc-error://") == true) {
                                    handlePlaybackError(url)
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString()
                                if (url?.startsWith("fc-error://") == true) {
                                    handlePlaybackError(url)
                                    return true
                                }
                                return super.shouldOverrideUrlLoading(view, request)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    errorState = PlayerErrorState.USER_SIDE_ERROR
                                }
                            }
                        }
                        
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                android.util.Log.d("YouTubePlayerView", "JS: ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})")
                                return super.onConsoleMessage(consoleMessage)
                            }

                            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                                customView = view
                                customViewCallback = callback
                            }

                            override fun onHideCustomView() {
                                customView = null
                                customViewCallback = null
                            }
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
                        
                        val html = buildYouTubeHtml(currentVideoId)
                        
                        loadDataWithBaseURL("https://dirzaaulia.com", html, "text/html", "UTF-8", null)
                        webViewInstance = this
                    }
                },
                onRelease = { wv ->
                    webViewInstance = null
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
            )
        }

        when (errorState) {
            PlayerErrorState.REGION_BLOCKED -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Not Available in Your Region",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This official match highlight is restricted in your country ($userCountry) due to regional broadcast licensing agreements.",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            PlayerErrorState.NOT_EMBEDDABLE -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Playback Restricted in Embedded Player",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "The rights holder requires this highlight to be watched directly on official YouTube channels.",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$currentVideoId")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$currentVideoId")).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            try {
                                context.startActivity(appIntent)
                            } catch (ex: Exception) {
                                context.startActivity(webIntent)
                            }
                            onDismiss(true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Watch on YouTube")
                    }
                }
            }
            PlayerErrorState.USER_SIDE_ERROR -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Playback Error",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Unable to load video. Please check your internet connection and try again.",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            errorState = PlayerErrorState.NONE
                            val html = buildYouTubeHtml(currentVideoId)
                            webViewInstance?.loadDataWithBaseURL("https://dirzaaulia.com", html, "text/html", "UTF-8", null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Try Again")
                    }
                }
            }
            PlayerErrorState.NONE -> {
                // Video is active
            }
        }

        // Active failover notification pill
        if (hasSwitchedToBackup && errorState == PlayerErrorState.NONE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xD9101418))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .align(Alignment.TopCenter)
            ) {
                Text(
                    text = "Playing alternative official highlight stream",
                    color = Color(0xFF7098F8),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Fullscreen Overlay
        if (customView != null) {
            AndroidView(
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        (customView?.parent as? ViewGroup)?.removeView(customView)
                        addView(customView)
                        setBackgroundColor(android.graphics.Color.BLACK)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
