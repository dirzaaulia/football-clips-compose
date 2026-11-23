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

internal fun createShortsAdView(ctx: Context, nativeView: NativeAdView, nativeAd: NativeAd) {
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
        setBackgroundColor(AndroidColor.parseColor("#0A0B0E"))
        setPadding(16.dpToPx(ctx), 16.dpToPx(ctx), 16.dpToPx(ctx), 20.dpToPx(ctx))
    }

    val topHeader = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            setMargins(0, 0, 0, 12.dpToPx(ctx))
        }
    }

    val adBadgeDrawable = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#1C1E24"))
        setStroke(1.dpToPx(ctx), AndroidColor.parseColor("#DFFF00"))
        cornerRadius = 6.dpToPx(ctx).toFloat()
    }
    val adBadgeView = TextView(ctx).apply {
        text = "Sponsored"
        setTextColor(AndroidColor.parseColor("#DFFF00"))
        textSize = 10f
        setTypeface(null, Typeface.BOLD)
        background = adBadgeDrawable
        setPadding(8.dpToPx(ctx), 3.dpToPx(ctx), 8.dpToPx(ctx), 3.dpToPx(ctx))
    }
    topHeader.addView(adBadgeView)

    val advertiserView = TextView(ctx).apply {
        text = nativeAd.advertiser ?: "Featured Partner"
        setTextColor(AndroidColor.parseColor("#94A3B8"))
        textSize = 11f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        setPadding(10.dpToPx(ctx), 0, 0, 0)
    }
    topHeader.addView(advertiserView)
    rootLayout.addView(topHeader)

    val mediaContainer = FrameLayout(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f,
        ).apply {
            setMargins(0, 0, 0, 14.dpToPx(ctx))
        }
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
    mediaContainer.addView(mediaView)
    rootLayout.addView(mediaContainer)

    val bottomLayout = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
    }

    val infoRow = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            setMargins(0, 0, 0, 12.dpToPx(ctx))
        }
    }

    val iconView = ImageView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(48.dpToPx(ctx), 48.dpToPx(ctx)).apply {
            setMargins(0, 0, 12.dpToPx(ctx), 0)
        }
    }
    infoRow.addView(iconView)
    nativeView.iconView = iconView

    val textCol = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }
    val headlineView = TextView(ctx).apply {
        text = nativeAd.headline ?: "Recommended for you"
        setTextColor(AndroidColor.WHITE)
        textSize = 14f
        setTypeface(null, Typeface.BOLD)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    textCol.addView(headlineView)
    nativeView.headlineView = headlineView

    val bodyView = TextView(ctx).apply {
        text = nativeAd.body ?: ""
        setTextColor(AndroidColor.parseColor("#94A3B8"))
        textSize = 11f
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
    }
    textCol.addView(bodyView)
    nativeView.bodyView = bodyView
    infoRow.addView(textCol)
    bottomLayout.addView(infoRow)

    val ctaBackground = GradientDrawable().apply {
        setColor(AndroidColor.parseColor("#DFFF00"))
        cornerRadius = 12.dpToPx(ctx).toFloat()
    }
    val ctaView = TextView(ctx).apply {
        text = nativeAd.callToAction ?: "Learn More"
        setTextColor(AndroidColor.parseColor("#0A0B0E"))
        textSize = 13f
        setTypeface(null, Typeface.BOLD)
        background = ctaBackground
        gravity = Gravity.CENTER
        setPadding(0, 10.dpToPx(ctx), 0, 10.dpToPx(ctx))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
    }
    bottomLayout.addView(ctaView)
    nativeView.callToActionView = ctaView

    rootLayout.addView(bottomLayout)
    populateIcon(iconView, nativeAd)

    nativeView.addView(rootLayout)
    nativeView.registerNativeAd(nativeAd, mediaView)
}
