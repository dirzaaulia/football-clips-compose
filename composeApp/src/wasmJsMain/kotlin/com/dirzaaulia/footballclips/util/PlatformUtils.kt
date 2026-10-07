package com.dirzaaulia.footballclips.util

import kotlinx.browser.window

actual fun openUrl(url: String) {
    window.open(url, "_blank")
}

actual val isWasmTarget: Boolean = true
