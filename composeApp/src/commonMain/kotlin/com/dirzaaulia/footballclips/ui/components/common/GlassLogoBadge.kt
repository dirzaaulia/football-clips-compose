package com.dirzaaulia.footballclips.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
fun GlassLogoBadge(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    contentPadding: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val frostedGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.22f),
            Color.White.copy(alpha = 0.12f),
        ),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(frostedGradient)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.25f),
                ),
                shape = shape,
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun GlassLogoBadge(
    model: Any? = null,
    logoUrl: Any? = model,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    padding: Dp = 2.dp,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    val targetModel = logoUrl ?: model
    GlassLogoBadge(
        modifier = modifier.size(size),
        shape = shape,
        contentPadding = padding,
    ) {
        AsyncImage(
            model = (targetModel as? String)?.toProxyUrl() ?: targetModel,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
