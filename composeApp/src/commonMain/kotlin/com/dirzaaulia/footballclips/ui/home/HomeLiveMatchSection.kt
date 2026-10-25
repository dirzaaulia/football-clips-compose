package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.remote.SupportedClub
import com.dirzaaulia.footballclips.data.model.remote.isMatchForFavoriteClubs
import com.dirzaaulia.footballclips.domain.model.Match

@Composable
fun HomeLiveMatchSection(
    liveMatches: List<Match>,
    favoriteClubs: Set<String>,
    supportedClubs: List<SupportedClub>,
    onMatchClick: (HighlightUiItem) -> Unit,
    modifier: Modifier = Modifier,
    onSeeAllClick: () -> Unit = {},
) {
    if (liveMatches.isEmpty()) return

    val sortedMatches = remember(liveMatches, favoriteClubs) {
        liveMatches.sortedWith(
            compareByDescending<Match> { it.isMatchForFavoriteClubs(favoriteClubs, supportedClubs) }
                .thenBy { it.utcDate },
        )
    }
    val displayedMatches = remember(sortedMatches) { sortedMatches.take(5) }

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LiveMatchSectionHeader(
            totalInPlay = liveMatches.size,
            onSeeAllClick = onSeeAllClick,
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(displayedMatches, key = { it.id }) { match ->
                val isFav = match.isMatchForFavoriteClubs(favoriteClubs, supportedClubs)
                CompactLiveMatchCard(
                    match = match,
                    isFavorite = isFav,
                    onClick = { onMatchClick(HighlightUiItem.OracleMatch(match)) },
                )
            }

            if (sortedMatches.size > 5) {
                item(key = "see_all_live") {
                    SeeAllLiveMatchesCard(
                        remaining = sortedMatches.size - 5,
                        onClick = onSeeAllClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveMatchSectionHeader(
    totalInPlay: Int,
    onSeeAllClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFEF4444)))
            Text(
                text = "LIVE MATCHES",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = Color(0xFFEF4444),
            )
            Surface(color = Color(0xFFEF4444).copy(alpha = 0.15f), shape = CircleShape) {
                Text(
                    text = "$totalInPlay In-Play",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }

        Text(
            text = "See all",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { onSeeAllClick() }
                .padding(4.dp),
        )
    }
}
