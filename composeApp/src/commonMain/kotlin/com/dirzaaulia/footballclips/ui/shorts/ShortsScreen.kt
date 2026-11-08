package com.dirzaaulia.footballclips.ui.shorts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

    when (val state = uiState) {
        is ShortsUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
        }
        is ShortsUiState.Error -> {
            Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = Color.White)
            }
        }
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
private fun ShortsPagerContent(
    state: ShortsUiState.Success,
    onAction: (ShortsUiAction) -> Unit,
    onOpenYouTube: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { state.items.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onAction(ShortsUiAction.OnPageChanged(page))
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize().background(Color.Black),
        key = { state.items[it].stableId },
    ) { page ->
        when (val item = state.items[page]) {
            is ShortsFeedItem.Video -> {
                val isCurrent = page == pagerState.currentPage
                ShortVideoPage(
                    short = item.short,
                    isCurrent = isCurrent,
                    isPlaying = state.isPlaying,
                    isMuted = state.isMuted,
                    onAction = onAction,
                    onOpenYouTube = { onOpenYouTube(item.short.youtubeVideoId) },
                )
            }
            is ShortsFeedItem.Ad -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center,
                ) {
                    BannerAdItem(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        style = NativeAdStyle.HIGHLIGHT,
                    )
                }
            }
        }
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
    Box(modifier = Modifier.fillMaxSize()) {
        if (isCurrent) {
            YouTubePlayerView(
                videoId = short.youtubeVideoId,
                backupVideoId = null,
                regionInfo = null,
                backupRegionInfo = null,
                modifier = Modifier.fillMaxSize(),
                onDismiss = {},
            )
        } else {
            AsyncImage(
                model = short.thumbnailUrl,
                contentDescription = short.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        ShortsOverlayControls(
            short = short,
            isPlaying = isPlaying,
            isMuted = isMuted,
            onTogglePlayPause = { onAction(ShortsUiAction.TogglePlayPause) },
            onToggleMute = { onAction(ShortsUiAction.ToggleMute) },
            onLike = { onAction(ShortsUiAction.LikeShort(short.youtubeVideoId)) },
            onShare = {},
            onOpenYouTube = onOpenYouTube,
        )
    }
}
