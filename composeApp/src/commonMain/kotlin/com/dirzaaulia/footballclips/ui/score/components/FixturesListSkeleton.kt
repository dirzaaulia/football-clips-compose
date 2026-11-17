package com.dirzaaulia.footballclips.ui.score.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Exact-match Shimmer Wireframe Skeleton for Matches & Fixtures List.
 */
@Composable
fun FixturesListSkeleton(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    val transition = rememberInfiniteTransition(label = "FixturesSkeleton")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "FixturesShimmerTranslate",
    )

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor,
        ),
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim),
    )

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(count = 5) {
            FixtureCardSkeletonItem(brush = shimmerBrush)
        }
    }
}

@Composable
private fun FixtureCardSkeletonItem(
    brush: Brush,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top Row: Home Crest & Name | Time Pill | Away Name & Crest
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Home Team
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(brush),
                )
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.7f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush),
                )
            }

            // Center Stadium Pill
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush),
            )

            // Away Team
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.7f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush),
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(brush),
                )
            }
        }

        // Bottom Row: Date & Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(brush),
            )
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(brush),
            )
        }
    }
}
