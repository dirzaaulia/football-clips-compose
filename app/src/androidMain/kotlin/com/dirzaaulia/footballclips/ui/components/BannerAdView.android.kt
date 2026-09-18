package com.dirzaaulia.footballclips.ui.components

import android.app.Activity
import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.dirzaaulia.footballclips.BuildConfig
import com.dirzaaulia.footballclips.data.admob.NativeAdCache
import com.dirzaaulia.footballclips.data.admob.formatAdError
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

private const val TAG = "AdMob_Native"

private fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

@Composable
actual fun BannerAdView(
    onAdLoaded: () -> Unit,
    onAdFailed: (String) -> Unit,
    modifier: Modifier,
    style: NativeAdStyle
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Box(modifier = modifier) {
        activity?.let { act ->
            var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

            DisposableEffect(act) {
                fun attachAdEventCallback(ad: NativeAd, adStyle: NativeAdStyle) {
                    ad.adEventCallback = object : NativeAdEventCallback {
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "📺 FULL SCREEN AD SHOWED [$adStyle]: NativeAd '${ad.headline}'")
                        }
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "🚪 FULL SCREEN AD DISMISSED [$adStyle]")
                        }
                        override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                            Log.e(TAG, "🔴 FULL SCREEN AD SHOW FAILED [$adStyle]: ${fullScreenContentError.message} (Code: ${fullScreenContentError.code})")
                        }
                        override fun onAdImpression() {
                            Log.d(TAG, "👁️ IMPRESSION RECORDED [$adStyle]: NativeAd '${ad.headline}'")
                        }
                        override fun onAdClicked() {
                            Log.d(TAG, "👆 CLICK RECORDED [$adStyle]: NativeAd '${ad.headline}'")
                        }
                    }
                }

                fun loadNativeAd() {
                    val adUnitId = if (BuildConfig.DEBUG) {
                        "ca-app-pub-3940256099942544/2247696110" // Google AdMob Official Native Advanced Test Unit ID
                    } else {
                        BuildConfig.ADMOB_NATIVE_ID.ifEmpty { "ca-app-pub-3940256099942544/2247696110" }
                    }

                    Log.d(TAG, "🔍 AD REQUEST INITIATED [$style]: Unit ID = $adUnitId, Debug = ${BuildConfig.DEBUG}")

                    // 1. Serve immediately from cache if available (0 latency)
                    val cachedAd = NativeAdCache.getAd(adUnitId)
                    if (cachedAd != null) {
                        Log.d(TAG, "🟢 SERVED FROM CACHE [$style]: NativeAd '${cachedAd.headline}'")
                        attachAdEventCallback(cachedAd, style)
                        nativeAd = cachedAd
                        onAdLoaded()
                        NativeAdCache.preloadAd(act.applicationContext, adUnitId)
                        return
                    }

                    // 2. Fetch fresh batch of 5 ads if cache is empty
                    Log.d(TAG, "📡 BATCH NETWORK FETCH INITIATED [$style]: Cache empty, requesting batch of 5 NativeAds")
                    val types = listOf(NativeAd.NativeAdType.NATIVE)
                    val request = NativeAdRequest.Builder(adUnitId, types).build()
                    try {
                        NativeAdLoader.load(request, 5, object : NativeAdLoaderCallback {
                            override fun onNativeAdLoaded(ad: NativeAd) {
                                if (nativeAd == null) {
                                    Log.d(TAG, "🟢 BATCH AD #1 ASSIGNED [$style]: NativeAd '${ad.headline}'")
                                    attachAdEventCallback(ad, style)
                                    nativeAd = ad
                                    onAdLoaded()
                                } else {
                                    Log.d(TAG, "📥 BATCH AD STORED TO CACHE: NativeAd '${ad.headline}'")
                                    NativeAdCache.putAd(adUnitId, ad)
                                }
                                NativeAdCache.preloadAd(act.applicationContext, adUnitId)
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                Log.e(TAG, formatAdError(adError, "NATIVE [$style]"))
                                if (nativeAd == null) {
                                    onAdFailed(adError.message)
                                }
                            }

                            override fun onAdLoadingCompleted() {
                                Log.d(TAG, "✅ BATCH FETCH COMPLETED FOR UNIT $adUnitId")
                            }
                        })
                    } catch (t: Throwable) {
                        Log.e(TAG, "💥 NETWORK FETCH EXCEPTION [$style]: ${t.message}", t)
                        onAdFailed(t.message ?: "Native ad error")
                    }
                }

                try {
                    MobileAds.initialize(
                        act.applicationContext,
                        InitializationConfig.Builder(BuildConfig.ADMOB_APP_ID).build()
                    ) {
                        loadNativeAd()
                    }
                } catch (t: Throwable) {
                    loadNativeAd()
                }

                onDispose {
                    nativeAd?.destroy()
                }
            }

            val currentNativeAd = nativeAd
            if (currentNativeAd != null) {
                AndroidView(
                    factory = { ctx ->
                        val nativeView = NativeAdView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )
                        }

                        bindAdView(ctx, nativeView, currentNativeAd, style)
                        nativeView
                    },
                    update = { nativeView ->
                        bindAdView(nativeView.context, nativeView, currentNativeAd, style)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun bindAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd, style: NativeAdStyle) {
    nativeView.removeAllViews()
    when (style) {
        NativeAdStyle.HERO -> createHeroAdView(ctx, nativeView, nativeAd)
        NativeAdStyle.MATCH -> createMatchAdView(ctx, nativeView, nativeAd)
        NativeAdStyle.HIGHLIGHT -> createHighlightAdView(ctx, nativeView, nativeAd)
    }
    nativeView.post {
        nativeView.requestLayout()
        nativeView.invalidate()
    }
}

