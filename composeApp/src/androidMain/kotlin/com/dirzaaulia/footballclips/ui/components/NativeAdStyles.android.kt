package com.dirzaaulia.footballclips.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

internal fun createMatchAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
    createHeroAdView(ctx, nativeView, nativeAd)
}

internal fun createHighlightAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
    nativeView.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )

    val rootLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(AndroidColor.parseColor("#18181B"))
        setPadding(12.dpToPx(ctx), 12.dpToPx(ctx), 12.dpToPx(ctx), 12.dpToPx(ctx))
    }

    // MediaView container strictly enforcing 120dp x 120dp per Google AdMob Policy
    val mediaContainer = FrameLayout(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(120.dpToPx(ctx), 120.dpToPx(ctx)).apply {
            setMargins(0, 0, 12.dpToPx(ctx), 0)
        }
    }

    val mediaView = MediaView(ctx).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        imageScaleType = ImageView.ScaleType.FIT_CENTER
    }
    mediaView.mediaContent = nativeAd.mediaContent
    mediaContainer.addView(mediaView)

    val adBadgeDrawable = GradientDrawable().apply {
        setColor(AndroidColor.TRANSPARENT)
        setStroke(2, AndroidColor.parseColor("#D4AF37"))
        cornerRadius = 6.dpToPx(ctx).toFloat()
    }
    val adBadgeView = TextView(ctx).apply {
        text = "Ad"
        setTextColor(AndroidColor.parseColor("#D4AF37"))
        textSize = 9f
        setTypeface(null, Typeface.BOLD)
        background = adBadgeDrawable
        setPadding(6.dpToPx(ctx), 2.dpToPx(ctx), 6.dpToPx(ctx), 2.dpToPx(ctx))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            setMargins(6.dpToPx(ctx), 6.dpToPx(ctx), 0, 0)
        }
    }
    mediaContainer.addView(adBadgeView)
    rootLayout.addView(mediaContainer)

    // Right Column: Headline, Body, Icon & CTA
    val textColumn = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val headerRow = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 4.dpToPx(ctx))
        }
    }

    val iconView = ImageView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(24.dpToPx(ctx), 24.dpToPx(ctx)).apply {
            setMargins(0, 0, 8.dpToPx(ctx), 0)
        }
    }
    headerRow.addView(iconView)
    nativeView.iconView = iconView

    val headlineView = TextView(ctx).apply {
        setTextColor(AndroidColor.WHITE)
        textSize = 13f
        setTypeface(null, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    headerRow.addView(headlineView)
    nativeView.headlineView = headlineView

    textColumn.addView(headerRow)

    val bodyView = TextView(ctx).apply {
        setTextColor(AndroidColor.parseColor("#A1A1AA"))
        textSize = 11f
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    textColumn.addView(bodyView)
    nativeView.bodyView = bodyView

    val ctaBackground = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#D4AF37"))
        cornerRadius = 8.dpToPx(ctx).toFloat()
    }
    val ctaView = TextView(ctx).apply {
        setTextColor(AndroidColor.BLACK)
        textSize = 11f
        setTypeface(null, Typeface.BOLD)
        background = ctaBackground
        gravity = Gravity.CENTER
        setPadding(12.dpToPx(ctx), 6.dpToPx(ctx), 12.dpToPx(ctx), 6.dpToPx(ctx))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 8.dpToPx(ctx), 0, 0)
        }
    }
    textColumn.addView(ctaView)
    nativeView.callToActionView = ctaView

    rootLayout.addView(textColumn)
    nativeView.addView(rootLayout)

    headlineView.text = nativeAd.headline
    bodyView.text = nativeAd.body ?: nativeAd.advertiser
    ctaView.text = nativeAd.callToAction ?: "View"
    populateIcon(iconView, nativeAd)

    nativeView.registerNativeAd(nativeAd, mediaView)
}
