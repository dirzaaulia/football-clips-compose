package com.dirzaaulia.footballclips.ui.adaptive

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.zIndex
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.home.HomeScreen
import com.dirzaaulia.footballclips.ui.info.InfoScreen
import com.dirzaaulia.footballclips.ui.components.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.FilterBottomSheet
import com.dirzaaulia.footballclips.ui.components.WebHighlightCard
import com.dirzaaulia.footballclips.ui.home.HomeState
import com.dirzaaulia.footballclips.ui.home.HomeViewModel
import com.dirzaaulia.footballclips.ui.navigation.NavDestination
import com.dirzaaulia.footballclips.ui.navigation.bottomNavItems
import com.dirzaaulia.footballclips.ui.score.MatchesAndHighlightsScreen
import com.dirzaaulia.footballclips.ui.player.YouTubePlayerView
import com.dirzaaulia.footballclips.ui.score.components.LeagueHeader
import com.dirzaaulia.footballclips.ui.score.components.WebFixtureCard
import com.dirzaaulia.footballclips.util.extractVideoId
import com.dirzaaulia.footballclips.util.toProxyUrl
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun WebMatchesScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val uiState by viewModel.uiState.collectAsState()
    val isAdsRemoved by viewModel.isAdsRemoved.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val showExternalHighlights by viewModel.showExternalHighlights.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    val onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit = { item, _ ->
        navController.navigate("player/${item.uniqueId}")
    }

    val homeGridState = rememberLazyGridState()
    val fixturesGridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val showScrollToTop by remember {
        derivedStateOf {
            when (currentDestination?.route) {
                NavDestination.Home.route -> homeGridState.firstVisibleItemIndex > 0
                NavDestination.Fixtures.route -> fixturesGridState.firstVisibleItemIndex > 0
                else -> false
            }
        }
    }

    val isPlayerScreen = currentDestination?.route?.startsWith("player") == true

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isPlayerScreen) Color.Black else MaterialTheme.colorScheme.background)
                .onPreviewKeyEvent { 
                    if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                        if (currentDestination?.route?.startsWith("player") == true) {
                            navController.popBackStack()
                            true
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                }
        ) {
            // 1. Sleek, Thin Navigation Rail
            if (!isPlayerScreen) {
                NavigationRail(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    header = {
                        Surface(
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            shape = CircleShape,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        ) {
                            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SportsSoccer,
                                    contentDescription = "FootballClips Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().route ?: screen.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { 
                                Icon(
                                    screen.icon, 
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(24.dp)
                                ) 
                            },
                            label = { 
                                Text(
                                    screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ) 
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    AnimatedVisibility(
                        visible = currentDestination?.route == NavDestination.Home.route,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        NavigationRailItem(
                            selected = false,
                            onClick = { showFilterSheet = true },
                            icon = { Icon(Icons.Default.FilterList, contentDescription = "Filter") },
                            label = { Text("Filter", style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationRailItemDefaults.colors(
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                // Main Content
                NavHost(
                    navController = navController,
                    startDestination = NavDestination.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(NavDestination.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            gridState = homeGridState,
                            onVideoClick = onVideoClick
                        )
                    }
                    composable(NavDestination.Fixtures.route) {
                        MatchesAndHighlightsScreen(
                            onVideoClick = onVideoClick,
                            gridState = fixturesGridState
                        )
                    }
                    composable(NavDestination.Info.route) {
                        InfoScreen()
                    }

                    composable(
                        route = NavDestination.Player.route,
                        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId")
                        LaunchedEffect(itemId) {
                            if (itemId != null) {
                                viewModel.selectVideoById(itemId)
                            }
                        }

                        val selectedItem by viewModel.selectedItem.collectAsState()

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(100f)
                                .background(Color.Black)
                        ) {
                            MatchTheater(
                                item = selectedItem,
                                onClose = {
                                    navController.navigate(NavDestination.Home.route) {
                                        popUpTo(navController.graph.findStartDestination().route ?: NavDestination.Home.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                onItemClick = { item ->
                                    navController.navigate("player/${item.uniqueId}") {
                                        launchSingleTop = true
                                    }
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                }

                // Scroll to Top FAB
                androidx.compose.animation.AnimatedVisibility(
                    visible = showScrollToTop,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(32.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                when (currentDestination?.route) {
                                    NavDestination.Home.route -> homeGridState.animateScrollToItem(0)
                                    NavDestination.Fixtures.route -> fixturesGridState.animateScrollToItem(0)
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to Top")
                    }
                }
            }
        }
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
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MatchTheater(
    item: HighlightUiItem?,
    onClose: () -> Unit,
    onItemClick: (HighlightUiItem) -> Unit,
    viewModel: HomeViewModel
) {
    if (item == null) return

    val uriHandler = LocalUriHandler.current
    val isAdsRemoved by viewModel.isAdsRemoved.collectAsState()

    var showIssueDialog by remember { mutableStateOf(false) }

    val relatedHighlights by viewModel.relatedHighlights.collectAsState()
    val relatedFixturesFinished by viewModel.relatedFixturesFinished.collectAsState()
    val relatedFixturesUpcoming by viewModel.relatedFixturesUpcoming.collectAsState()

    val videoId = when (item) {
        is HighlightUiItem.SupabaseMatch -> item.match.highlightVideoId
        is HighlightUiItem.Highlight -> extractVideoId(item.highlight.embedHtml)
        else -> null
    }

    val title: String
    val competition: String
    val competitionId: String?
    val date: String
    val thumbnail: String

    when (item) {
        is HighlightUiItem.SupabaseMatch -> {
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}"
            competition = item.match.competitionName
            competitionId = item.match.competitionId
            date = item.match.dateOnly
            thumbnail = "https://img.youtube.com/vi/${item.match.highlightVideoId}/maxresdefault.jpg"
        }
        is HighlightUiItem.Highlight -> {
            title = item.highlight.title
            competition = item.highlight.leagueName
            competitionId = null
            date = item.highlight.date
            thumbnail = item.highlight.thumbnail
        }
        else -> return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        // Overall Dark Canvas
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "MATCH THEATER",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF0F0F0F).copy(alpha = 0.95f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier.zIndex(10f)
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // LEFT COLUMN (70%) - YouTube Player & Metadata (No Outer Scroll, Large Tall Player!)
                Column(
                    modifier = Modifier
                        .weight(0.70f)
                        .fillMaxHeight()
                        .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    // 1. YouTube-Style Ambient Mode Player Area (Fills Available Space, Large & Tall)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient Glow Backdrop (Vibrant background light following video thumbnail)
                        SubcomposeAsyncImage(
                            model = thumbnail.toProxyUrl(),
                            contentDescription = null,
                            modifier = Modifier
                                .matchParentSize()
                                .scale(1.35f)
                                .blur(140.dp),
                            contentScale = ContentScale.Crop,
                            alpha = 0.65f
                        )

                        // Video Player Box (Rounded Clipping & Full Container Fill)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        ) {
                            if (videoId != null) {
                                YouTubePlayerView(
                                    videoId = videoId, 
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                                )
                            } else {
                                SubcomposeAsyncImage(
                                    model = thumbnail.toProxyUrl(),
                                    contentDescription = null,
                                    loading = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            ExpressiveLoadingIndicator(
                                                modifier = Modifier.size(48.dp),
                                                strokeWidth = 6.dp
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // 2. Ultra-Sleek Single-Row Metadata Bar directly under the 16:9 Player
                    Surface(
                        color = Color(0xFF181818),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Section: Match Scoreboard or Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                if (item is HighlightUiItem.SupabaseMatch) {
                                    // Home Team Crest & Name
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SubcomposeAsyncImage(
                                            model = item.match.homeTeamCrest?.toProxyUrl(),
                                            contentDescription = null,
                                            modifier = Modifier.size(26.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = item.match.homeTeamName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Digital Score / Kickoff Badge
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                                    ) {
                                        if (item.match.isLive || item.match.isFinished) {
                                            Text(
                                                text = "${item.match.homeScore ?: 0} : ${item.match.awayScore ?: 0}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Black,
                                                color = if (item.match.isLive) Color(0xFFFF6B6B) else Color.White,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                letterSpacing = 1.sp
                                            )
                                        } else {
                                            Text(
                                                text = item.match.formattedLocalKickoff,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White.copy(alpha = 0.9f),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // Away Team Crest & Name
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SubcomposeAsyncImage(
                                            model = item.match.awayTeamCrest?.toProxyUrl(),
                                            contentDescription = null,
                                            modifier = Modifier.size(26.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = item.match.awayTeamName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            // Right Section: League Badge, Date & Action Pill Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                competitionId?.let { id ->
                                    LeagueHeader(
                                        competitionName = competition,
                                        competitionId = id,
                                        isDark = true
                                    )
                                }

                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )

                                // YouTube Action Pill Button ("Playback Issue?")
                                Surface(
                                    onClick = { showIssueDialog = true },
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.HelpOutline,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.9f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Playback Issue?",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // RIGHT COLUMN (30%) - YouTube-Style Sidebar (Highlights & Fixtures)
                Surface(
                    modifier = Modifier
                        .weight(0.30f)
                        .fillMaxHeight(),
                    color = Color(0xFF141414),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        var mainSidebarTab by remember { mutableStateOf(0) } // 0: Highlights, 1: Fixtures
                        var fixtureTab by remember { mutableStateOf(0) } // 0: Finished, 1: Upcoming

                        val mainTabs = listOf("HIGHLIGHTS", "FIXTURES")

                        // Sticky Sidebar Header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A1A1A))
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = competition.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            TabRow(
                                selectedTabIndex = mainSidebarTab,
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary,
                                divider = {},
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[mainSidebarTab]),
                                        color = MaterialTheme.colorScheme.primary,
                                        height = 3.dp
                                    )
                                }
                            ) {
                                mainTabs.forEachIndexed { index, titleText ->
                                    Tab(
                                        selected = mainSidebarTab == index,
                                        onClick = { mainSidebarTab = index },
                                        text = {
                                            Text(
                                                titleText,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Black,
                                                color = if (mainSidebarTab == index) Color.White else Color.White.copy(alpha = 0.4f),
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    )
                                }
                            }

                            // Sub-tabs for Fixtures (Finished / Upcoming)
                            if (mainSidebarTab == 1) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = fixtureTab == 0,
                                        onClick = { fixtureTab = 0 },
                                        label = { Text("RESULTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color.White.copy(alpha = 0.05f),
                                            labelColor = Color.White.copy(alpha = 0.6f)
                                        )
                                    )

                                    FilterChip(
                                        selected = fixtureTab == 1,
                                        onClick = { fixtureTab = 1 },
                                        label = { Text("UPCOMING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color.White.copy(alpha = 0.05f),
                                            labelColor = Color.White.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        // Sidebar List
                        if (mainSidebarTab == 0) {
                            // Highlights List
                            val relatedWithAds = remember(relatedHighlights, isAdsRemoved) {
                                if (isAdsRemoved) return@remember relatedHighlights

                                val result = mutableListOf<HighlightUiItem>()
                                relatedHighlights.forEachIndexed { index, item ->
                                    result.add(item)
                                    if ((index + 1) % 4 == 0 && index != relatedHighlights.lastIndex) {
                                        result.add(HighlightUiItem.BannerAd("rel-ad-$index"))
                                    }
                                }
                                result
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)
                            ) {
                                items(
                                    items = relatedWithAds,
                                    key = { it.uniqueId }
                                ) { relatedItem ->
                                    if (relatedItem is HighlightUiItem.BannerAd) {
                                        TheaterAdCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    } else {
                                        SidebarHighlightCard(
                                            item = relatedItem,
                                            onClick = { onItemClick(relatedItem) }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Fixtures List (Results / Upcoming)
                            val fixtures = if (fixtureTab == 0) relatedFixturesFinished else relatedFixturesUpcoming
                            val fixturesWithAds = remember(fixtures, isAdsRemoved) {
                                if (isAdsRemoved) return@remember fixtures

                                val result = mutableListOf<HighlightUiItem>()
                                fixtures.forEachIndexed { index, item ->
                                    result.add(item)
                                    if ((index + 1) % 5 == 0 && index != fixtures.lastIndex) {
                                        result.add(HighlightUiItem.BannerAd("fix-ad-$index"))
                                    }
                                }
                                result
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(1.dp),
                                modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)
                            ) {
                                items(
                                    items = fixturesWithAds,
                                    key = { it.uniqueId }
                                ) { fixtureItem ->
                                    if (fixtureItem is HighlightUiItem.BannerAd) {
                                        TheaterAdCard(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    } else if (fixtureItem is HighlightUiItem.SupabaseMatch) {
                                        TheaterFixtureCard(
                                            match = fixtureItem.match,
                                            onClick = { /* Handle fixture details */ }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showIssueDialog) {
        AlertDialog(
            onDismissRequest = { showIssueDialog = false },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f),
            title = { Text("Playback Issue", fontWeight = FontWeight.Black) },
            text = { Text("This video might have embedding restrictions. Would you like to watch it directly on YouTube?") },
            confirmButton = {
                Button(
                    onClick = {
                        showIssueDialog = false
                        videoId?.let { id ->
                            uriHandler.openUri("https://www.youtube.com/watch?v=$id")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Yes, Open YouTube", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showIssueDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
private fun SidebarHighlightCard(
    item: HighlightUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title: String
    val thumbnail: String
    val league: String

    when (item) {
        is HighlightUiItem.SupabaseMatch -> {
            title = "${item.match.homeTeamName} vs ${item.match.awayTeamName}"
            thumbnail = "https://img.youtube.com/vi/${item.match.highlightVideoId}/maxresdefault.jpg"
            league = item.match.competitionName
        }
        is HighlightUiItem.Highlight -> {
            title = item.highlight.title
            thumbnail = item.highlight.thumbnail
            league = item.highlight.leagueName
        }
        else -> return
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 16:9 Thumbnail
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
            ) {
                SubcomposeAsyncImage(
                    model = thumbnail.toProxyUrl(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(12.dp))

            // Title & Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = league,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TheaterFixtureCard(
    match: com.dirzaaulia.footballclips.domain.model.Match,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubcomposeAsyncImage(
                        model = match.homeTeamCrest?.toProxyUrl(),
                        contentDescription = null,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                ExpressiveLoadingIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                            }
                        },
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = match.homeTeamName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubcomposeAsyncImage(
                        model = match.awayTeamCrest?.toProxyUrl(),
                        contentDescription = null,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                ExpressiveLoadingIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp
                                )
                            }
                        },
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = match.awayTeamName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (match.isFinished || match.isLive) {
                    Text(
                        text = "${match.homeScore ?: 0}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (match.isLive) Color(0xFFFF6B6B) else Color.White
                    )
                    Text(
                        text = "${match.awayScore ?: 0}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (match.isLive) Color(0xFFFF6B6B) else Color.White
                    )
                } else {
                    Text(
                        text = match.formattedLocalKickoff,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TheaterAdCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        BannerAdItem(modifier = Modifier.padding(16.dp))
    }
}

