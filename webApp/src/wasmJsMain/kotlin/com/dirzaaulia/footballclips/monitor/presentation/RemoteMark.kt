package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.SubcomposeAsyncImage

@Composable
internal fun RemoteMark(url: String, description: String, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = description,
        modifier = modifier,
        loading = { Icon(Icons.Rounded.Image, contentDescription = description) },
        error = { Icon(Icons.Rounded.BrokenImage, contentDescription = description) },
    )
}
