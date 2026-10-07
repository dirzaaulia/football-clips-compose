package com.dirzaaulia.footballclips.util

import android.content.Context

actual val isDebugBuild: Boolean
    get() = getBuildConfigBoolean("DEBUG") {
        val context = ActivityHolder.currentActivity?.applicationContext
        if (context != null) {
            (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } else false
    }

fun getBuildConfigString(fieldName: String, defaultValue: String = ""): String {
    return try {
        val clazz = Class.forName("com.dirzaaulia.footballclips.BuildConfig")
        val field = clazz.getField(fieldName)
        (field.get(null) as? String)?.takeIf { it.isNotBlank() } ?: defaultValue
    } catch (_: Throwable) {
        defaultValue
    }
}

fun getBuildConfigBoolean(fieldName: String, fallback: () -> Boolean = { false }): Boolean {
    return try {
        val clazz = Class.forName("com.dirzaaulia.footballclips.BuildConfig")
        val field = clazz.getField(fieldName)
        field.getBoolean(null)
    } catch (_: Throwable) {
        fallback()
    }
}
