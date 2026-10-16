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

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Initialize Google Play In-App Update Manager
        inAppUpdateManager = InAppUpdateManager(this)
        inAppUpdateManager.checkForAppUpdate(updateResultLauncher)

        // Handle Deep Link if activity is created via URL
        supabase.handleDeeplinks(intent)

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
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle Deep Link if activity receives new intent
        supabase.handleDeeplinks(intent)
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
}
