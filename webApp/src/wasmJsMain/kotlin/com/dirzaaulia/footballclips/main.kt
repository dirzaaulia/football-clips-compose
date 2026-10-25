package com.dirzaaulia.footballclips

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.dirzaaulia.footballclips.monitor.presentation.PersonalMonitor
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val container = (document.getElementById("app-viewport") as? org.w3c.dom.HTMLElement)
        ?: document.body
        ?: return

    ComposeViewport(container) {
        PersonalMonitor()
    }
}
