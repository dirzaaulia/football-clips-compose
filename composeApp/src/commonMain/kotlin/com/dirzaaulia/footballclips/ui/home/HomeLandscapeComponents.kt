package com.dirzaaulia.footballclips.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.data.model.uniqueId
import com.dirzaaulia.footballclips.ui.components.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.NativeAdStyle
import com.dirzaaulia.footballclips.ui.components.VerticalHighlightCard
import com.dirzaaulia.footballclips.ui.score.components.MatchCard

@Composable
internal fun LandscapeActionPill(
    favoriteClubs: Set<String>,
    isScrolled: Boolean,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isScrolled,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200)) + scaleOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                IconButton(
                    onClick = { viewModel.setFilterSheetVisible(true) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.setMyClubsSheetVisible(true) },
                    modifier = Modifier.size(40.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (favoriteClubs.isNotEmpty()) {
                                Badge(containerColor = Color(0xFFFFB300), contentColor = Color.Black) {
                                    Text("${favoriteClubs.size}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (favoriteClubs.isNotEmpty()) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "My Clubs",
                            tint = if (favoriteClubs.isNotEmpty()) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.triggerScrollToTop() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun LandscapeHighlightItem(
    item: HighlightUiItem,
    isSpoilerFreeMode: Boolean,
    revealedItemIds: Set<String>,
    showExternalHighlights: Boolean,
    onRevealItem: (String) -> Unit,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    viewModel: HomeViewModel
) {
    val itemUniqueId = item.uniqueId
    val isRevealed = revealedItemIds.contains(itemUniqueId)

    when (item) {
        is HighlightUiItem.SupabaseMatch -> {
            MatchCard(
                match = item.match,
                onWatchHighlightClick = {
                    onVideoClick(item) { isJumping ->
                        viewModel.setPendingInterstitial(true)
                        if (!isJumping) viewModel.consumePendingInterstitial()
                    }
                },
                showStatus = false,
                isSpoilerFree = isSpoilerFreeMode,
                isRevealed = isRevealed,
                onRevealClick = { onRevealItem(itemUniqueId) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        is HighlightUiItem.Highlight -> {
            VerticalHighlightCard(
                highlight = item.highlight,
                isSpoilerFree = isSpoilerFreeMode,
                isRevealed = isRevealed,
                onRevealClick = { onRevealItem(itemUniqueId) },
                onClick = {
                    onVideoClick(item) { isJumping ->
                        viewModel.setPendingInterstitial(true)
                        if (!isJumping) viewModel.consumePendingInterstitial()
                    }
                }
            )
        }
        is HighlightUiItem.BannerAd -> {
            BannerAdItem(style = if (showExternalHighlights) NativeAdStyle.HIGHLIGHT else NativeAdStyle.MATCH)
        }
    }
}

@Composable
internal fun LandscapeExploreMoreButton(
    onToggleExternalHighlights: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onToggleExternalHighlights) {
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
