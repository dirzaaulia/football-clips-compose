package com.dirzaaulia.footballclips

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.dirzaaulia.footballclips.telemetry.data.TelemetryRepository
import com.dirzaaulia.footballclips.telemetry.ui.dashboard.TelemetryScreen
import com.dirzaaulia.footballclips.telemetry.ui.theme.TelemetryTheme
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val repository = TelemetryRepository()
    val container = (document.getElementById("app-viewport") as? org.w3c.dom.HTMLElement)
        ?: document.body
        ?: return

    ComposeViewport(container) {
        TelemetryTheme {
            TelemetryScreen(repository = repository)
        }
    }
}
