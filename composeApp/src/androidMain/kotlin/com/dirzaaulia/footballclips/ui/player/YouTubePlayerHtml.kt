package com.dirzaaulia.footballclips.ui.player

import androidx.compose.foundation.layout.fillMaxSize

internal fun buildYouTubeHtml(videoId: String, isShort: Boolean = false): String {
    val scaleTransform = if (isShort) "scale(1.18)" else "none"
    val pointerEvents = if (isShort) "none" else "auto"
    val controlsVal = if (isShort) 0 else 1
    val fsVal = if (isShort) 0 else 1
    val loopConfig = if (isShort) "'loop': 1, 'playlist': '$videoId'," else ""

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #000000 !important; overflow: hidden; display: flex; justify-content: center; align-items: center; }
                #player { width: 100%; height: 100%; transform: $scaleTransform; transform-origin: center center; background-color: #000000 !important; }
                iframe { width: 100%; height: 100%; border: none; pointer-events: $pointerEvents; background-color: #000000 !important; }
                .ytp-chrome-top, .ytp-watermark, .ytp-pause-overlay, .ytp-cued-thumbnail-overlay { display: none !important; opacity: 0 !important; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        height: '100%',
                        width: '100%',
                        videoId: '$videoId',
                        playerVars: {
                            'autoplay': 1,
                            'playsinline': 1,
                            'rel': 0,
                            'controls': $controlsVal,
                            'disablekb': ${if (isShort) 1 else 0},
                            'iv_load_policy': 3,
                            'modestbranding': 1,
                            'fs': $fsVal,
                            $loopConfig
                            'origin': 'https://dirzaaulia.com',
                            'enablejsapi': 1
                        },
                        events: {
                            'onReady': function(event) {
                                if (window.FCBridge && window.FCBridge.postReady) {
                                    window.FCBridge.postReady();
                                } else {
                                    window.location.href = "fc-ready://ready";
                                }
                                event.target.playVideo();
                            },
                            'onStateChange': function(event) {
                                if (window.FCBridge && window.FCBridge.postStateChange) {
                                    window.FCBridge.postStateChange(event.data);
                                } else {
                                    window.location.href = "fc-state://" + event.data;
                                }
                                if (event.data === 0) {
                                    event.target.seekTo(0);
                                    event.target.playVideo();
                                }
                            },
                            'onError': function(event) {
                                if (window.FCBridge && window.FCBridge.postError) {
                                    window.FCBridge.postError(event.data);
                                } else {
                                    window.location.href = "fc-error://" + event.data;
                                }
                            }
                        }
                    });
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}

internal fun cleanupWebView(wv: android.webkit.WebView) {
    try {
        wv.stopLoading()
        wv.webViewClient = object : android.webkit.WebViewClient() {}
        wv.webChromeClient = null
        wv.loadUrl("about:blank")
        wv.clearHistory()
        wv.removeAllViews()
        (wv.parent as? android.view.ViewGroup)?.removeView(wv)
        wv.destroy()
    } catch (_: Throwable) {}
}

@androidx.compose.runtime.Composable
internal fun FullscreenPlayerOverlay(customView: android.view.View) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { ctx ->
            android.widget.FrameLayout(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                )
                (customView.parent as? android.view.ViewGroup)?.removeView(customView)
                addView(customView)
                setBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
    )
}

internal fun resolveUserCountry(context: android.content.Context): String {
    return try {
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
