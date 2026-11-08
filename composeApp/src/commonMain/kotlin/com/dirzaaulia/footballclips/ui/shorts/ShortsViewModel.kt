package com.dirzaaulia.footballclips.ui.shorts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.repository.ShortsRepository
import com.dirzaaulia.footballclips.domain.model.FootballShort
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShortsViewModel(
    private val repository: ShortsRepository,
    private val billingManager: BillingManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShortsUiState>(ShortsUiState.Loading)
    val uiState: StateFlow<ShortsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ShortsUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val isPremium: StateFlow<Boolean> = billingManager.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        loadShorts()
    }

    fun onAction(action: ShortsUiAction) {
        when (action) {
            is ShortsUiAction.OnPageChanged -> updatePageIndex(action.pageIndex)
            is ShortsUiAction.TogglePlayPause -> togglePlayPause()
            is ShortsUiAction.ToggleMute -> toggleMute()
            is ShortsUiAction.LikeShort -> handleLike(action.videoId)
            is ShortsUiAction.SelectCategory -> loadShorts(action.category)
            is ShortsUiAction.Retry -> loadShorts()
        }
    }

    private fun loadShorts(category: String? = null) {
        viewModelScope.launch {
            _uiState.value = ShortsUiState.Loading
            when (val result = repository.getShorts(category = category)) {
                is NetworkResult.Success -> {
                    val feedItems = buildShortsFeedItems(result.data, isPremium.value)
                    _uiState.value = ShortsUiState.Success(
                        items = feedItems,
                        selectedCategory = category,
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = ShortsUiState.Error(result.message)
                }
                is NetworkResult.Exception -> {
                    _uiState.value = ShortsUiState.Error(result.e.message ?: "Failed to load shorts")
                }
            }
        }
    }

    private fun updatePageIndex(index: Int) {
        val current = _uiState.value as? ShortsUiState.Success ?: return
        _uiState.value = current.copy(currentVideoIndex = index, isPlaying = true)
    }

    private fun togglePlayPause() {
        val current = _uiState.value as? ShortsUiState.Success ?: return
        _uiState.value = current.copy(isPlaying = !current.isPlaying)
    }

    private fun toggleMute() {
        val current = _uiState.value as? ShortsUiState.Success ?: return
        _uiState.value = current.copy(isMuted = !current.isMuted)
    }

    private fun handleLike(videoId: String) {
        viewModelScope.launch {
            _effects.send(ShortsUiEffect.ShowToast("Added to liked shorts!"))
        }
    }

    fun openYouTubeOutlink(videoId: String) {
        viewModelScope.launch {
            _effects.send(ShortsUiEffect.OpenExternalYouTube("https://youtube.com/shorts/$videoId"))
        }
    }
}

internal fun buildShortsFeedItems(
    shorts: List<FootballShort>,
    premium: Boolean,
): List<ShortsFeedItem> {
    if (premium || shorts.isEmpty()) {
        return shorts.map { ShortsFeedItem.Video(it) }
    }
    val items = mutableListOf<ShortsFeedItem>()
    var adIndex = 0
    shorts.forEachIndexed { index, short ->
        items.add(ShortsFeedItem.Video(short))
        if ((index + 1) % 6 == 0) {
            items.add(ShortsFeedItem.Ad(adIndex++))
        }
    }
    return items
}

