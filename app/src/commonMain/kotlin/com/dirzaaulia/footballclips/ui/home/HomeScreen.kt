package com.dirzaaulia.footballclips.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.util.isDebugBuild
import kotlinx.datetime.Clock
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import org.koin.compose.koinInject
import com.dirzaaulia.footballclips.ui.components.*
import com.dirzaaulia.footballclips.ui.score.components.MatchCard
import kotlinx.coroutines.launch
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
                            text = if (isWeb) "FootballClips" else "Highlights",
                            style = if (isWeb) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = if (isWeb) 2.sp else 0.sp,
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
                            IconButton(
                                onClick = { viewModel.setDarkMode(!isDarkMode) },
                                modifier = Modifier.padding(end = 4.dp)
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
                    var expanded by remember { mutableStateOf(false) }
                    
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 100.dp)
                    ) {
                        // Menu Items
                        AnimatedVisibility(
                            visible = expanded,
                            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Scroll to Top Item
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { 
                                        expanded = false
                                        coroutineScope.launch { listState.animateScrollToItem(0) }
                                    }
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        shadowElevation = 2.dp
                                    ) {
                                        Text(
                                            "Scroll to Top",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    SmallFloatingActionButton(
                                        onClick = {
                                            expanded = false
                                            coroutineScope.launch { listState.animateScrollToItem(0) }
                                        },
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        shape = CircleShape
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                                    }
                                }

                                // Filter Item
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { 
                                        expanded = false
                                        showFilterSheet = true 
                                    }
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        shadowElevation = 2.dp
                                    ) {
                                        Text(
                                            "Filter Results",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    SmallFloatingActionButton(
                                        onClick = {
                                            expanded = false
                                            showFilterSheet = true
                                        },
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        shape = CircleShape
                                    ) {
                                        Icon(Icons.Default.FilterList, contentDescription = null)
                                    }
                                }
                            }
                        }

                        // Main FAB
                        FloatingActionButton(
                            onClick = { expanded = !expanded },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            val rotation by animateFloatAsState(if (expanded) 90f else 0f)
                            val icon = if (expanded) Icons.Default.Close else Icons.Default.Menu
                            Icon(
                                imageVector = icon,
                                contentDescription = "Menu",
                                modifier = Modifier.rotate(rotation)
                            )
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
                when (val state = uiState) {
                    is HomeState.Loading -> {
                        ExpressiveLoadingIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 5.dp
                        )
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
                                    items(gridItems) { item ->
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
                                    items(gridItems) { item ->
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
                        Text(text = state.message)
                    }
                }
            }

        val composeAuth = koinInject<ComposeAuth>()
        val googleSignInAction = composeAuth.rememberSignInWithGoogle(
            fallback = {
                viewModel.signIn()
            }
        )

        if (showPaywall) {
            PaywallBottomSheet(
                isPremium = isPremium,
                profile = profile,
                customerInfo = customerInfo,
                offerings = offerings,
                onSignInClick = {
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
                onDismiss = { showDevScreen = false }
            )
        }
    }
}
}
