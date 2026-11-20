package com.dirzaaulia.footballclips.ui.player

internal fun buildYouTubeHtml(videoId: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; background-color: #000000; overflow: hidden; display: flex; justify-content: center; align-items: center; }
                #player { width: 100%; height: 100%; transform: scale(1.18); transform-origin: center center; }
                iframe { width: 100%; height: 100%; border: none; pointer-events: none; }
                .ytp-chrome-top, .ytp-chrome-bottom, .ytp-watermark, .ytp-pause-overlay { display: none !important; opacity: 0 !important; }
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
                            'controls': 0,
                            'disablekb': 1,
                            'iv_load_policy': 3,
                            'modestbranding': 1,
                            'fs': 0,
                            'origin': 'https://dirzaaulia.com',
                            'enablejsapi': 1
                        },
                        events: {
                            'onReady': function(event) {
                                window.location.href = "fc-ready://ready";
                                event.target.playVideo();
                            },
                            'onStateChange': function(event) {
                                window.location.href = "fc-state://" + event.data;
                            },
                            'onError': function(event) {
                                window.location.href = "fc-error://" + event.data;
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
