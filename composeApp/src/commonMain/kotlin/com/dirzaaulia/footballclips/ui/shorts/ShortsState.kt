package com.dirzaaulia.footballclips.ui.shorts

import com.dirzaaulia.footballclips.domain.model.FootballShort

sealed interface ShortsFeedItem {
    val stableId: String

    data class Video(val short: FootballShort) : ShortsFeedItem {
        override val stableId: String = short.youtubeVideoId
    }

    data class Ad(val adIndex: Int) : ShortsFeedItem {
        override val stableId: String = "ad_$adIndex"
    }
}

sealed interface ShortsUiState {
    data object Loading : ShortsUiState
    data class Error(val message: String) : ShortsUiState
    data class Success(
        val items: List<ShortsFeedItem>,
        val currentVideoIndex: Int = 0,
        val isPlaying: Boolean = true,
        val selectedCategory: String? = null,
        val isMuted: Boolean = false,
    ) : ShortsUiState
}

sealed interface ShortsUiAction {
    data class OnPageChanged(val pageIndex: Int) : ShortsUiAction
    data object TogglePlayPause : ShortsUiAction
    data object ToggleMute : ShortsUiAction
    data class LikeShort(val videoId: String) : ShortsUiAction
    data class SelectCategory(val category: String?) : ShortsUiAction
    data object Retry : ShortsUiAction
}

sealed interface ShortsUiEffect {
    data class ShowToast(val message: String) : ShortsUiEffect
    data class OpenExternalYouTube(val url: String) : ShortsUiEffect
}
