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
import com.dirzaaulia.footballclips.ui.adaptive.App
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val supabase: SupabaseClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

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
}
