package com.dirzaaulia.footballclips

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.dirzaaulia.footballclips.di.appModules
import com.dirzaaulia.footballclips.ui.adaptive.App
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(appModules)
    }

    val container = (document.getElementById("app-viewport") as? org.w3c.dom.HTMLElement)
        ?: document.body
        ?: return
    
    ComposeViewport(container) {
        App()
    }
}
