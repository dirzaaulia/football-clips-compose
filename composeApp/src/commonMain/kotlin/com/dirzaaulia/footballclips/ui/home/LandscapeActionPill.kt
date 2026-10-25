package com.dirzaaulia.footballclips.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun LandscapeActionPill(
    favoriteClubs: Set<String>,
    isScrolled: Boolean,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isScrolled,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200)) + scaleOut(animationSpec = tween(200)),
        modifier = modifier,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)),
        ) {
            LandscapeActionPillButtons(
                favoriteClubs = favoriteClubs,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun LandscapeActionPillButtons(
    favoriteClubs: Set<String>,
    viewModel: HomeViewModel,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        IconButton(
            onClick = { viewModel.setFilterSheetVisible(true) },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(
            onClick = { viewModel.setMyClubsSheetVisible(true) },
            modifier = Modifier.size(40.dp),
        ) {
            BadgedBox(
                badge = {
                    if (favoriteClubs.isNotEmpty()) {
                        Badge(containerColor = Color(0xFFFFB300), contentColor = Color.Black) {
                            Text("${favoriteClubs.size}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
            ) {
                Icon(
                    imageVector = if (favoriteClubs.isNotEmpty()) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "My Clubs",
                    tint = if (favoriteClubs.isNotEmpty()) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        IconButton(
            onClick = { viewModel.triggerScrollToTop() },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Scroll to top",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
