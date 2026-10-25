package com.dirzaaulia.footballclips.monitor.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
internal fun BrowserNavigation(action: (MonitorAction) -> Unit) {
    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            action(MonitorAction.Filter(initialFilter()))
            action(MonitorAction.Navigate(initialPage(), false))
        }
        window.addEventListener("popstate", listener)
        onDispose { window.removeEventListener("popstate", listener) }
    }
}
