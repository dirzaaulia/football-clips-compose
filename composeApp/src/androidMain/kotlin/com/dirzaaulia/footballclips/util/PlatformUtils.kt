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

actual fun shareText(title: String, text: String, url: String) {
    val context = ActivityHolder.currentActivity
    if (context != null) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, if (text.isNotBlank()) "$text\n$url" else url)
        }
        val chooser = Intent.createChooser(shareIntent, title)
        context.startActivity(chooser)
    }
}

actual val isWasmTarget: Boolean = false

actual fun getDeviceCountryCode(): String = java.util.Locale.getDefault().country

