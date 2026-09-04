package com.dirzaaulia.footballclips.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement

@JsFun("(id, videoId, onError) => { " +
        "console.log('initYouTubePlayer called for:', id, videoId); " +
        "if (typeof YT === 'undefined' || typeof YT.Player === 'undefined') { " +
        "  console.error('YouTube API not ready'); " +
        "  return null; " +
        "} " +
        "try { " +
        "  return new YT.Player(id, { " +
        "    height: '100%', " +
        "    width: '100%', " +
        "    videoId: videoId, " +
        "    playerVars: { 'autoplay': 1, 'playsinline': 1, 'modestbranding': 1 }, " +
        "    events: { " +
        "      'onReady': () => console.log('YT Player Ready:', id), " +
        "      'onError': (event) => { " +
        "         console.log('YouTube Error for ' + id + ':', event.data); " +
        "         onError(); " +
        "      } " +
        "    } " +
        "  }); " +
        "} catch (e) { " +
        "  console.error('Failed to create YT.Player:', e); " +
        "  return null; " +
        "} " +
        "}")
external fun initYouTubePlayer(id: String, videoId: String, onError: () -> Unit): JsAny?

@JsFun("(player) => { if (player && typeof player.destroy === 'function') { player.destroy(); } }")
external fun destroyYouTubePlayer(player: JsAny?)

@Composable
actual fun YouTubePlayerView(
    videoId: String, 
    modifier: Modifier,
    onDismiss: (Boolean) -> Unit
) {
    val density = LocalDensity.current
    var containerElement by remember { mutableStateOf<HTMLDivElement?>(null) }
    var isVideoBlocked by remember { mutableStateOf(false) }
    
    key(videoId) {
        // Unique ID for this specific player instance
        val elementId = remember(videoId) { "yt-player-$videoId" }

        Box(
            modifier = modifier
                .background(Color.Black)
                .onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInWindow()
                    val size = coordinates.size
                    
                    containerElement?.let { container ->
                        container.style.left = "${position.x / density.density}px"
                        container.style.top = "${position.y / density.density}px"
                        container.style.width = "${size.width / density.density}px"
                        container.style.height = "${size.height / density.density}px"
                        // Only hide if we have a confirmation of block
                        container.style.display = if (isVideoBlocked) "none" else "block"
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            DisposableEffect(videoId) {
                // Create a persistent container div that holds the positioning
                val container = (document.createElement("div") as HTMLDivElement).apply {
                    id = "container-$elementId"
                    style.cssText = "position: absolute; border: none; z-index: 1000; display: none; background: black; border-radius: 16px; overflow: hidden;"
                }
                
                // Create the div that will be replaced by the YouTube iframe
                val playerDiv = (document.createElement("div") as HTMLDivElement).apply {
                    id = elementId
                }
                container.appendChild(playerDiv)
                
                document.body?.appendChild(container)
                containerElement = container
                
                val player = initYouTubePlayer(elementId, videoId) {
                    isVideoBlocked = true
                }
                
                onDispose {
                    destroyYouTubePlayer(player)
                    container.remove()
                    containerElement = null
                }
            }

            if (isVideoBlocked) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        text = "Video Restricted",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        text = "This content provider (e.g., LaLiga) has restricted playback to YouTube only.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            window.open("https://www.youtube.com/watch?v=$videoId", "_blank")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Watch on YouTube", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
