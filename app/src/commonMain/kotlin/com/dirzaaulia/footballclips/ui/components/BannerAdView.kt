package com.dirzaaulia.footballclips.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun BannerAdView(onAdLoaded: () -> Unit, onAdFailed: (String) -> Unit, modifier: Modifier = Modifier)
