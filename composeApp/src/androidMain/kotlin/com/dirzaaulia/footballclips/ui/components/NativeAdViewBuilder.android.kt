package com.dirzaaulia.footballclips.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

internal fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}

internal fun bindAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd, style: NativeAdStyle) {
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

internal fun populateIcon(iconView: ImageView, nativeAd: NativeAd) {
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

internal fun createHeroAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
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

    val iconView = ImageView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(64.dpToPx(ctx), 64.dpToPx(ctx)).apply {
            setMargins(0, 0, 12.dpToPx(ctx), 0)
        }
    }
    footerLayout.addView(iconView)
    nativeView.iconView = iconView

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

    headlineView.text = nativeAd.headline ?: "Promoted Partner"
    bodyView.text = nativeAd.body ?: nativeAd.advertiser ?: "Featured offer"
    populateIcon(iconView, nativeAd)

    nativeView.addView(rootLayout)
    nativeView.registerNativeAd(nativeAd, mediaView)
}
