package com.dirzaaulia.footballclips.data.local

import android.app.UiModeManager
import android.content.Context
import android.os.Build

internal fun applySystemNightMode(context: Context, isDark: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        try {
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            uiModeManager?.setApplicationNightMode(
                if (isDark) {
                    UiModeManager.MODE_NIGHT_YES
                } else {
                    UiModeManager.MODE_NIGHT_NO
                },
            )
        } catch (_: Throwable) {
        }
    }
}
