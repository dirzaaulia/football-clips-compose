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
                iframe { width: 100%; height: 100%; border: none; }
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
                            'modestbranding': 1,
                            'fs': 1,
                            'origin': 'https://dirzaaulia.com',
                            'enablejsapi': 1
                        },
                        events: {
                            'onReady': function(event) { event.target.playVideo(); },
                            'onError': function(event) { window.location.href = "fc-error://playback"; }
                        }
                    });
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
