package com.dirzaaulia.footballclips.ui.player

import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dirzaaulia.footballclips.domain.model.RegionRestriction
import com.dirzaaulia.footballclips.ui.home.findActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer as PierYouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView as PierYouTubePlayerView
import kotlinx.coroutines.delay

private const val TAG = "YouTubePlayerView"

@Composable
actual fun YouTubePlayerView(
    videoId: String,
    backupVideoId: String?,
    regionInfo: RegionRestriction?,
    backupRegionInfo: RegionRestriction?,
    modifier: Modifier,
    isPlaying: Boolean,
    isMuted: Boolean,
    onDismiss: (Boolean) -> Unit,
    onPlaybackStarted: () -> Unit,
    isShort: Boolean,
    shouldAutoPlay: Boolean,
    onAutoMuted: () -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current
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
    var customFullscreenView by remember { mutableStateOf<View?>(null) }
    var errorState by remember(initialError) { mutableStateOf(initialError) }
    var isLoading by remember(currentVideoId) { mutableStateOf(true) }
    var retryNonce by remember { mutableStateOf(0) }
    var activePlayer by remember(initialVideoId) { mutableStateOf<PierYouTubePlayer?>(null) }

    LaunchedEffect(currentVideoId, isLoading, errorState, retryNonce, shouldAutoPlay) {
        if (shouldAutoPlay && isLoading && errorState == PlayerErrorState.NONE) {
            delay(8000)
            if (shouldAutoPlay && isLoading && errorState == PlayerErrorState.NONE) {
                if (backupVideoId != null && !hasSwitchedToBackup && backupVideoId != currentVideoId && isBackupAllowed) {
                    hasSwitchedToBackup = true
                    currentVideoId = backupVideoId
                    isLoading = true
                    activePlayer?.loadVideo(backupVideoId, 0f)
                } else {
                    errorState = PlayerErrorState.NETWORK_TIMEOUT
                    isLoading = false
                    onPlaybackStarted()
                }
            }
        }
    }

    LaunchedEffect(isPlaying, shouldAutoPlay, activePlayer) {
        val player = activePlayer ?: return@LaunchedEffect
        if (isPlaying && shouldAutoPlay) player.play() else player.pause()
    }

    LaunchedEffect(isMuted, activePlayer) {
        val player = activePlayer ?: return@LaunchedEffect
        if (isMuted) player.mute() else player.unMute()
    }

    DisposableEffect(customFullscreenView) {
        activity?.requestedOrientation = if (customFullscreenView != null) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    Box(modifier = resolvePlayerModifier(modifier, customFullscreenView)) {
        if (errorState == PlayerErrorState.NONE) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PierYouTubePlayerView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        enableAutomaticInitialization = false
                        findChildWebView(this)?.settings?.apply {
                            mediaPlaybackRequiresUserGesture = false
                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                        }
                        lifecycleOwner.lifecycle.addObserver(this)
                        addFullscreenListener(object : FullscreenListener {
                            override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                                customFullscreenView = fullscreenView
                            }

                            override fun onExitFullscreen() {
                                customFullscreenView = null
                            }
                        })
                        val options = IFramePlayerOptions.Builder(ctx)
                            .controls(if (isShort) 0 else 1)
                            .rel(0)
                            .ivLoadPolicy(3)
                            .fullscreen(if (isShort) 0 else 1)
                            .origin("https://dirzaaulia.com")
                            .build()
                        initialize(
                            object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: PierYouTubePlayer) {
                                    activePlayer = youTubePlayer
                                    if (isMuted) youTubePlayer.mute() else youTubePlayer.unMute()
                                    if (isPlaying && shouldAutoPlay) {
                                        youTubePlayer.loadVideo(currentVideoId, 0f)
                                    } else {
                                        youTubePlayer.cueVideo(currentVideoId, 0f)
                                    }
                                    isLoading = false
                                }

                                override fun onStateChange(
                                    youTubePlayer: PierYouTubePlayer,
                                    state: PlayerConstants.PlayerState,
                                ) {
                                    Log.d(TAG, "onStateChange($currentVideoId): $state")
                                    when (state) {
                                        PlayerConstants.PlayerState.PLAYING -> {
                                            isLoading = false
                                            onPlaybackStarted()
                                        }
                                        PlayerConstants.PlayerState.BUFFERING -> {
                                            if (isShort && shouldAutoPlay) onPlaybackStarted()
                                        }
                                        PlayerConstants.PlayerState.PAUSED -> isLoading = false
                                        PlayerConstants.PlayerState.ENDED -> {
                                            if (isShort) {
                                                youTubePlayer.seekTo(0f)
                                                youTubePlayer.play()
                                            }
                                        }
                                        else -> {}
                                    }
                                }

                                override fun onError(
                                    youTubePlayer: PierYouTubePlayer,
                                    error: PlayerConstants.PlayerError,
                                ) {
                                    Log.e(TAG, "onError($currentVideoId): $error")
                                    isLoading = false
                                    onPlaybackStarted()
                                    val isEmbedErr = error == PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER ||
                                        error == PlayerConstants.PlayerError.VIDEO_NOT_FOUND
                                    if (isEmbedErr && backupVideoId != null && !hasSwitchedToBackup && backupVideoId != currentVideoId && isBackupAllowed) {
                                        hasSwitchedToBackup = true
                                        currentVideoId = backupVideoId
                                        isLoading = true
                                        youTubePlayer.loadVideo(backupVideoId, 0f)
                                    } else if (isEmbedErr) {
                                        errorState = PlayerErrorState.NOT_EMBEDDABLE
                                    } else {
                                        errorState = PlayerErrorState.USER_SIDE_ERROR
                                    }
                                }
                            },
                            options,
                        )
                    }
                },
                onRelease = { view ->
                    lifecycleOwner.lifecycle.removeObserver(view)
                    view.release()
                },
            )
        }

        if (isLoading && errorState == PlayerErrorState.NONE && shouldAutoPlay) {
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
                activePlayer?.loadVideo(currentVideoId, 0f)
            },
            onDismiss = onDismiss,
        )

        if (hasSwitchedToBackup && errorState == PlayerErrorState.NONE) {
            PlayerBackupPill()
        }

        customFullscreenView?.let { FullscreenPlayerOverlay(customView = it) }
    }
}

private fun findChildWebView(view: View): WebView? {
    if (view is WebView) return view
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            findChildWebView(view.getChildAt(i))?.let { return it }
        }
    }
    return null
}

private fun resolvePlayerModifier(modifier: Modifier, fullscreenView: View?): Modifier {
    return if (fullscreenView != null) {
        modifier.background(Color.Black)
    } else {
        modifier
            .background(Color.Black)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF7098F8).copy(alpha = 0.35f), Color(0xFF1E293B).copy(alpha = 0.2f)),
                ),
                shape = RoundedCornerShape(16.dp),
            )
            .clip(RoundedCornerShape(16.dp))
    }
}
