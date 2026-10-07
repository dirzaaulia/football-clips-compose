package com.dirzaaulia.footballclips.util

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityHolder {
    private var currentActivityRef: WeakReference<Activity>? = null

    var currentActivity: Activity?
        get() = currentActivityRef?.get()
        set(value) {
            currentActivityRef = value?.let { WeakReference(it) }
        }
}
