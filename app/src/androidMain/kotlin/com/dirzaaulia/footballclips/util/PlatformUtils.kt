package com.dirzaaulia.footballclips.util

import android.content.Intent
import android.net.Uri
import com.dirzaaulia.footballclips.FootballClipsApplication

actual fun openUrl(url: String) {
    val context = FootballClipsApplication.getCurrentActivity()
    if (context != null) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}

actual val isWasmTarget: Boolean = false
