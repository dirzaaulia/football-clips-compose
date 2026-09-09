package com.dirzaaulia.footballclips.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dirzaaulia.footballclips.ui.theme.rememberThemeCapture
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.BigLeaguesQuickFilterBar
import com.dirzaaulia.footballclips.ui.components.DeveloperOptionsBottomSheet
import com.dirzaaulia.footballclips.ui.components.EmptyState
import com.dirzaaulia.footballclips.ui.components.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.FilterBottomSheet
import com.dirzaaulia.footballclips.ui.components.HeroCard
import com.dirzaaulia.footballclips.ui.components.PaywallBottomSheet
import com.dirzaaulia.footballclips.ui.components.VerticalHighlightCard
import com.dirzaaulia.footballclips.ui.components.WebHighlightCard
import com.dirzaaulia.footballclips.ui.score.components.MatchCard
import com.dirzaaulia.footballclips.util.isDebugBuild
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val isDebugPremium by viewModel.isDebugPremium.collectAsState()
    val isForceNonPremium by viewModel.isForceNonPremium.collectAsState()
    val isBillingLoading by viewModel.isBillingLoading.collectAsState()

    var titleTapCount by remember { mutableStateOf(0) }
    var lastTitleTapTime by remember { mutableStateOf(0L) }
    var showDevScreen by remember { mutableStateOf(false) }

    var showPaywall by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val filterState by viewModel.filterState.collectAsState()
    val selectedLeagueId by viewModel.selectedLeagueId.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.consumePendingInterstitial()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWeb = maxWidth > 840.dp
        
        val shouldLoadMore = remember {
            derivedStateOf {
                val totalItemsCount = if (isWeb) gridState.layoutInfo.totalItemsCount else listState.layoutInfo.totalItemsCount
                val lastVisibleItemIndex = if (isWeb) {
                    gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                } else {
                    listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                }
                
                totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 5
            }
        }

        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value) {
                viewModel.loadMore()
            }
        }

        LaunchedEffect(Unit) {
            viewModel.errorMessage.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }

        Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { 
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.padding(bottom = 120.dp)
                    ) 
                },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (isWeb) "Football Highlights & Clips" else "Highlights",
                                style = if (isWeb) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = if (isWeb) 1.sp else 0.sp,
                                modifier = Modifier.clickable {
                                    if (!isWeb && isDebugBuild) {
                                        val now = Clock.System.now().toEpochMilliseconds()
                                        if (now - lastTitleTapTime < 800) {
                                            titleTapCount++
                                        } else {
                                            titleTapCount = 1
                                        }
                                        lastTitleTapTime = now

                                        if (titleTapCount >= 3) {
                                            titleTapCount = 0
                                            showDevScreen = true
                                        }
                                    }
                                }
                            )
                        },
                        actions = {
                            if (!isWeb) {
                                var lastThemeToggleTime by remember { mutableStateOf(0L) }
                                val triggerThemeCapture = rememberThemeCapture()
                                var toggleCenter by remember { mutableStateOf(Offset.Zero) }

                                IconButton(
                                    onClick = {
                                        val now = Clock.System.now().toEpochMilliseconds()
                                        if (now - lastThemeToggleTime >= 750) {
                                            lastThemeToggleTime = now
                                            triggerThemeCapture(toggleCenter)
                                            viewModel.setDarkMode(!isDarkMode)
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .onGloballyPositioned { coordinates ->
                                            val pos = coordinates.positionInWindow()
                                            val size = coordinates.size
                                            toggleCenter = Offset(
                                                x = pos.x + size.width / 2f,
                                                y = pos.y + size.height / 2f
                                            )
                                        }
                                ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                        Button(
                            onClick = { showPaywall = true },
                            modifier = Modifier
                                .padding(end = if (isWeb) 24.dp else 8.dp)
                                .height(if (isWeb) 48.dp else 40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPremium) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                                contentColor = if (isPremium) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                if (isPremium) Icons.Default.VideoLibrary else Icons.Default.Diamond, 
                                contentDescription = null, 
                                modifier = Modifier.size(if (isWeb) 20.dp else 18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isPremium) "PREMIUM" else "GO PREMIUM", 
                                fontWeight = FontWeight.Black,
                                style = if (isWeb) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            },
            floatingActionButton = {
                if (!isWeb) {
                    val showScrollToTop by remember {
                        derivedStateOf { listState.firstVisibleItemIndex > 1 }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 100.dp, end = 8.dp)
                    ) {
                        // Scroll To Top Pill (Appears when scrolled)
                        AnimatedVisibility(
                            visible = showScrollToTop,
                            enter = fadeIn() + slideInVertically { it / 2 } + scaleIn(),
                            exit = fadeOut() + slideOutVertically { it / 2 } + scaleOut()
                        ) {
                            Surface(
                                onClick = {
                                    coroutineScope.launch { listState.animateScrollToItem(0) }
                                },
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                                tonalElevation = 6.dp,
                                shadowElevation = 8.dp,
                                border = BorderStroke(
                                    0.5.dp, 
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Scroll to top",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Top",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Quick Filter Pill
                        Surface(
                            onClick = { showFilterSheet = true },
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                            tonalElevation = 6.dp,
                            shadowElevation = 8.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp, 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Filter",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(400, easing = EaseOutCubic)) +
                                slideInVertically(animationSpec = tween(400, easing = EaseOutCubic)) { it / 10 })
                            .togetherWith(
                                fadeOut(animationSpec = tween(200, easing = EaseInCubic))
                            )
                    },
                    label = "HomeScreenStateTransition",
                    modifier = Modifier.fillMaxSize()
                ) { state ->
                    when (state) {
                        is HomeState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                ExpressiveLoadingIndicator(
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 5.dp
                                )
                            }
                        }
                        is HomeState.Success -> {
                        val items = state.items
                        
                        val isFiltering = selectedLeagueId != null || filterState.searchQuery.isNotEmpty() || 
                                         filterState.selectedCountries.isNotEmpty() || filterState.selectedLeagues.isNotEmpty()
                        
                        val featuredHighlight = if (!isFiltering && !showExternalHighlights) {
                            items.firstOrNull { it !is HighlightUiItem.BannerAd }
                        } else null

                        val gridItems = if (featuredHighlight != null) {
                            items.filterIndexed { index, _ -> 
                                index != items.indexOf(featuredHighlight) 
                            }
                        } else {
                            items
                        }

                        if (isWeb) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                state = gridState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                if (featuredHighlight != null) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        if (isAdsRemoved) {
                                            HeroCard(
                                                item = featuredHighlight,
                                                isCinematic = true,
                                                onClick = {
                                                    onVideoClick(featuredHighlight) { isJumping ->
                                                        viewModel.setPendingInterstitial(true)
                                                        if (!isJumping) {
                                                            viewModel.consumePendingInterstitial()
                                                        }
                                                    }
                                                }
                                            )
                                        } else {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(IntrinsicSize.Min),
                                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                                            ) {
                                                Box(modifier = Modifier.weight(0.65f)) {
                                                    HeroCard(
                                                        item = featuredHighlight,
                                                        isCinematic = true,
                                                        onClick = {
                                                            onVideoClick(featuredHighlight) { isJumping ->
                                                                viewModel.setPendingInterstitial(true)
                                                                if (!isJumping) {
                                                                    viewModel.consumePendingInterstitial()
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .weight(0.35f)
                                                        .fillMaxHeight()
                                                ) {
                                                    BannerAdItem(
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    if (isAdsRemoved) {
                                        BigLeaguesQuickFilterBar(
                                            selectedLeagueId = selectedLeagueId,
                                            onLeagueSelected = viewModel::selectLeague
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Box(modifier = Modifier.weight(0.7f)) {
                                                BigLeaguesQuickFilterBar(
                                                    selectedLeagueId = selectedLeagueId,
                                                    onLeagueSelected = viewModel::selectLeague
                                                )
                                            }
                                            Box(modifier = Modifier.weight(0.3f)) {
                                                BannerAdItem(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(80.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (items.isEmpty()) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        EmptyState(
                                            title = "No Highlights Found",
                                            description = "Try selecting another league or check back later.",
                                            onActionClick = { viewModel.resetFilters() }
                                        )
                                    }
                                } else {
                                    items(
                                        items = gridItems,
                                        key = { item -> item.uniqueId }
                                    ) { item ->
                                        when (item) {
                                            is HighlightUiItem.SupabaseMatch, is HighlightUiItem.Highlight -> {
                                                WebHighlightCard(
                                                    item = item,
                                                    onClick = {
                                                        onVideoClick(item) { isJumping ->
                                                            viewModel.setPendingInterstitial(true)
                                                            if (!isJumping) {
                                                                viewModel.consumePendingInterstitial()
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                            is HighlightUiItem.BannerAd -> {
                                                BannerAdItem(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(16 / 9f)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!showExternalHighlights) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            TextButton(
                                                onClick = { viewModel.toggleExternalHighlights() }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Explore,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Explore More Highlights",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                item(span = { GridItemSpan(maxLineSpan) }) { 
                                    Spacer(modifier = Modifier.height(100.dp)) 
                                }
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 140.dp)
                            ) {
                                if (featuredHighlight != null) {
                                    item {
                                        HeroCard(
                                            item = featuredHighlight,
                                            onClick = {
                                                onVideoClick(featuredHighlight) { isJumping ->
                                                    viewModel.setPendingInterstitial(true)
                                                    if (!isJumping) {
                                                        viewModel.consumePendingInterstitial()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }

                                item {
                                    BigLeaguesQuickFilterBar(
                                        selectedLeagueId = selectedLeagueId,
                                        onLeagueSelected = viewModel::selectLeague
                                    )
                                }

                                if (items.isEmpty()) {
                                    item {
                                        EmptyState(
                                            title = "No Highlights Found",
                                            description = "Try selecting another league or check back later.",
                                            onActionClick = { viewModel.resetFilters() }
                                        )
                                    }
                                } else {
                                    items(
                                        items = gridItems,
                                        key = { item -> item.uniqueId }
                                    ) { item ->
                                        when (item) {
                                            is HighlightUiItem.SupabaseMatch -> {
                                                MatchCard(
                                                    match = item.match,
                                                    onWatchHighlightClick = {
                                                        onVideoClick(item) { isJumping ->
                                                            viewModel.setPendingInterstitial(true)
                                                            if (!isJumping) {
                                                                viewModel.consumePendingInterstitial()
                                                            }
                                                        }
                                                    },
                                                    showStatus = false,
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                            }
                                            is HighlightUiItem.Highlight -> {
                                                VerticalHighlightCard(
                                                    highlight = item.highlight,
                                                    onClick = {
                                                        onVideoClick(item) { isJumping ->
                                                            viewModel.setPendingInterstitial(true)
                                                            if (!isJumping) {
                                                                viewModel.consumePendingInterstitial()
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                            is HighlightUiItem.BannerAd -> {
                                                BannerAdItem()
                                            }
                                        }
                                    }
                                }
                                
                                if (!showExternalHighlights) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            TextButton(
                                                onClick = { viewModel.toggleExternalHighlights() }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Explore,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Explore More Highlights",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (state.isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            ExpressiveLoadingIndicator(
                                                modifier = Modifier.size(36.dp),
                                                strokeWidth = 4.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is HomeState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = state.message)
                        }
                    }
                }
                }
            }

        val composeAuth = koinInject<ComposeAuth>()
        val googleSignInAction = composeAuth.rememberSignInWithGoogle(
            onResult = { result ->
                when (result) {
                    is NativeSignInResult.Success -> {
                        println("Native Google Sign-In Success!")
                        viewModel.setVerifyingAuth(false)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Sign in successful!")
                        }
                    }
                    is NativeSignInResult.Error -> {
                        println("Native Google Sign-In Error: ${result.message}")
                        viewModel.setVerifyingAuth(false)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Native error: ${result.message}, opening browser...")
                        }
                        viewModel.signIn()
                    }
                    is NativeSignInResult.NetworkError -> {
                        println("Native Google Sign-In NetworkError: ${result.message}")
                        viewModel.setVerifyingAuth(false)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Network error: ${result.message}")
                        }
                    }
                    NativeSignInResult.ClosedByUser -> {
                        println("Native Google Sign-In ClosedByUser")
                        viewModel.setVerifyingAuth(false)
                    }
                }
            },
            fallback = {
                println("Native Sign-In Fallback triggered")
                viewModel.setVerifyingAuth(false)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Native sign in unavailable, opening browser...")
                }
                viewModel.signIn()
            }
        )

        if (showPaywall) {
            PaywallBottomSheet(
                isPremium = isPremium,
                profile = profile,
                customerInfo = customerInfo,
                offerings = offerings,
                isLoading = isBillingLoading,
                onSignInClick = {
                    viewModel.setVerifyingAuth(true)
                    googleSignInAction.startFlow()
                },
                onPurchaseClick = { rcPackage ->
                    viewModel.purchasePackage(rcPackage)
                },
                onRestoreClick = {
                    viewModel.restorePurchases()
                },
                onDismiss = { showPaywall = false }
            )
        }

        if (showFilterSheet) {
            FilterBottomSheet(
                state = filterState,
                isLoadingMore = (uiState as? HomeState.Success)?.isLoadingMore == true,
                showLoadMore = showExternalHighlights,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onCountryToggle = viewModel::toggleCountry,
                onLeagueToggle = viewModel::toggleLeague,
                onReset = viewModel::resetFilters,
                onLoadMore = viewModel::loadMore,
                onApply = {
                    viewModel.applyFilters()
                    showFilterSheet = false
                },
                onDismiss = { showFilterSheet = false }
            )
        }

        if (showDevScreen) {
            DeveloperOptionsBottomSheet(
                isDebugPremium = isDebugPremium,
                onToggleDebugPremium = { enabled ->
                    viewModel.setDebugPremium(enabled)
                },
                isForceNonPremium = isForceNonPremium,
                onToggleForceNonPremium = { enabled ->
                    viewModel.setForceNonPremium(enabled)
                },
                onDismiss = { showDevScreen = false }
            )
        }
    }
}
}
