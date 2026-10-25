package com.dirzaaulia.footballclips.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.dirzaaulia.footballclips.data.model.remote.StandingDto
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.home.HomeLiveMatchSection
import com.dirzaaulia.footballclips.ui.info.InfoScreen
import com.dirzaaulia.footballclips.ui.score.StandingsScreen
import com.dirzaaulia.footballclips.ui.score.StandingsState
import com.dirzaaulia.footballclips.ui.score.components.ModernHighlightCard
import com.dirzaaulia.footballclips.ui.score.components.StandardMatchCard
import com.dirzaaulia.footballclips.ui.theme.FootballClipsTheme

val previewMockMatches = listOf(
    Match(
        id = 101L,
        competitionId = "PL",
        competitionName = "Premier League",
        utcDate = "2026-10-22T19:00:00Z",
        status = "FINISHED",
        matchday = 8,
        homeTeamName = "Arsenal FC",
        homeTeamCrest = "https://a.espncdn.com/i/teamlogos/soccer/500-dark/359.png",
        awayTeamName = "Chelsea FC",
        awayTeamCrest = "https://a.espncdn.com/i/teamlogos/soccer/500-dark/363.png",
        homeScore = 3,
        awayScore = 1,
        highlightVideoId = "dQw4w9WgXcQ",
    ),
    Match(
        id = 102L,
        competitionId = "IDN",
        competitionName = "Liga 1 Indonesia",
        utcDate = "2026-10-22T12:00:00Z",
        status = "FINISHED",
        matchday = 6,
        homeTeamName = "Madura United FC",
        homeTeamCrest = "https://a.espncdn.com/i/teamlogos/soccer/500-dark/18327.png",
        awayTeamName = "PERSIB Bandung",
        awayTeamCrest = "https://a.espncdn.com/i/teamlogos/soccer/500-dark/18320.png",
        homeScore = 1,
        awayScore = 2,
        highlightVideoId = "g9VdxhMHgs4",
    ),
    Match(
        id = 103L,
        competitionId = "CL",
        competitionName = "Champions League",
        utcDate = "2026-10-22T20:00:00Z",
        status = "IN_PLAY",
        matchday = 3,
        homeTeamName = "Real Madrid",
        homeTeamCrest = "https://a.espncdn.com/i/teamlogos/soccer/500-dark/86.png",
        awayTeamName = "FC Bayern München",
        awayTeamCrest = "https://a.espncdn.com/i/teamlogos/soccer/500-dark/132.png",
        homeScore = 2,
        awayScore = 2,
        highlightVideoId = "dQw4w9WgXcQ",
    ),
)

val previewMockStandings = listOf(
    StandingDto(position = 1, teamId = 359, teamName = "Arsenal FC", teamCrest = "https://a.espncdn.com/i/teamlogos/soccer/500-dark/359.png", playedGames = 8, won = 6, draw = 2, lost = 0, points = 20, goalsFor = 18, goalsAgainst = 5, goalDifference = 13, competitionId = "PL"),
    StandingDto(position = 2, teamId = 382, teamName = "Manchester City", teamCrest = "https://a.espncdn.com/i/teamlogos/soccer/500-dark/382.png", playedGames = 8, won = 6, draw = 1, lost = 1, points = 19, goalsFor = 19, goalsAgainst = 7, goalDifference = 12, competitionId = "PL"),
    StandingDto(position = 3, teamId = 364, teamName = "Liverpool FC", teamCrest = "https://a.espncdn.com/i/teamlogos/soccer/500-dark/364.png", playedGames = 8, won = 5, draw = 2, lost = 1, points = 17, goalsFor = 15, goalsAgainst = 8, goalDifference = 7, competitionId = "PL"),
)

@OptIn(ExperimentalCoilApi::class)
@Composable
fun MatrixScreenContainer(
    title: String,
    deviceLabel: String,
    content: @Composable () -> Unit,
) {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(color = 0xFF334155.toInt(), width = 100, height = 100)
    }

    CompositionLocalProvider(
        LocalAsyncImagePreviewHandler provides previewHandler,
    ) {
        FootballClipsTheme(darkTheme = true) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(6.dp),
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = deviceLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightsTabPreviewContent(deviceLabel: String) {
    val liveMatches = previewMockMatches.filter { it.status == "IN_PLAY" }
    MatrixScreenContainer(title = "Highlights", deviceLabel = deviceLabel) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (liveMatches.isNotEmpty()) {
                HomeLiveMatchSection(
                    liveMatches = liveMatches,
                    favoriteClubs = emptySet(),
                    supportedClubs = emptyList(),
                    onMatchClick = {},
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(previewMockMatches) { match ->
                    ModernHighlightCard(
                        match = match,
                        onWatchHighlightClick = {},
                        isSpoilerFree = false,
                        isRevealed = true,
                        isFavorite = true,
                    )
                }
            }
        }
    }
}

@Composable
fun FixturesTabPreviewContent(deviceLabel: String) {
    MatrixScreenContainer(title = "Fixtures", deviceLabel = deviceLabel) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(previewMockMatches) { match ->
                StandardMatchCard(
                    match = match,
                    isSpoilerFree = false,
                    isRevealed = true,
                    isFavorite = true,
                )
            }
        }
    }
}

@Composable
fun StandingsTabPreviewContent(deviceLabel: String) {
    MatrixScreenContainer(title = "Standings", deviceLabel = deviceLabel) {
        StandingsScreen(
            state = StandingsState.Success(
                standings = previewMockStandings,
                knockoutMatches = previewMockMatches,
            ),
            isPremium = true,
            selectedCompetitionId = "PL",
            selectedSeason = 2026,
            onRetry = {},
        )
    }
}

@Composable
fun InformationTabPreviewContent(deviceLabel: String) {
    MatrixScreenContainer(title = "Information", deviceLabel = deviceLabel) {
        InfoScreen()
    }
}
