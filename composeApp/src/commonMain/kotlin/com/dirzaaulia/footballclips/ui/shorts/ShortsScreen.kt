package com.dirzaaulia.footballclips.ui.shorts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.domain.model.FootballShort
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle
import com.dirzaaulia.footballclips.ui.player.YouTubePlayerView

@Composable
fun ShortsRoute(
    viewModel: ShortsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ShortsUiEffect.OpenExternalYouTube -> {
                    try {
                        uriHandler.openUri(effect.url)
                    } catch (_: Throwable) {}
                }
                is ShortsUiEffect.ShowToast -> {}
            }
        }
    }

    when (val state = uiState) {
        is ShortsUiState.Loading -> ShortsScreenShimmer(modifier = modifier)
        is ShortsUiState.Error -> ShortsErrorContent(message = state.message, modifier = modifier)
        is ShortsUiState.Success -> {
            ShortsPagerContent(
                state = state,
                onAction = viewModel::onAction,
                onOpenYouTube = viewModel::openYouTubeOutlink,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ShortsErrorContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message, color = Color.White)
    }
}

@Composable
private fun ShortsPagerContent(
    state: ShortsUiState.Success,
    onAction: (ShortsUiAction) -> Unit,
    onOpenYouTube: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { state.items.size })

    LaunchedEffect(pagerState.currentPage, state.items.size) {
        onAction(ShortsUiAction.OnPageChanged(pagerState.currentPage))
        if (pagerState.currentPage >= state.items.size - 4) {
            onAction(ShortsUiAction.LoadMore)
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize().background(Color.Black),
        key = { state.items[it].stableId },
    ) { page ->
        when (val item = state.items[page]) {
            is ShortsFeedItem.Video -> {
                ShortVideoPage(
                    short = item.short,
                    isCurrent = page == pagerState.currentPage,
                    isPlaying = state.isPlaying,
                    isMuted = state.isMuted,
                    onAction = onAction,
                    onOpenYouTube = { onOpenYouTube(item.short.youtubeVideoId) },
                )
            }
            is ShortsFeedItem.Ad -> {
                ShortAdPage(
                    adIndex = item.adIndex,
                    onAdFailed = { onAction(ShortsUiAction.OnAdFailed(item.adIndex)) },
                )
            }
        }
    }
}

@Composable
private fun ShortAdPage(
    adIndex: Int,
    onAdFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0B0E))
            .padding(horizontal = 16.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        BannerAdItem(
            modifier = Modifier.fillMaxSize(),
            style = NativeAdStyle.SHORTS,
            onAdFailed = onAdFailed,
        )
    }
}

@Composable
private fun ShortVideoPage(
    short: FootballShort,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isMuted: Boolean,
    onAction: (ShortsUiAction) -> Unit,
    onOpenYouTube: () -> Unit,
) {
    var isPlaybackActive by remember(short.youtubeVideoId) { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isCurrent) {
            YouTubePlayerView(
                videoId = short.youtubeVideoId,
                backupVideoId = null,
                regionInfo = null,
                backupRegionInfo = null,
                modifier = Modifier.fillMaxSize(),
                isPlaying = isPlaying,
                isMuted = isMuted,
                onDismiss = { isPlaybackActive = true },
                onPlaybackStarted = { isPlaybackActive = true },
                isShort = true,
            )
        }

        ShortVideoPosterCover(
            thumbnailUrl = short.thumbnailUrl,
            title = short.title,
            visible = !isCurrent || !isPlaybackActive,
        )

        ShortsOverlayControls(
            short = short,
            isPlaying = isPlaying,
            isMuted = isMuted,
            onTogglePlayPause = { onAction(ShortsUiAction.TogglePlayPause) },
            onToggleMute = { onAction(ShortsUiAction.ToggleMute) },
            onShare = {},
            onOpenYouTube = onOpenYouTube,
        )
    }
}

@Composable
private fun ShortVideoPosterCover(
    thumbnailUrl: String,
    title: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(animationSpec = tween(400)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                        ),
                    ),
            )
        }
    }
}
