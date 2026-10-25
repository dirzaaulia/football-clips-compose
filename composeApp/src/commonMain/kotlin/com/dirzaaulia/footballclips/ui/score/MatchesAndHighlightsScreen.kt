package com.dirzaaulia.footballclips.ui.score

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.notification.rememberNotificationPermissionState
import com.dirzaaulia.footballclips.ui.components.common.EmptyState
import com.dirzaaulia.footballclips.ui.components.common.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.components.common.MyClubsBottomSheet
import com.dirzaaulia.footballclips.ui.components.notification.NotificationPermissionBottomSheet
import com.dirzaaulia.footballclips.ui.score.components.FixturesContentList
import com.dirzaaulia.footballclips.ui.score.components.FixturesLandscapeContent
import com.dirzaaulia.footballclips.ui.score.components.MatchesTopBar
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MatchesAndHighlightsScreen(
    onVideoClick: (HighlightUiItem, onDismiss: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScoreViewModel = koinViewModel(),
    listState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val dynamicCompetitions by viewModel.competitions.collectAsState()
    val selectedCompetitionId by viewModel.selectedCompetitionId.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isSpoilerFreeMode by viewModel.isSpoilerFreeMode.collectAsState()
    var revealedMatchIds by remember { mutableStateOf(emptySet<Long>()) }

    val favoriteClubs by viewModel.favoriteClubs.collectAsState()
    val supportedClubs by viewModel.supportedClubs.collectAsState()
    val isMyClubsActive by viewModel.isMyClubsFilterActive.collectAsState()
    val isLiveOnlyActive by viewModel.isLiveOnlyFilterActive.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()
    val liveMatchCount by viewModel.liveMatchCount.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val isFiltersExpanded by viewModel.isFiltersExpanded.collectAsState()
    val isMyClubsSheetVisible by viewModel.showMyClubsSheet.collectAsState()
    var showMyClubsSheet by remember { mutableStateOf(false) }
    val permissionState = rememberNotificationPermissionState()
    var showNotificationRationale by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxHeight < 500.dp
        val isGrid = maxWidth >= 600.dp && !isLandscape

        val isScrolled by remember {
            derivedStateOf {
                val hasMatches = (uiState as? ScoreState.Success)?.matches?.isNotEmpty() == true
                val index = if (isGrid) gridState.firstVisibleItemIndex else listState.firstVisibleItemIndex
                val offset = if (isGrid) gridState.firstVisibleItemScrollOffset else listState.firstVisibleItemScrollOffset
                hasMatches && (index > 0 || offset > 30)
            }
        }

        LaunchedEffect(isScrolled) { viewModel.setScrollToTopVisible(isScrolled) }
        LaunchedEffect(Unit) { viewModel.scrollToTopEvent.collect { if (isGrid) gridState.animateScrollToItem(0) else listState.animateScrollToItem(0) } }

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
                viewModel = viewModel,
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    MatchesTopBar(
                        isWeb = isGrid,
                        isSpoilerFreeMode = isSpoilerFreeMode,
                        onToggleSpoilerFree = { viewModel.toggleSpoilerFreeMode() },
                        favoriteClubsCount = favoriteClubs.size,
                        isMyClubsActive = isMyClubsActive,
                        onToggleMyClubs = { viewModel.toggleMyClubsFilter() },
                        onOpenMyClubsSheet = { showMyClubsSheet = true },
                        isLiveOnlyActive = isLiveOnlyActive,
                        onToggleLiveOnly = { viewModel.toggleLiveOnlyFilter() },
                        liveMatchCount = liveMatchCount,
                        dates = viewModel.availableDates,
                        selectedDate = selectedDate,
                        onDateSelected = viewModel::selectDate,
                        selectedCompetitionId = selectedCompetitionId,
                        onLeagueSelected = { _, competitionId -> viewModel.selectCompetition(competitionId) },
                        dynamicCompetitions = dynamicCompetitions,
                        selectedSeason = selectedSeason,
                        onSeasonSelected = viewModel::setSeason,
                        selectedStatusFilter = selectedStatusFilter,
                        onStatusFilterSelected = viewModel::selectStatusFilter,
                        isFiltersExpanded = isFiltersExpanded,
                        onToggleFilterExpanded = { viewModel.toggleFiltersExpanded() },
                    )
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    when (val state = uiState) {
                        is ScoreState.Loading -> ExpressiveLoadingIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 5.dp,
                        )

                        is ScoreState.Error -> {
                            EmptyState(
                                title = "Failed to Load Matches",
                                description = state.message,
                                onActionClick = { viewModel.getMatches() },
                                actionText = "Try Again",
                            )
                        }

                        is ScoreState.Success -> {
                            if (state.matches.isEmpty()) {
                                EmptyState(
                                    title = "No Matches Scheduled",
                                    description = "Try selecting a different date or competition.",
                                )
                            } else {
                                FixturesContentList(
                                    isGrid = isGrid,
                                    matches = state.matches,
                                    gridState = gridState,
                                    listState = listState,
                                    favoriteClubs = favoriteClubs,
                                    supportedClubs = supportedClubs,
                                    isSpoilerFreeMode = isSpoilerFreeMode,
                                    revealedMatchIds = revealedMatchIds,
                                    onRevealMatch = { id -> revealedMatchIds = revealedMatchIds + id },
                                    onVideoClick = onVideoClick,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showMyClubsSheet || isMyClubsSheetVisible) {
            MyClubsBottomSheet(
                selectedClubIds = favoriteClubs,
                onToggleClub = { clubId ->
                    val isAdding = !favoriteClubs.contains(clubId)
                    viewModel.toggleFavoriteClub(clubId)
                    if (isAdding && !permissionState.hasPermission) {
                        showNotificationRationale = true
                    }
                },
                onDismissRequest = {
                    showMyClubsSheet = false
                    viewModel.setMyClubsSheetVisible(false)
                },
                clubs = supportedClubs,
            )
        }

        NotificationPermissionBottomSheet(
            isVisible = showNotificationRationale,
            isPermanentlyDenied = permissionState.isPermanentlyDenied,
            onConfirm = {
                showNotificationRationale = false
                permissionState.launchPermissionRequest()
            },
            onDismiss = {
                showNotificationRationale = false
            },
            onOpenSettings = {
                showNotificationRationale = false
                permissionState.openSettings()
            },
        )
    }
}
