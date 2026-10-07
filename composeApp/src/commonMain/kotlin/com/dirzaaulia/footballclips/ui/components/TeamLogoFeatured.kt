package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun TeamLogoFeatured(logo: String, isCinematic: Boolean = false) {
    if (logo.isNotEmpty()) {
        Surface(
            color = Color.White.copy(alpha = 0.9f),
            shape = CircleShape,
            modifier = Modifier.size(if (isCinematic) 56.dp else 36.dp),
            shadowElevation = 2.dp
        ) {
            SubcomposeAsyncImage(
                model = logo.toProxyUrl(),
                contentDescription = null,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpressiveLoadingIndicator(
                            modifier = Modifier.size(if (isCinematic) 24.dp else 16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                modifier = Modifier.padding(if (isCinematic) 10.dp else 6.dp)
            )
        }
    }
}
