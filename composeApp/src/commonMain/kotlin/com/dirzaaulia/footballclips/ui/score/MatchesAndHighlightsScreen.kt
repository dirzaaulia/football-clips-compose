package com.dirzaaulia.footballclips.ui.score

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import com.dirzaaulia.footballclips.data.constants.SupportedClubsConstants
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.EmptyState
import com.dirzaaulia.footballclips.ui.components.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.MyClubsBottomSheet
import com.dirzaaulia.footballclips.ui.score.components.FixturesLandscapeContent
import com.dirzaaulia.footballclips.ui.score.components.MatchCard
import com.dirzaaulia.footballclips.ui.score.components.MatchesTopBar
import com.dirzaaulia.footballclips.ui.score.components.WebFixtureCard
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MatchesAndHighlightsScreen(
    onVideoClick: (HighlightUiItem, onDismiss: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScoreViewModel = koinViewModel(),
    listState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCompetitionId by viewModel.selectedCompetitionId.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isSpoilerFreeMode by viewModel.isSpoilerFreeMode.collectAsState()
    var revealedMatchIds by remember { mutableStateOf(emptySet<Long>()) }

    val favoriteClubs by viewModel.favoriteClubs.collectAsState()
    val supportedClubs by viewModel.supportedClubs.collectAsState()
    val isMyClubsActive by viewModel.isMyClubsFilterActive.collectAsState()
    val isFiltersExpanded by viewModel.isFiltersExpanded.collectAsState()
    val isMyClubsSheetVisible by viewModel.showMyClubsSheet.collectAsState()
    var showMyClubsSheet by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxHeight < 500.dp
        val isWeb = maxWidth > 840.dp && !isLandscape

        val isScrolled by remember {
            derivedStateOf {
                val hasMatches = (uiState as? ScoreState.Success)?.matches?.isNotEmpty() == true
                val index = if (isWeb) gridState.firstVisibleItemIndex else listState.firstVisibleItemIndex
                val offset = if (isWeb) gridState.firstVisibleItemScrollOffset else listState.firstVisibleItemScrollOffset
                hasMatches && (index > 0 || offset > 30)
            }
        }

        LaunchedEffect(isScrolled) { viewModel.setScrollToTopVisible(isScrolled) }
        LaunchedEffect(Unit) { viewModel.scrollToTopEvent.collect { if (isWeb) gridState.animateScrollToItem(0) else listState.animateScrollToItem(0) } }
        
        if (isLandscape) {
            FixturesLandscapeContent(
                uiState = uiState,
                dates = viewModel.availableDates,
                selectedDate = selectedDate,
                onDateSelected = viewModel::selectDate,
                selectedCompetitionId = selectedCompetitionId,
                onLeagueSelected = { _: Int?, competitionId: String? -> viewModel.selectCompetition(competitionId) },
                favoriteClubs = favoriteClubs,
                supportedClubs = supportedClubs,
                isMyClubsActive = isMyClubsActive,
                onToggleMyClubs = { viewModel.toggleMyClubsFilter() },
                onOpenMyClubsSheet = { showMyClubsSheet = true },
                isSpoilerFreeMode = isSpoilerFreeMode,
                revealedMatchIds = revealedMatchIds,
                onRevealMatch = { id: Long -> revealedMatchIds = revealedMatchIds + id },
                gridState = gridState,
                onVideoClick = onVideoClick,
                viewModel = viewModel
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    MatchesTopBar(
                        isWeb = isWeb,
                        isSpoilerFreeMode = isSpoilerFreeMode,
                        onToggleSpoilerFree = { viewModel.toggleSpoilerFreeMode() },
                        favoriteClubsCount = favoriteClubs.size,
                        isMyClubsActive = isMyClubsActive,
                        onToggleMyClubs = { viewModel.toggleMyClubsFilter() },
                        onOpenMyClubsSheet = { showMyClubsSheet = true },
                        dates = viewModel.availableDates,
                        selectedDate = selectedDate,
                        onDateSelected = viewModel::selectDate,
                        selectedCompetitionId = selectedCompetitionId,
                        onLeagueSelected = { _, competitionId -> viewModel.selectCompetition(competitionId) },
                        isFiltersExpanded = isFiltersExpanded,
                        onToggleFilterExpanded = { viewModel.toggleFiltersExpanded() }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = uiState) {
                        is ScoreState.Loading -> ExpressiveLoadingIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 5.dp
                        )
                        is ScoreState.Error -> {
                            EmptyState(
                                title = "Failed to Load Matches",
                                description = state.message,
                                onActionClick = { viewModel.getMatches() },
                                actionText = "Try Again"
                            )
                        }
                        is ScoreState.Success -> {
                            if (state.matches.isEmpty()) {
                                EmptyState(
                                    title = "No Matches Scheduled",
                                    description = "Try selecting a different date or competition."
                                )
                            } else {
                                if (isWeb) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Adaptive(minSize = 450.dp),
                                        state = gridState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(32.dp),
                                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                                        verticalArrangement = Arrangement.spacedBy(32.dp)
                                    ) {
                                        items(
                                            items = state.matches,
                                            key = { it.uniqueId },
                                            span = { item ->
                                                if (item is HighlightUiItem.BannerAd) GridItemSpan(maxLineSpan)
                                                else GridItemSpan(1)
                                            }
                                        ) { item ->
                                            when (item) {
                                                is HighlightUiItem.SupabaseMatch -> {
                                                    val isFav = SupportedClubsConstants.isMatchForFavoriteClubs(item.match, favoriteClubs, supportedClubs)
                                                    WebFixtureCard(
                                                        match = item.match,
                                                        isSpoilerFree = isSpoilerFreeMode,
                                                        isRevealed = revealedMatchIds.contains(item.match.id),
                                                        isFavorite = isFav,
                                                        onRevealClick = { revealedMatchIds = revealedMatchIds + item.match.id },
                                                        onClick = { if (item.match.highlightVideoId != null) onVideoClick(item) { } }
                                                    )
                                                }
                                                is HighlightUiItem.BannerAd -> BannerAdItem()
                                                else -> {}
                                            }
                                        }
                                        item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(100.dp)) }
                                    }
                                } else {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(
                                            items = state.matches,
                                            key = { it.uniqueId }
                                        ) { item ->
                                            when (item) {
                                                is HighlightUiItem.SupabaseMatch -> {
                                                    val isFav = SupportedClubsConstants.isMatchForFavoriteClubs(item.match, favoriteClubs, supportedClubs)
                                                    MatchCard(
                                                        match = item.match,
                                                        isSpoilerFree = isSpoilerFreeMode,
                                                        isRevealed = revealedMatchIds.contains(item.match.id),
                                                        isFavorite = isFav,
                                                        onRevealClick = { revealedMatchIds = revealedMatchIds + item.match.id },
                                                        onWatchHighlightClick = { onVideoClick(item) { } }
                                                    )
                                                }
                                                is HighlightUiItem.BannerAd -> BannerAdItem()
                                                else -> {}
                                            }
                                        }
                                        item { Spacer(modifier = Modifier.height(100.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showMyClubsSheet || isMyClubsSheetVisible) {
            MyClubsBottomSheet(
                selectedClubIds = favoriteClubs,
                onToggleClub = { viewModel.toggleFavoriteClub(it) },
                onDismissRequest = {
                    showMyClubsSheet = false
                    viewModel.setMyClubsSheetVisible(false)
                },
                clubs = supportedClubs
            )
        }
    }
}
