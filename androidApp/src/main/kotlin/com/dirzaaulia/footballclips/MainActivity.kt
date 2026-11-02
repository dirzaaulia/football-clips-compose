package com.dirzaaulia.footballclips

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dirzaaulia.footballclips.ui.adaptive.App
import com.dirzaaulia.footballclips.update.InAppUpdateManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val supabase: SupabaseClient by inject()
    private lateinit var inAppUpdateManager: InAppUpdateManager
    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // Handle result code if needed (RESULT_OK, RESULT_CANCELED)
    }

    private var deepLinkMatchId by mutableStateOf<String?>(null)

    private fun extractMatchId(intent: Intent?): String? {
        val uri = intent?.data
        if (uri != null) {
            if (uri.scheme == "https" && uri.host == "fc.dirzaaulia.com" && uri.path?.startsWith("/match") == true) {
                val queryId = uri.getQueryParameter("id")
                if (!queryId.isNullOrBlank()) return queryId
                val segment = uri.lastPathSegment
                if (segment != null && segment != "match") return segment
            }
            if (uri.scheme == "drzfc" && (uri.host == "match" || uri.path?.startsWith("/match") == true)) {
                val queryId = uri.getQueryParameter("id")
                if (!queryId.isNullOrBlank()) return queryId
                val segment = uri.lastPathSegment
                if (segment != null && segment != "match") return segment
            }
        }

        // Support background FCM notifications clicked from Android system tray
        val extraMatchId = intent?.getStringExtra("match_id") ?: intent?.getStringExtra("EXTRA_MATCH_ID")
        if (!extraMatchId.isNullOrBlank()) {
            return extraMatchId
        }

        val extraUrl = intent?.getStringExtra("url")
        if (!extraUrl.isNullOrBlank() && extraUrl.startsWith("/match/")) {
            val id = extraUrl.removePrefix("/match/").substringBefore('?').substringBefore('/')
            if (id.isNotBlank()) return id
        }

        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Initialize Google Play In-App Update Manager
        inAppUpdateManager = InAppUpdateManager(this)
        inAppUpdateManager.checkForAppUpdate(
            updateLauncher = updateResultLauncher,
            onFlexibleDownloadComplete = {
                runOnUiThread {
                    showUpdateDownloadedDialog()
                }
            },
        )

        // Handle Deep Link if activity is created via URL
        supabase.handleDeeplinks(intent)
        deepLinkMatchId = extractMatchId(intent)

        // Prominent, cinematic zoom-and-fade exit animation for splash screen
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val iconView = splashScreenView.iconView

            val scaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, 2.2f)
            val scaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, 2.2f)
            val alphaIcon = ObjectAnimator.ofFloat(iconView, View.ALPHA, 1f, 0f)
            val alphaView = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f)
            val translateY = ObjectAnimator.ofFloat(splashScreenView.view, View.TRANSLATION_Y, 0f, -60f)

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, alphaIcon, alphaView, translateY)
                duration = 600L
                interpolator = FastOutSlowInInterpolator()
                doOnEnd { splashScreenView.remove() }
                start()
            }
        }

        enableEdgeToEdge()
        setContent {
            App(
                initialMatchId = deepLinkMatchId,
                onMatchConsumed = { deepLinkMatchId = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle Deep Link if activity receives new intent
        supabase.handleDeeplinks(intent)
        val matchId = extractMatchId(intent)
        if (matchId != null) {
            deepLinkMatchId = matchId
        }
    }

    override fun onResume() {
        super.onResume()
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.onResumeCheck(updateResultLauncher)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.onDestroy()
        }
    }

    private fun showUpdateDownloadedDialog() {
        if (isFinishing || isDestroyed) return
        android.app.AlertDialog.Builder(this)
            .setTitle("Update Downloaded")
            .setMessage("A fresh version of Football Clips has been downloaded. Restart the app to apply the latest features and match updates?")
            .setPositiveButton("Restart") { _, _ ->
                inAppUpdateManager.completeFlexibleUpdate()
            }
            .setNegativeButton("Later", null)
            .setCancelable(true)
            .show()
    }
}
