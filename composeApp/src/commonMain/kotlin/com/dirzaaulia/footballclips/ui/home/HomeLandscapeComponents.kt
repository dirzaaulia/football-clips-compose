package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.model.HighlightUiItem
import com.dirzaaulia.footballclips.ui.components.ad.BannerAdItem
import com.dirzaaulia.footballclips.ui.components.ad.NativeAdStyle

@Composable
internal fun LandscapeHighlightItem(
    item: HighlightUiItem,
    isSpoilerFreeMode: Boolean,
    revealedItemIds: Set<String>,
    showExternalHighlights: Boolean,
    onRevealItem: (String) -> Unit,
    onVideoClick: (HighlightUiItem, (Boolean) -> Unit) -> Unit,
    viewModel: HomeViewModel,
) {
    val handleVideoClick: () -> Unit = {
        onVideoClick(item) { isJumping ->
            viewModel.setPendingInterstitial(true)
            if (!isJumping) viewModel.consumePendingInterstitial()
        }
    }

    when (item) {
        is HighlightUiItem.BannerAd -> {
            BannerAdItem(style = if (showExternalHighlights) NativeAdStyle.HIGHLIGHT else NativeAdStyle.MATCH)
        }
        else -> {
            LandscapeHighlightRowCard(
                item = item,
                isSpoilerFreeMode = isSpoilerFreeMode,
                onClick = handleVideoClick,
            )
        }
    }
}

@Composable
internal fun LandscapeExploreMoreButton(
    onToggleExternalHighlights: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(onClick = onToggleExternalHighlights) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Explore More Highlights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