private fun populateIcon(iconView: ImageView, nativeAd: NativeAd) {
    val icon = nativeAd.icon
    if (icon?.drawable != null) {
        iconView.setImageDrawable(icon.drawable)
        iconView.visibility = View.VISIBLE
    } else if (icon?.uri != null) {
        iconView.setImageURI(icon.uri)
        iconView.visibility = View.VISIBLE
    } else {
        iconView.visibility = View.GONE
    }
}

/**
 * 1. HERO AD STYLE: Full-Bleed Card Layout (Media fills 100% of card area, Metadata overlays at bottom)
 */
private fun createHeroAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
    nativeView.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )

    val rootLayout = FrameLayout(ctx).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(AndroidColor.parseColor("#161618"))
    }

    // 1. Full-Card Media View (Fills max space while preserving media aspect ratio)
    val mediaView = MediaView(ctx).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        imageScaleType = ImageView.ScaleType.FIT_CENTER
    }
    mediaView.mediaContent = nativeAd.mediaContent
    rootLayout.addView(mediaView)

    // 2. Top-Left Gold "Ad" Badge
    val adBadgeDrawable = GradientDrawable().apply {
        setColor(AndroidColor.TRANSPARENT)
        setStroke(2, AndroidColor.parseColor("#D4AF37"))
        cornerRadius = 8.dpToPx(ctx).toFloat()
    }
    val adBadgeView = TextView(ctx).apply {
        text = "Ad"
        setTextColor(AndroidColor.parseColor("#D4AF37"))
        textSize = 10f
        setTypeface(null, Typeface.BOLD)
        background = adBadgeDrawable
        setPadding(12.dpToPx(ctx), 4.dpToPx(ctx), 12.dpToPx(ctx), 4.dpToPx(ctx))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            setMargins(16.dpToPx(ctx), 16.dpToPx(ctx), 0, 0)
        }
    }
    rootLayout.addView(adBadgeView)

    // 3. Bottom Gradient Footer Overlay
    val footerBackground = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(
            AndroidColor.TRANSPARENT,
            AndroidColor.parseColor("#CC121214"),
            AndroidColor.parseColor("#F2121214")
        )
    )
    val footerLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = footerBackground
        setPadding(16.dpToPx(ctx), 20.dpToPx(ctx), 16.dpToPx(ctx), 14.dpToPx(ctx))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
        }
    }

    // Brand/App Icon
    val iconView = ImageView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(64.dpToPx(ctx), 64.dpToPx(ctx)).apply {
            setMargins(0, 0, 12.dpToPx(ctx), 0)
        }
    }
    footerLayout.addView(iconView)
    nativeView.iconView = iconView

    // Text Details
    val textColumn = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(0, 0, 12.dpToPx(ctx), 0)
        }
    }

    val headlineView = TextView(ctx).apply {
        setTextColor(AndroidColor.WHITE)
        textSize = 15f
        setTypeface(null, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    textColumn.addView(headlineView)
    nativeView.headlineView = headlineView

    val bodyView = TextView(ctx).apply {
        setTextColor(AndroidColor.LTGRAY)
        textSize = 11f
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    textColumn.addView(bodyView)
    nativeView.bodyView = bodyView

    footerLayout.addView(textColumn)

    // Outlined Gold CTA Button
    val ctaDrawable = GradientDrawable().apply {
        setColor(AndroidColor.TRANSPARENT)
        setStroke(2, AndroidColor.parseColor("#D4AF37"))
        cornerRadius = 14.dpToPx(ctx).toFloat()
    }
    val ctaView = TextView(ctx).apply {
        text = nativeAd.callToAction ?: "Open"
        setTextColor(AndroidColor.parseColor("#D4AF37"))
        textSize = 11f
        setTypeface(null, Typeface.BOLD)
        background = ctaDrawable
        gravity = Gravity.CENTER
        setPadding(16.dpToPx(ctx), 8.dpToPx(ctx), 16.dpToPx(ctx), 8.dpToPx(ctx))
    }
    footerLayout.addView(ctaView)
    nativeView.callToActionView = ctaView

    rootLayout.addView(footerLayout)

    // Set Data
    headlineView.text = nativeAd.headline ?: "Promoted Partner"
    bodyView.text = nativeAd.body ?: nativeAd.advertiser ?: "Featured offer"
    populateIcon(iconView, nativeAd)

    nativeView.addView(rootLayout)
    nativeView.registerNativeAd(nativeAd, mediaView)
}

/**
 * 2. MATCH AD STYLE: In-Feed Match Card Layout (Top Media ~135dp, Bottom Details Bar ~75dp)
 */
private fun createMatchAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
    nativeView.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )

    val rootLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(AndroidColor.parseColor("#161618"))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }

    // 1. Top Media Container with explicit pixel height
    val topMediaBox = FrameLayout(ctx).apply {
        setBackgroundColor(AndroidColor.parseColor("#121214"))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            135.dpToPx(ctx)
        )
    }

    val mediaView = MediaView(ctx).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        imageScaleType = ImageView.ScaleType.FIT_CENTER
    }
    mediaView.mediaContent = nativeAd.mediaContent
    topMediaBox.addView(mediaView)

    // Top-Left Gold "Ad" Badge
    val adBadgeDrawable = GradientDrawable().apply {
        setColor(AndroidColor.TRANSPARENT)
        setStroke(2, AndroidColor.parseColor("#D4AF37"))
        cornerRadius = 6.dpToPx(ctx).toFloat()
    }
    val adBadgeView = TextView(ctx).apply {
        text = "Ad"
        setTextColor(AndroidColor.parseColor("#D4AF37"))
        textSize = 10f
        setTypeface(null, Typeface.BOLD)
        background = adBadgeDrawable
        setPadding(10.dpToPx(ctx), 3.dpToPx(ctx), 10.dpToPx(ctx), 3.dpToPx(ctx))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            setMargins(12.dpToPx(ctx), 12.dpToPx(ctx), 0, 0)
        }
    }
    topMediaBox.addView(adBadgeView)

    rootLayout.addView(topMediaBox)

    // 2. Bottom Metadata Bar with explicit pixel height
    val bottomLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(14.dpToPx(ctx), 10.dpToPx(ctx), 14.dpToPx(ctx), 10.dpToPx(ctx))
        setBackgroundColor(AndroidColor.parseColor("#161618"))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            75.dpToPx(ctx)
        )
    }

    val iconView = ImageView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(52.dpToPx(ctx), 52.dpToPx(ctx)).apply {
            setMargins(0, 0, 10.dpToPx(ctx), 0)
        }
    }
    bottomLayout.addView(iconView)
    nativeView.iconView = iconView

    val textColumn = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(0, 0, 10.dpToPx(ctx), 0)
        }
    }

    val headlineView = TextView(ctx).apply {
        setTextColor(AndroidColor.WHITE)
        textSize = 13f
        setTypeface(null, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    textColumn.addView(headlineView)
    nativeView.headlineView = headlineView

    val bodyView = TextView(ctx).apply {
        setTextColor(AndroidColor.LTGRAY)
        textSize = 11f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    textColumn.addView(bodyView)
    nativeView.bodyView = bodyView

    bottomLayout.addView(textColumn)

    val ctaDrawable = GradientDrawable().apply {
        setColor(AndroidColor.TRANSPARENT)
        setStroke(2, AndroidColor.parseColor("#D4AF37"))
        cornerRadius = 12.dpToPx(ctx).toFloat()
    }
    val ctaView = TextView(ctx).apply {
        text = nativeAd.callToAction ?: "Open"
        setTextColor(AndroidColor.parseColor("#D4AF37"))
        textSize = 11f
        setTypeface(null, Typeface.BOLD)
        background = ctaDrawable
        gravity = Gravity.CENTER
        setPadding(14.dpToPx(ctx), 6.dpToPx(ctx), 14.dpToPx(ctx), 6.dpToPx(ctx))
    }
    bottomLayout.addView(ctaView)
    nativeView.callToActionView = ctaView

    rootLayout.addView(bottomLayout)

    // Set Data
    headlineView.text = nativeAd.headline ?: "Promoted Match"
    bodyView.text = nativeAd.body ?: nativeAd.advertiser ?: "Sponsored Content"
    populateIcon(iconView, nativeAd)

    nativeView.addView(rootLayout)
    nativeView.registerNativeAd(nativeAd, mediaView)
}

/**
 * 3. HIGHLIGHT AD STYLE: Highlight Card Layout (Horizontal ~124dp matching VerticalHighlightCard)
 */
private fun createHighlightAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
    nativeView.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )

    val rootLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundColor(AndroidColor.parseColor("#1E1E22"))
        setPadding(12.dpToPx(ctx), 12.dpToPx(ctx), 12.dpToPx(ctx), 12.dpToPx(ctx))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }

    // Left Media Thumbnail (Must be at least 120x120dp for AdMob video native ads -> 125x125dp)
    val mediaBox = FrameLayout(ctx).apply {
        setBackgroundColor(AndroidColor.parseColor("#121214"))
        layoutParams = LinearLayout.LayoutParams(125.dpToPx(ctx), 125.dpToPx(ctx)).apply {
            setMargins(0, 0, 12.dpToPx(ctx), 0)
        }
    }

    val mediaView = MediaView(ctx).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        imageScaleType = ImageView.ScaleType.FIT_CENTER
    }
    mediaView.mediaContent = nativeAd.mediaContent
    mediaBox.addView(mediaView)

    val adBadgeDrawable = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#B3000000"))
        setStroke(2, AndroidColor.parseColor("#D4AF37"))
        cornerRadius = 6.dpToPx(ctx).toFloat()
    }
    val adBadgeView = TextView(ctx).apply {
        text = "Ad"
        setTextColor(AndroidColor.parseColor("#D4AF37"))
        textSize = 9f
        setTypeface(null, Typeface.BOLD)
        background = adBadgeDrawable
        setPadding(8.dpToPx(ctx), 2.dpToPx(ctx), 8.dpToPx(ctx), 2.dpToPx(ctx))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            setMargins(6.dpToPx(ctx), 6.dpToPx(ctx), 0, 0)
        }
    }
    mediaBox.addView(adBadgeView)

    rootLayout.addView(mediaBox)

    // Right Column (Headline + Body + CTA)
    val rightColumn = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val headlineView = TextView(ctx).apply {
        setTextColor(AndroidColor.WHITE)
        textSize = 13f
        setTypeface(null, Typeface.BOLD)
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    rightColumn.addView(headlineView)
    nativeView.headlineView = headlineView

    val bodyView = TextView(ctx).apply {
        setTextColor(AndroidColor.parseColor("#AAAAAA"))
        textSize = 11f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    rightColumn.addView(bodyView)
    nativeView.bodyView = bodyView

    val ctaDrawable = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#D4AF37"))
        cornerRadius = 10.dpToPx(ctx).toFloat()
    }
    val ctaView = TextView(ctx).apply {
        text = nativeAd.callToAction ?: "View"
        setTextColor(AndroidColor.BLACK)
        textSize = 10f
        setTypeface(null, Typeface.BOLD)
        background = ctaDrawable
        gravity = Gravity.CENTER
        setPadding(12.dpToPx(ctx), 6.dpToPx(ctx), 12.dpToPx(ctx), 6.dpToPx(ctx))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 6.dpToPx(ctx), 0, 0)
        }
    }
    rightColumn.addView(ctaView)
    nativeView.callToActionView = ctaView

    rootLayout.addView(rightColumn)

    headlineView.text = nativeAd.headline ?: "Featured Clip"
    bodyView.text = nativeAd.body ?: nativeAd.advertiser ?: "Promoted"

    nativeView.addView(rootLayout)
    nativeView.registerNativeAd(nativeAd, mediaView)
}
