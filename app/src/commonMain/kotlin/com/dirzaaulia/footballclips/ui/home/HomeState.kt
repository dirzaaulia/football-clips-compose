package com.dirzaaulia.footballclips.ui.home

import com.dirzaaulia.footballclips.data.model.HighlightUiItem

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(
        val items: List<HighlightUiItem>,
        val isAdsRemoved: Boolean,
        val canLoadMore: Boolean = false,
        val isLoadingMore: Boolean = false
    ) : HomeState
    data class Error(val message: String) : HomeState
}
