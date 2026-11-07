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

internal fun createMatchAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
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
        setBackgroundColor(AndroidColor.parseColor("#18181B"))
        setPadding(12.dpToPx(ctx), 8.dpToPx(ctx), 12.dpToPx(ctx), 8.dpToPx(ctx))
    }

    val headerRow = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            setMargins(0, 0, 0, 6.dpToPx(ctx))
        }
    }

    val adBadgeDrawable = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#27272A"))
        setStroke(1.dpToPx(ctx), AndroidColor.parseColor("#D4AF37"))
        cornerRadius = 6.dpToPx(ctx).toFloat()
    }
    val adBadgeView = TextView(ctx).apply {
        text = "Sponsored"
        setTextColor(AndroidColor.parseColor("#D4AF37"))
        textSize = 9f
        setTypeface(null, Typeface.BOLD)
        background = adBadgeDrawable
        setPadding(6.dpToPx(ctx), 2.dpToPx(ctx), 6.dpToPx(ctx), 2.dpToPx(ctx))
    }
    headerRow.addView(adBadgeView)

    val advertiserView = TextView(ctx).apply {
        text = nativeAd.advertiser ?: "Featured"
        setTextColor(AndroidColor.parseColor("#94A3B8"))
        textSize = 10f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        setPadding(8.dpToPx(ctx), 0, 0, 0)
    }
    headerRow.addView(advertiserView)
    rootLayout.addView(headerRow)

    val contentRow = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f,
        )
    }

    val mediaContainer = FrameLayout(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(120.dpToPx(ctx), 120.dpToPx(ctx)).apply {
            setMargins(0, 0, 10.dpToPx(ctx), 0)
        }
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
    contentRow.addView(mediaContainer)

    val infoColumn = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val headlineView = TextView(ctx).apply {
        text = nativeAd.headline
        setTextColor(AndroidColor.WHITE)
        textSize = 12f
        setTypeface(null, Typeface.BOLD)
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    infoColumn.addView(headlineView)
    nativeView.headlineView = headlineView

    val bodyView = TextView(ctx).apply {
        text = nativeAd.body ?: ""
        setTextColor(AndroidColor.parseColor("#A1A1AA"))
        textSize = 10f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    if (!nativeAd.body.isNullOrBlank()) {
        infoColumn.addView(bodyView)
        nativeView.bodyView = bodyView
    }

    val ctaBackground = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#27272A"))
        setStroke(1.dpToPx(ctx), AndroidColor.parseColor("#52525B"))
        cornerRadius = 8.dpToPx(ctx).toFloat()
    }
    val ctaView = TextView(ctx).apply {
        text = nativeAd.callToAction ?: "View"
        setTextColor(AndroidColor.parseColor("#7098F8"))
        textSize = 11f
        setTypeface(null, Typeface.BOLD)
        background = ctaBackground
        gravity = Gravity.CENTER
        setPadding(10.dpToPx(ctx), 5.dpToPx(ctx), 10.dpToPx(ctx), 5.dpToPx(ctx))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            setMargins(0, 6.dpToPx(ctx), 0, 0)
        }
    }
    infoColumn.addView(ctaView)
    nativeView.callToActionView = ctaView

    contentRow.addView(infoColumn)
    rootLayout.addView(contentRow)
    nativeView.addView(rootLayout)

    nativeView.registerNativeAd(nativeAd, mediaView)
}

internal fun createStandingsAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
    nativeView.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT,
    )

    val rootLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        setBackgroundColor(AndroidColor.parseColor("#181C20"))
        setPadding(10.dpToPx(ctx), 8.dpToPx(ctx), 10.dpToPx(ctx), 8.dpToPx(ctx))
    }

    val mediaContainer = FrameLayout(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(120.dpToPx(ctx), 120.dpToPx(ctx)).apply {
            setMargins(0, 0, 10.dpToPx(ctx), 0)
        }
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
    rootLayout.addView(mediaContainer)

    val textCol = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val badgeDrawable = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#1E293B"))
        setStroke(1.dpToPx(ctx), AndroidColor.parseColor("#334155"))
        cornerRadius = 4.dpToPx(ctx).toFloat()
    }
    val badgeView = TextView(ctx).apply {
        text = "Ad • Standings Partner"
        setTextColor(AndroidColor.parseColor("#94A3B8"))
        textSize = 9f
        setTypeface(null, Typeface.BOLD)
        background = badgeDrawable
        setPadding(6.dpToPx(ctx), 2.dpToPx(ctx), 6.dpToPx(ctx), 2.dpToPx(ctx))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            setMargins(0, 0, 0, 4.dpToPx(ctx))
        }
    }
    textCol.addView(badgeView)

    val headlineView = TextView(ctx).apply {
        text = nativeAd.headline
        setTextColor(AndroidColor.WHITE)
        textSize = 12f
        setTypeface(null, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    textCol.addView(headlineView)
    nativeView.headlineView = headlineView

    val bodyView = TextView(ctx).apply {
        text = nativeAd.body ?: (nativeAd.advertiser ?: "")
        setTextColor(AndroidColor.parseColor("#94A3B8"))
        textSize = 10f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    textCol.addView(bodyView)
    nativeView.bodyView = bodyView

    val ctaBg = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#7098F8"))
        cornerRadius = 6.dpToPx(ctx).toFloat()
    }
    val ctaView = TextView(ctx).apply {
        text = nativeAd.callToAction ?: "View"
        setTextColor(AndroidColor.parseColor("#0F172A"))
        textSize = 11f
        setTypeface(null, Typeface.BOLD)
        background = ctaBg
        gravity = Gravity.CENTER
        setPadding(10.dpToPx(ctx), 4.dpToPx(ctx), 10.dpToPx(ctx), 4.dpToPx(ctx))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            setMargins(0, 6.dpToPx(ctx), 0, 0)
        }
    }
    textCol.addView(ctaView)
    nativeView.callToActionView = ctaView

    rootLayout.addView(textCol)
    nativeView.addView(rootLayout)

}

