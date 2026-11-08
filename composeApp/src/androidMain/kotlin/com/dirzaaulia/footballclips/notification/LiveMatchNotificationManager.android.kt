package com.dirzaaulia.footballclips.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class LiveMatchNotificationManager(
    private val context: Context,
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "live_matches_channel"
        const val CHANNEL_NAME = "Live Match Ticker"
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Real-time match scores and key play tickers"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showOrUpdateLiveMatch(
        matchId: Long,
        homeTeam: String,
        awayTeam: String,
        homeScore: Int,
        awayScore: Int,
        clock: String,
        eventText: String? = null,
        isFinished: Boolean = false,
    ) {
        val title = "$homeTeam $homeScore - $awayScore $awayTeam"
        val subtitle = if (isFinished) {
            "Full Time • Final Score"
        } else {
            val playSuffix = if (!eventText.isNullOrBlank()) " • $eventText" else ""
            "🔴 $clock$playSuffix"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setOngoing(!isFinished)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(isFinished)
            .build()

        notificationManager.notify(matchId.toInt(), notification)
    }

    fun dismissLiveMatch(matchId: Long) {
        notificationManager.cancel(matchId.toInt())
    }
}
