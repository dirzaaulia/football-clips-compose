package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.domain.model.Match
import com.dirzaaulia.footballclips.ui.adaptive.LocalAnimatedVisibilityScope
import com.dirzaaulia.footballclips.ui.adaptive.LocalSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ModernHighlightCard(
    match: Match,
    onWatchHighlightClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSpoilerFree: Boolean = false,
    isRevealed: Boolean = false,
    isFavorite: Boolean = false,
    showTeamName: Boolean = true,
    onRevealClick: (() -> Unit)? = null,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    Surface(
        onClick = {
            if (!match.highlightVideoId.isNullOrEmpty()) {
                onWatchHighlightClick(match.highlightVideoId)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (sharedTransitionScope != null && animatedVisibilityScope != null && match.highlightVideoId != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            rememberSharedContentState(key = "player-${HighlightUiItem.OracleMatch(match).uniqueId}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isFavorite) 1.5.dp else 1.dp,
            color = if (isFavorite) Color(0xFFFFB300).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        ),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HighlightThumbnailBox(
                match = match,
                isSpoilerFree = isSpoilerFree,
                isRevealed = isRevealed,
                isFavorite = isFavorite,
                onRevealClick = onRevealClick,
            )
            HighlightTeamsSection(
                match = match,
                isSpoilerFree = isSpoilerFree,
                isRevealed = isRevealed,
                onRevealClick = onRevealClick,
            )
        }
    }
}
