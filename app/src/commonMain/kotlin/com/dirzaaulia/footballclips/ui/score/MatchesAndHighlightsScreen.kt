package com.dirzaaulia.footballclips.ui.score

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.domain.model.Match
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.BigLeaguesQuickFilterBar
import com.dirzaaulia.footballclips.ui.components.EmptyState
import com.dirzaaulia.footballclips.ui.components.ExpressiveLoadingIndicator
import com.dirzaaulia.footballclips.ui.score.components.MatchCard
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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWeb = maxWidth > 840.dp
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 2.dp
                ) {
                    Column {
                        if (!isWeb) {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Fixtures",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        val horizontalPadding = if (isWeb) 32.dp else 16.dp

                        DateSelectorBar(
                            dates = viewModel.availableDates,
                            selectedDate = selectedDate,
                            onDateSelected = viewModel::selectDate,
                            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 4.dp)
                        )

                        BigLeaguesQuickFilterBar(
                            selectedCompetitionId = selectedCompetitionId,
                            onLeagueSelected = { _, competitionId -> 
                                viewModel.selectCompetition(competitionId)
                            },
                            contentPadding = PaddingValues(horizontal = horizontalPadding)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
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
                    is ScoreState.Loading -> ExpressiveLoadingIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 5.dp
                    )
                    is ScoreState.Error -> Text(state.message, modifier = Modifier.padding(16.dp))
                    is ScoreState.Success -> {
                        if (state.matches.isEmpty()) {
                            EmptyState(
                                title = "No Matches Scheduled",
                                description = "Try selecting a different date or competition.",
                                onActionClick = { 
                                    viewModel.selectCompetition(null)
                                }
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
                                        span = { item ->
                                            if (item is HighlightUiItem.BannerAd) GridItemSpan(maxLineSpan)
                                            else GridItemSpan(1)
                                        }
                                    ) { item ->
                                        when (item) {
                                            is HighlightUiItem.SupabaseMatch -> {
                                                WebFixtureCard(
                                                    match = item.match,
                                                    onClick = {
                                                        if (item.match.highlightVideoId != null) {
                                                            onVideoClick(item) { }
                                                        }
                                                    }
                                                )
                                            }
                                            is HighlightUiItem.BannerAd -> {
                                                BannerAdItem()
                                            }
                                            else -> {}
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
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(state.matches) { item ->
                                        when (item) {
                                            is HighlightUiItem.SupabaseMatch -> {
                                                MatchCard(
                                                    match = item.match,
                                                    onWatchHighlightClick = {
                                                        onVideoClick(item) { }
                                                    }
                                                )
                                            }
                                            is HighlightUiItem.BannerAd -> {
                                                BannerAdItem()
                                            }
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
}

@Composable
fun DateSelectorBar(
    dates: List<DateOption>,
    selectedDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(dates) { option ->
            DateItem(
                option = option,
                isSelected = option.date == selectedDate,
                onClick = { onDateSelected(option.date) }
            )
        }
    }
}

@Composable
fun DateItem(
    option: DateOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    val dayColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val numberColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val monthColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .width(72.dp)
            .height(64.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = option.displayDay.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = dayColor,
                fontSize = 10.sp,
                letterSpacing = 0.3.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = option.displayDate.split(" ").first(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = numberColor,
                fontSize = 16.sp,
                lineHeight = 18.sp
            )
            Text(
                text = option.displayDate.split(" ").last(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = monthColor,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
