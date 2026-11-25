package com.dirzaaulia.footballclips.ui.shorts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dirzaaulia.footballclips.data.billing.BillingManager
import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.dirzaaulia.footballclips.data.model.NetworkResult
import com.dirzaaulia.footballclips.data.repository.ShortsRepository
import com.dirzaaulia.footballclips.domain.model.FootballShort
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShortsViewModel(
    private val repository: ShortsRepository,
    private val billingManager: BillingManager,
    private val preferenceManager: PreferenceManager? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShortsUiState>(ShortsUiState.Loading)
    val uiState: StateFlow<ShortsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ShortsUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var currentOffset = 0
    private var isLoadingMore = false
    private var hasMoreData = true
    private val cachedShorts = mutableListOf<FootballShort>()

    val isAdsRemoved: StateFlow<Boolean> = if (preferenceManager != null) {
        combine(billingManager.isPremium, preferenceManager.isAdsRemoved) { premium, adsRemoved ->
            premium || adsRemoved
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    } else {
        billingManager.isPremium.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    }

    init {
        loadShorts()
        observeAdsRemoved()
    }

    private fun observeAdsRemoved() = viewModelScope.launch {
        isAdsRemoved.collectLatest { removed ->
            val curr = _uiState.value as? ShortsUiState.Success ?: return@collectLatest
            val raw = curr.items.filterIsInstance<ShortsFeedItem.Video>().map { it.short }
            _uiState.value = curr.copy(items = buildShortsFeedItems(raw, removed))
        }
    }

    fun onAction(action: ShortsUiAction) {
        when (action) {
            is ShortsUiAction.OnPageChanged -> updatePageIndex(action.pageIndex)
            is ShortsUiAction.TogglePlayPause -> togglePlayPause()
            is ShortsUiAction.ToggleMute -> toggleMute()
            is ShortsUiAction.LikeShort -> handleLike(action.videoId)
            is ShortsUiAction.SelectCategory -> loadShorts(action.category)
            is ShortsUiAction.Retry -> loadShorts()
            is ShortsUiAction.LoadMore -> loadMoreShorts()
            is ShortsUiAction.OnAdFailed -> handleAdFailed(action.adIndex)
        }
    }

    private fun loadShorts(category: String? = null) = viewModelScope.launch {
        _uiState.value = ShortsUiState.Loading
        currentOffset = 0
        hasMoreData = true
        cachedShorts.clear()
        when (val res = repository.getShorts(category, 20, 0)) {
            is NetworkResult.Success -> {
                cachedShorts.addAll(res.data)
                currentOffset = res.data.size
                hasMoreData = res.data.size >= 20
                val items = buildShortsFeedItems(res.data, isAdsRemoved.value)
                _uiState.value = ShortsUiState.Success(items = items, selectedCategory = category)
            }
            is NetworkResult.Error -> _uiState.value = ShortsUiState.Error(res.message)
            is NetworkResult.Exception -> _uiState.value = ShortsUiState.Error(res.e.message ?: "Failed")
        }
    }

    private fun loadMoreShorts() {
        val current = _uiState.value as? ShortsUiState.Success ?: return
        if (isLoadingMore) return
        isLoadingMore = true
        viewModelScope.launch {
            try {
                if (hasMoreData) {
                    when (val res = repository.getShorts(current.selectedCategory, 20, currentOffset)) {
                        is NetworkResult.Success -> handleMoreSuccess(current, res.data)
                        else -> triggerLoopFallback(current)
                    }
                } else triggerLoopFallback(current)
            } finally {
                isLoadingMore = false
            }
        }
    }

    private fun handleMoreSuccess(current: ShortsUiState.Success, data: List<FootballShort>) {
        if (data.isNotEmpty()) {
            cachedShorts.addAll(data)
            currentOffset += data.size
            hasMoreData = data.size >= 20
            val items = appendShortsFeedItems(current.items, data, isAdsRemoved.value)
            _uiState.value = current.copy(items = items)
        } else {
            hasMoreData = false
            triggerLoopFallback(current)
        }
    }

    private fun triggerLoopFallback(current: ShortsUiState.Success) {
        _uiState.value = current.copy(items = createLoopFallbackBatch(cachedShorts, current.items, isAdsRemoved.value))
    }

    private fun handleAdFailed(adIndex: Int) = (_uiState.value as? ShortsUiState.Success)?.let {
        _uiState.value = it.copy(items = filterOutFailedAd(it.items, adIndex))
    }

    private fun updatePageIndex(index: Int) = (_uiState.value as? ShortsUiState.Success)?.let {
        _uiState.value = it.copy(currentVideoIndex = index, isPlaying = true)
    }

    private fun togglePlayPause() = (_uiState.value as? ShortsUiState.Success)?.let {
        _uiState.value = it.copy(isPlaying = !it.isPlaying)
    }

    private fun toggleMute() = (_uiState.value as? ShortsUiState.Success)?.let {
        _uiState.value = it.copy(isMuted = !it.isMuted)
    }

    private fun handleLike(videoId: String) = viewModelScope.launch {
        _effects.send(ShortsUiEffect.ShowToast("Added to liked shorts!"))
    }

    fun openYouTubeOutlink(videoId: String) = viewModelScope.launch {
        _effects.send(ShortsUiEffect.OpenExternalYouTube("https://youtube.com/shorts/$videoId"))
    }
}
