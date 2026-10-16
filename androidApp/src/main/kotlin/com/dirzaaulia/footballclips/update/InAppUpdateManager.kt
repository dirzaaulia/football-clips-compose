package com.dirzaaulia.footballclips.update

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Production In-App Update Manager powered by Google Play Core SDK.
 * Supports:
 * 1. Immediate (Force Update): Blocks app usage until update is applied (configured via Play Console priority or force flag).
 * 2. Flexible (Background Update): Downloads in the background and prompts user to restart.
 * 3. Resume Protection: Resumes pending updates if the user backgrounds and reopens the app.
 */
class InAppUpdateManager(private val activity: Activity) {

    private val tag = "InAppUpdateManager"
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var installStateUpdatedListener: InstallStateUpdatedListener? = null

    /**
     * Checks Google Play Store for an available update and initiates either
     * an IMMEDIATE (force) or FLEXIBLE flow.
     *
     * @param updateLauncher ActivityResultLauncher to handle the update result intent
     * @param forceImmediate If true, enforces AppUpdateType.IMMEDIATE regardless of Play Console priority
     * @param onFlexibleDownloadComplete Callback when flexible update has finished downloading
     */
    fun checkForAppUpdate(
        updateLauncher: ActivityResultLauncher<IntentSenderRequest>,
        forceImmediate: Boolean = false,
        onFlexibleDownloadComplete: (() -> Unit)? = null
    ) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            val availability = appUpdateInfo.updateAvailability()
            val priority = appUpdateInfo.updatePriority()
            val stalenessDays = appUpdateInfo.clientVersionStalenessDays() ?: 0

            Log.d(tag, "Update check result: availability=$availability, priority=$priority, stalenessDays=$stalenessDays")

            if (availability == UpdateAvailability.UPDATE_AVAILABLE) {
                // Determine update mode:
                // Priority >= 4 (set in Play Console release) OR staleness >= 14 days OR manual force flag -> IMMEDIATE
                val isImmediate = forceImmediate || priority >= 4 || stalenessDays >= 14

                if (isImmediate && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    Log.i(tag, "Starting IMMEDIATE (Force) update flow...")
                    startUpdateFlow(appUpdateInfo, AppUpdateType.IMMEDIATE, updateLauncher)
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    Log.i(tag, "Starting FLEXIBLE background update flow...")
                    registerFlexibleListener(onFlexibleDownloadComplete)
                    startUpdateFlow(appUpdateInfo, AppUpdateType.FLEXIBLE, updateLauncher)
                }
            }
        }.addOnFailureListener { e ->
            Log.w(tag, "Failed to check for Google Play In-App updates: ${e.message}")
        }
    }

    /**
     * Ensures an immediate update flow resumes if the user backgrounds and reopens the app.
     * Call this inside Activity onResume().
     */
    fun onResumeCheck(updateLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                Log.i(tag, "Resuming active in-progress IMMEDIATE update...")
                startUpdateFlow(appUpdateInfo, AppUpdateType.IMMEDIATE, updateLauncher)
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Log.i(tag, "Flexible update already downloaded. Ready to complete.")
            }
        }
    }

    /**
     * Completes flexible update installation by restarting the app.
     */
    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }

    /**
     * Cleans up registered listeners. Call inside Activity onDestroy().
     */
    fun onDestroy() {
        installStateUpdatedListener?.let {
            appUpdateManager.unregisterListener(it)
        }
    }

    private fun startUpdateFlow(
        appUpdateInfo: AppUpdateInfo,
        @AppUpdateType updateType: Int,
        updateLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val options = AppUpdateOptions.newBuilder(updateType).build()
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            updateLauncher,
            options
        )
    }

    private fun registerFlexibleListener(onComplete: (() -> Unit)?) {
        installStateUpdatedListener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                Log.i(tag, "Flexible in-app update downloaded successfully.")
                onComplete?.invoke()
            }
        }
        appUpdateManager.registerListener(installStateUpdatedListener!!)
    }
}
