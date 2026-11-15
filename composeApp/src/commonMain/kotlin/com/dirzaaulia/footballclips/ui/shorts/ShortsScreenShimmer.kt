package com.dirzaaulia.footballclips.ui.shorts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.ui.components.common.shimmerEffect

@Composable
fun ShortsScreenShimmer(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Video Card Background with Shimmer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shimmerEffect(
                    baseColor = Color(0xFF0F172A),
                    shimmerColor = Color(0xFF1E293B),
                ),
        )

        // Action Rail Skeleton on the Right
        ShortsActionRailShimmer(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 80.dp),
        )

        // Bottom Info Skeleton
        ShortsMetadataShimmer(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 80.dp, bottom = 24.dp),
        )
    }
}

@Composable
private fun ShortsActionRailShimmer(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(4) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .shimmerEffect(
                            baseColor = Color(0xFF1E293B),
                            shimmerColor = Color(0xFF334155),
                        ),
                )
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect(
                            baseColor = Color(0xFF1E293B),
                            shimmerColor = Color(0xFF334155),
                        ),
                )
            }
        }
    }
}

@Composable
private fun ShortsMetadataShimmer(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .shimmerEffect(
                        baseColor = Color(0xFF1E293B),
                        shimmerColor = Color(0xFF334155),
                    ),
            )
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerEffect(
                        baseColor = Color(0xFF1E293B),
                        shimmerColor = Color(0xFF334155),
                    ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(16.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerEffect(
                    baseColor = Color(0xFF1E293B),
                    shimmerColor = Color(0xFF334155),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerEffect(
                    baseColor = Color(0xFF1E293B),
                    shimmerColor = Color(0xFF334155),
                ),
        )
    }
}
