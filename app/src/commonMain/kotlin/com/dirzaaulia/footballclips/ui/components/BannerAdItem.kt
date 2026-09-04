package com.dirzaaulia.footballclips.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun BannerAdItem(
    modifier: Modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
) {
    var isLoading by remember { mutableStateOf(true) }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) CircularProgressIndicator(
                strokeCap = StrokeCap.Round,
                modifier = Modifier.padding(16.dp)
            )

            BannerAdView(
                onAdLoaded = { isLoading = false },
                onAdFailed = { isLoading = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isLoading) Modifier.height(0.dp) else Modifier)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }
}
