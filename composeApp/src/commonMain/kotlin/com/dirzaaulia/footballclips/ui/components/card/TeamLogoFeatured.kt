package com.dirzaaulia.footballclips.ui.components.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.ui.components.common.GlassLogoBadge
import com.dirzaaulia.footballclips.util.toProxyUrl

@Composable
internal fun TeamLogoFeatured(logo: String, isCinematic: Boolean = false) {
    if (logo.isNotEmpty()) {
        GlassLogoBadge(
            model = logo.toProxyUrl(),
            size = if (isCinematic) 56.dp else 36.dp,
            padding = if (isCinematic) 8.dp else 5.dp,
            modifier = Modifier,
        )
    }
}
