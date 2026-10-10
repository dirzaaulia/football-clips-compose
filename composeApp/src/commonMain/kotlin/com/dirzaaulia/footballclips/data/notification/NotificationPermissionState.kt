package com.dirzaaulia.footballclips.data.notification

import androidx.compose.runtime.Composable

interface NotificationPermissionState {
    val hasPermission: Boolean
    val isPermanentlyDenied: Boolean
    fun launchPermissionRequest()
    fun openSettings()
}

@Composable
expect fun rememberNotificationPermissionState(): NotificationPermissionState
