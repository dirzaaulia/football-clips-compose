package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.*
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    listState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    onVideoClick: (HighlightUiItem, onDismiss: (Boolean) -> Unit) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val offerings by viewModel.offerings.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val isAdsRemoved by viewModel.isAdsRemoved.collectAsState()
    val profile by viewModel.currentUserProfile.collectAsState(null)
    val customerInfo by viewModel.customerInfo.collectAsState(null)
    val showExternalHighlights by viewModel.showExternalHighlights.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isSpoilerFreeMode by viewModel.isSpoilerFreeMode.collectAsState()
    val isSpoilerFreeConfigured by viewModel.isSpoilerFreeConfigured.collectAsState()
    val isDebugPremium by viewModel.isDebugPremium.collectAsState()
    val isForceNonPremium by viewModel.isForceNonPremium.collectAsState()
    val isBillingLoading by viewModel.isBillingLoading.collectAsState()

    var revealedItemIds by remember { mutableStateOf(emptySet<String>()) }
    var titleTapCount by remember { mutableStateOf(0) }
    var lastTitleTapTime by remember { mutableStateOf(0L) }
    var showDevScreen by remember { mutableStateOf(false) }

    var showPaywall by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val filterState by viewModel.filterState.collectAsState()
    val selectedLeagueId by viewModel.selectedLeagueId.collectAsState()
    val favoriteClubs by viewModel.favoriteClubs.collectAsState()
    var showMyClubsSheet by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.consumePendingInterstitial()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxHeight < 500.dp
        val isWeb = maxWidth > 840.dp && !isLandscape
        val isScrolled by remember {
            derivedStateOf {
                val hasContent = (uiState as? HomeState.Success)?.items?.isNotEmpty() == true
                val index = if (isWeb) gridState.firstVisibleItemIndex else listState.firstVisibleItemIndex
                val offset = if (isWeb) gridState.firstVisibleItemScrollOffset else listState.firstVisibleItemScrollOffset
                hasContent && (index > 0 || offset > 30)
            }
        }

        val shouldLoadMore = remember(uiState, isWeb) {
            derivedStateOf {
                val successState = uiState as? HomeState.Success
                val canLoadMore = successState?.canLoadMore == true
                val isLoadingMore = successState?.isLoadingMore == true
                val hasLoadMoreError = successState?.loadMoreError != null

                if (!canLoadMore || isLoadingMore || hasLoadMoreError) {
                    return@derivedStateOf false
                }

                val total = if (isWeb) gridState.layoutInfo.totalItemsCount else listState.layoutInfo.totalItemsCount
                val lastVisible = if (isWeb) gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                else listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                val threshold = if (isWeb) 10 else 5
                total > 0 && lastVisible >= total - threshold
            }
        }

        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value) viewModel.loadMore()
        }

        LaunchedEffect(isScrolled) {
            viewModel.setScrollToTopVisible(isScrolled)
        }

        LaunchedEffect(Unit) {
            viewModel.errorMessage.collect { message -> snackbarHostState.showSnackbar(message) }
        }

        LaunchedEffect(Unit) {
            viewModel.scrollToTopEvent.collect {
                if (isWeb) gridState.animateScrollToItem(0) else listState.animateScrollToItem(0)
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(bottom = 120.dp)) },
            topBar = {
                HomeScreenTopBar(
                    isWeb = isWeb,
                    isDarkMode = isDarkMode,
                    isPremium = isPremium,
                    isSpoilerFreeMode = isSpoilerFreeMode,
                    favoriteClubsCount = favoriteClubs.size,
                    isScrolled = isScrolled,
                    onTitleTap = {
                        val now = Clock.System.now().toEpochMilliseconds()
                        if (now - lastTitleTapTime < 800) titleTapCount++ else titleTapCount = 1
                        lastTitleTapTime = now
                        if (titleTapCount >= 3) { titleTapCount = 0; showDevScreen = true }
                    },
                    onDarkModeToggle = { dark -> viewModel.setDarkMode(dark) },
                    onToggleSpoilerFree = { viewModel.toggleSpoilerFreeMode() },
                    onShowMyClubs = { showMyClubsSheet = true },
                    onShowPaywall = { showPaywall = true },
                    onShowFilter = { showFilterSheet = true }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                HomeScreenBody(
                    uiState = uiState,
                    isWeb = isWeb,
                    isLandscape = isLandscape,
                    selectedLeagueId = selectedLeagueId,
                    filterState = filterState,
                    showExternalHighlights = showExternalHighlights,
                    isAdsRemoved = isAdsRemoved,
                    isSpoilerFreeMode = isSpoilerFreeMode,
                    revealedItemIds = revealedItemIds,
                    onRevealItem = { id -> revealedItemIds = revealedItemIds + id },
                    gridState = gridState,
                    listState = listState,
                    onVideoClick = onVideoClick,
                    viewModel = viewModel
                )
            }
        }

        val composeAuth = koinInject<ComposeAuth>()
        val googleSignInAction = composeAuth.rememberSignInWithGoogle(
            onResult = { result ->
                viewModel.setVerifyingAuth(false)
                when (result) {
                    is NativeSignInResult.Success -> viewModel.onSignInSuccess()
                    is NativeSignInResult.Error -> {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Native error: ${result.message}, opening browser...") }
                        viewModel.signIn()
                    }
                    is NativeSignInResult.NetworkError -> {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Network error: ${result.message}") }
                    }
                    NativeSignInResult.ClosedByUser -> {}
                }
            },
            fallback = {
                viewModel.setVerifyingAuth(false)
                coroutineScope.launch { snackbarHostState.showSnackbar("Native sign in unavailable, opening browser...") }
                viewModel.signIn()
            }
        )

        val isFilterSheetVisible by viewModel.showFilterSheet.collectAsState()
        val isMyClubsSheetVisible by viewModel.showMyClubsSheet.collectAsState()

        HomeScreenDialogs(
            showPaywall = showPaywall,
            showFilterSheet = showFilterSheet || isFilterSheetVisible,
            showDevScreen = showDevScreen,
            isPremium = isPremium,
            profile = profile,
            customerInfo = customerInfo,
            offerings = offerings,
            isBillingLoading = isBillingLoading,
            filterState = filterState,
            isLoadingMore = (uiState as? HomeState.Success)?.isLoadingMore == true,
            showExternalHighlights = showExternalHighlights,
            isDebugPremium = isDebugPremium,
            isForceNonPremium = isForceNonPremium,
            viewModel = viewModel,
            onSignIn = { viewModel.setVerifyingAuth(true); googleSignInAction.startFlow() },
            onDismissPaywall = { showPaywall = false },
            onDismissFilter = { showFilterSheet = false; viewModel.setFilterSheetVisible(false) },
            onDismissDev = { showDevScreen = false }
        )

        if (showMyClubsSheet || isMyClubsSheetVisible) {
            MyClubsBottomSheet(
                selectedClubIds = favoriteClubs,
                onToggleClub = { viewModel.toggleFavoriteClub(it) },
                onDismissRequest = { showMyClubsSheet = false; viewModel.setMyClubsSheetVisible(false) }
            )
        }

        if (isSpoilerFreeConfigured == false) {
            SpoilerEducationDialog(
                onSelectPreference = { isSpoilerFree ->
                    viewModel.setSpoilerFreePreference(isSpoilerFree)
                }
            )
        }
    }
}
