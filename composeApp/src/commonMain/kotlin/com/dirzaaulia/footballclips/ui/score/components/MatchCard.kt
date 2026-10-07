package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dirzaaulia.footballclips.domain.model.Match

@Composable
fun MatchCard(
    match: Match,
    onWatchHighlightClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showStatus: Boolean = true,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
    isFavorite: Boolean = false,
    showTeamName: Boolean = true,
    onRevealClick: (() -> Unit)? = null
) {
    val hasHighlight = !match.highlightVideoId.isNullOrEmpty()

    if (hasHighlight) {
        ModernHighlightCard(
            match = match,
            onWatchHighlightClick = onWatchHighlightClick,
            modifier = modifier,
            isSpoilerFree = isSpoilerFree,
            isRevealed = isRevealed,
            isFavorite = isFavorite,
            showTeamName = showTeamName,
            onRevealClick = onRevealClick
        )
    } else {
        StandardMatchCard(
            match = match,
            modifier = modifier,
            showStatus = showStatus,
            isSpoilerFree = isSpoilerFree,
            isRevealed = isRevealed,
            isFavorite = isFavorite,
            showTeamName = showTeamName,
            onRevealClick = onRevealClick
        )
    }
}
