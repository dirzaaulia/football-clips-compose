package com.dirzaaulia.footballclips.ui.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class PlayerErrorState {
    NONE,
    REGION_BLOCKED,
    NOT_EMBEDDABLE,
    USER_SIDE_ERROR,
    NETWORK_TIMEOUT,
}

@Composable
internal fun PlayerLoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E14)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Connecting to official highlight stream...",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
internal fun PlayerErrorOverlay(
    errorState: PlayerErrorState,
    userCountry: String,
    currentVideoId: String,
    context: Context,
    onRetry: () -> Unit,
    onDismiss: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (errorState == PlayerErrorState.NONE) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E14))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (errorState) {
            PlayerErrorState.REGION_BLOCKED -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Not Available in Your Region",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This official match highlight is restricted in your country ($userCountry) due to regional broadcast licensing agreements.",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            PlayerErrorState.NOT_EMBEDDABLE -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Playback Restricted in Embedded Player",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The rights holder requires this highlight to be watched directly on official YouTube channels.",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { launchYouTube(context, currentVideoId, onDismiss) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        Text("Watch on YouTube")
                    }
                }
            }

            PlayerErrorState.NETWORK_TIMEOUT, PlayerErrorState.USER_SIDE_ERROR -> {
                val isTimeout = errorState == PlayerErrorState.NETWORK_TIMEOUT
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = if (isTimeout) "Connection Timed Out" else "Playback Error",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isTimeout) {
                            "The highlight stream took too long to connect. Check your internet connection and try again."
                        } else {
                            "Unable to load video. Please check your internet connection and try again."
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Text("Retry")
                        }
                        OutlinedButton(
                            onClick = { launchYouTube(context, currentVideoId, onDismiss) },
                        ) {
                            Text("YouTube")
                        }
                    }
                }
            }

            PlayerErrorState.NONE -> Unit
        }
    }
}

@Composable
internal fun BoxScope.PlayerBackupPill() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xD9101418))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .align(Alignment.TopCenter),
    ) {
        Text(
            text = "Playing alternative official highlight stream",
            color = Color(0xFF7098F8),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

private fun launchYouTube(context: Context, videoId: String, onDismiss: (Boolean) -> Unit) {
    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId")).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId")).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(appIntent)
    } catch (_: Exception) {
        context.startActivity(webIntent)
    }
    onDismiss(true)
}
