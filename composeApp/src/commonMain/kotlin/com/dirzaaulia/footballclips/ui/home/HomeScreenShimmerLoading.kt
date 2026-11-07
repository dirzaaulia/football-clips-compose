package com.dirzaaulia.footballclips.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.ui.components.common.shimmerEffect

@Composable
internal fun HomeScreenShimmerLoading(
    isGrid: Boolean,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isLandscape) {
        LandscapeShimmerLayout(modifier = modifier)
    } else if (isGrid) {
        GridShimmerLayout(modifier = modifier)
    } else {
        PortraitShimmerLayout(modifier = modifier)
    }
}

@Composable
private fun PortraitShimmerLayout(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCarouselShimmer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        items(3) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CompetitionHeaderShimmer(
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false,
                ) {
                    items(3) {
                        HighlightCardShimmer(
                            modifier = Modifier.width(280.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridShimmerLayout(
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 240.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false,
    ) {
        items(8) {
            HighlightCardShimmer(
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LandscapeShimmerLayout(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect(),
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 200.dp),
            modifier = Modifier.weight(1f).fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false,
        ) {
            items(4) {
                HighlightCardShimmer(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun HeroCarouselShimmer(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(16.dp))
            .shimmerEffect(),
    )
}

@Composable
private fun CompetitionHeaderShimmer(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .shimmerEffect(),
        )
        Box(
            modifier = Modifier
                .size(width = 130.dp, height = 18.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect(),
        )
    }
}

@Composable
private fun HighlightCardShimmer(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clip(RoundedCornerShape(12.dp))
                .shimmerEffect(),
        )
        ShimmerTeamRow(width = 110.dp)
        ShimmerTeamRow(width = 90.dp)
        Box(
            modifier = Modifier
                .size(width = 70.dp, height = 10.dp)
                .clip(RoundedCornerShape(3.dp))
                .shimmerEffect(),
        )
    }
}

@Composable
private fun ShimmerTeamRow(
    width: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .shimmerEffect(),
        )
        Box(
            modifier = Modifier
                .size(width = width, height = 14.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect(),
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 14.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect(),
        )
    }
}
