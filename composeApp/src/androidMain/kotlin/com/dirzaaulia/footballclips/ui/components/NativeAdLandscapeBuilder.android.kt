package com.dirzaaulia.footballclips.ui.components.ad

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

internal fun createLandscapeHeroAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
    nativeView.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT,
    )

    val rootLayout = FrameLayout(ctx).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        setBackgroundColor(AndroidColor.parseColor("#161618"))
    }

    val mediaView = MediaView(ctx).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER,
        )
        imageScaleType = ImageView.ScaleType.FIT_CENTER
    }
    mediaView.mediaContent = nativeAd.mediaContent
    rootLayout.addView(mediaView)

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
        setPadding(8.dpToPx(ctx), 2.dpToPx(ctx), 8.dpToPx(ctx), 2.dpToPx(ctx))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            setMargins(10.dpToPx(ctx), 10.dpToPx(ctx), 0, 0)
        }
    }
    rootLayout.addView(adBadgeView)

    val footerBackground = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(
            AndroidColor.TRANSPARENT,
            AndroidColor.parseColor("#DD121214"),
            AndroidColor.parseColor("#F4121214"),
        ),
    )
    val footerLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = footerBackground
        setPadding(10.dpToPx(ctx), 8.dpToPx(ctx), 10.dpToPx(ctx), 8.dpToPx(ctx))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            gravity = Gravity.BOTTOM
        }
    }

    val iconView = ImageView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(36.dpToPx(ctx), 36.dpToPx(ctx)).apply {
            setMargins(0, 0, 8.dpToPx(ctx), 0)
        }
    }
    footerLayout.addView(iconView)
    nativeView.iconView = iconView

    val textColumn = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(0, 0, 8.dpToPx(ctx), 0)
        }
    }

    val headlineView = TextView(ctx).apply {
        setTextColor(AndroidColor.WHITE)
        textSize = 12f
        setTypeface(null, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    textColumn.addView(headlineView)
    nativeView.headlineView = headlineView

    val bodyView = TextView(ctx).apply {
        setTextColor(AndroidColor.LTGRAY)
        textSize = 10f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    textColumn.addView(bodyView)
    nativeView.bodyView = bodyView

    footerLayout.addView(textColumn)

    val ctaDrawable = GradientDrawable().apply {
        setColor(AndroidColor.TRANSPARENT)
        setStroke(2, AndroidColor.parseColor("#D4AF37"))
        cornerRadius = 10.dpToPx(ctx).toFloat()
    }
    val ctaView = TextView(ctx).apply {
        text = nativeAd.callToAction ?: "Open"
        setTextColor(AndroidColor.parseColor("#D4AF37"))
        textSize = 10f
        setTypeface(null, Typeface.BOLD)
        background = ctaDrawable
        gravity = Gravity.CENTER
        setPadding(10.dpToPx(ctx), 4.dpToPx(ctx), 10.dpToPx(ctx), 4.dpToPx(ctx))
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
