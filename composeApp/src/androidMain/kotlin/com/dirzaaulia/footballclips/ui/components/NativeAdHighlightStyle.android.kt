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

internal fun createHighlightAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
    nativeView.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT,
    )

    val rootLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        setBackgroundColor(AndroidColor.parseColor("#111318"))
    }

    val mediaContainer = FrameLayout(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f,
        )
        setBackgroundColor(AndroidColor.parseColor("#0B0C10"))
    }

    val mediaView = MediaView(ctx).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        imageScaleType = ImageView.ScaleType.FIT_CENTER
    }
    mediaView.mediaContent = nativeAd.mediaContent
    mediaContainer.addView(mediaView)

    val badgeDrawable = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#CC0B0C10"))
        setStroke(1.dpToPx(ctx), AndroidColor.parseColor("#DFFF00"))
        cornerRadius = 10.dpToPx(ctx).toFloat()
    }
    val adBadgeView = TextView(ctx).apply {
        text = "Sponsored"
        setTextColor(AndroidColor.parseColor("#DFFF00"))
        textSize = 10f
        setTypeface(null, Typeface.BOLD)
        background = badgeDrawable
        setPadding(8.dpToPx(ctx), 3.dpToPx(ctx), 8.dpToPx(ctx), 3.dpToPx(ctx))
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins(0, 10.dpToPx(ctx), 10.dpToPx(ctx), 0)
        }
    }
    mediaContainer.addView(adBadgeView)
    rootLayout.addView(mediaContainer)

    val footer = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        setPadding(14.dpToPx(ctx), 10.dpToPx(ctx), 14.dpToPx(ctx), 12.dpToPx(ctx))
    }

    val topRow = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
    }

    val iconView = ImageView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(24.dpToPx(ctx), 24.dpToPx(ctx)).apply {
            setMargins(0, 0, 8.dpToPx(ctx), 0)
        }
    }
    topRow.addView(iconView)
    nativeView.iconView = iconView

    val headlineView = TextView(ctx).apply {
        setTextColor(AndroidColor.WHITE)
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(0, 0, 10.dpToPx(ctx), 0)
        }
    }
    topRow.addView(headlineView)
    nativeView.headlineView = headlineView

    val ctaBackground = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#DFFF00"))
        cornerRadius = 10.dpToPx(ctx).toFloat()
    }
    val ctaView = TextView(ctx).apply {
        setTextColor(AndroidColor.parseColor("#0B0C10"))
        textSize = 11f
        setTypeface(null, Typeface.BOLD)
        background = ctaBackground
        gravity = Gravity.CENTER
        setPadding(12.dpToPx(ctx), 6.dpToPx(ctx), 12.dpToPx(ctx), 6.dpToPx(ctx))
    }
    topRow.addView(ctaView)
    nativeView.callToActionView = ctaView
    footer.addView(topRow)

    val bodyView = TextView(ctx).apply {
        setTextColor(AndroidColor.parseColor("#94A3B8"))
        textSize = 12f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        setPadding(0, 4.dpToPx(ctx), 0, 0)
    }
    footer.addView(bodyView)
    nativeView.bodyView = bodyView

    rootLayout.addView(footer)
    nativeView.addView(rootLayout)

    headlineView.text = nativeAd.headline
    bodyView.text = nativeAd.body ?: nativeAd.advertiser ?: "Official Partner"
    ctaView.text = nativeAd.callToAction ?: "Open"
    populateIcon(iconView, nativeAd)

    nativeView.registerNativeAd(nativeAd, mediaView)
}
