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

    val body = document.body ?: return
    
    ComposeViewport(body) {
        App()
    }
}
