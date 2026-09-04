package com.dirzaaulia.footballclips.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.dirzaaulia.footballclips.data.constants.AdConfiguration
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

@JsFun("() => { try { (window.adsbygoogle = window.adsbygoogle || []).push({}); } catch(e) { console.error(e); } }")
external fun pushAdSense()

@Composable
actual fun BannerAdView(onAdLoaded: () -> Unit, onAdFailed: (String) -> Unit, modifier: Modifier) {
    val hostname = window.location.hostname
    val isProduction = hostname in AdConfiguration.ALLOWED_PRODUCTION_HOSTS
    
    if (!isProduction) {
        // DEBUG/LOCAL: Show Placeholder
        LaunchedEffect(Unit) { onAdLoaded() }
        AdPlaceholder(
            modifier = modifier,
            title = "WASM DEBUG AD",
            message = "AdSense active on ${AdConfiguration.ALLOWED_PRODUCTION_HOSTS.joinToString(", ")}"
        )
    } else {
        // PRODUCTION: Inject Real AdSense
        AdSenseContainer(onAdLoaded, onAdFailed, modifier)
    }
}

@Composable
private fun AdSenseContainer(onAdLoaded: () -> Unit, onAdFailed: (String) -> Unit, modifier: Modifier) {
    val density = LocalDensity.current
    var adContainer: HTMLElement? by remember { mutableStateOf(null) }
    val adId = remember { "adsense-${(0..1000000).random()}" }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val size = coordinates.size
                
                adContainer?.let { container ->
                    container.style.left = "${position.x / density.density}px"
                    container.style.top = "${position.y / density.density}px"
                    container.style.width = "${size.width / density.density}px"
                    container.style.height = "${size.height / density.density}px"
                    container.style.display = "block"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        DisposableEffect(adId) {
            val container = document.createElement("div") as HTMLElement
            container.id = adId
            container.style.position = "absolute"
            container.style.zIndex = "1000"
            container.style.display = "none"
            
            val ins = document.createElement("ins") as HTMLElement
            ins.className = "adsbygoogle"
            ins.style.display = "block"
            ins.style.width = "100%"
            ins.style.height = "100%"
            ins.setAttribute("data-ad-client", AdConfiguration.ADSENSE_CLIENT_ID)
            ins.setAttribute("data-ad-slot", AdConfiguration.ADSENSE_BANNER_SLOT_ID)
            ins.setAttribute("data-ad-format", "auto")
            ins.setAttribute("data-full-width-responsive", "true")
            
            container.appendChild(ins)
            document.body?.appendChild(container)
            adContainer = container
            
            onDispose {
                document.body?.removeChild(container)
                adContainer = null
            }
        }

        LaunchedEffect(adId) {
            // Memberikan jeda sedikit agar DOM benar-benar siap
            kotlinx.coroutines.delay(500)
            try {
                pushAdSense()
                onAdLoaded()
            } catch (e: Exception) {
                onAdFailed("AdSense push failed")
            }
        }
    }
}
