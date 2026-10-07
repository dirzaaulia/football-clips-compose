package com.dirzaaulia.footballclips.util

import android.content.Intent
import android.net.Uri

actual fun openUrl(url: String) {
    val context = ActivityHolder.currentActivity
    if (context != null) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}

actual val isWasmTarget: Boolean = false
