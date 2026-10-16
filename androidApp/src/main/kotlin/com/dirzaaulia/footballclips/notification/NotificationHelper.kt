package com.dirzaaulia.footballclips.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dirzaaulia.footballclips.MainActivity
import com.dirzaaulia.footballclips.R

object NotificationHelper {
    const val CHANNEL_LIVE_SCORES = "fc_channel_live_scores"
    const val CHANNEL_HIGHLIGHTS = "fc_channel_highlights"
    const val CHANNEL_REMINDERS = "fc_channel_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val scoreChannel = NotificationChannel(
                CHANNEL_LIVE_SCORES,
                "Live Scores & Match Events",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time kickoff, live score, and full-time alerts for your saved clubs."
                enableVibration(true)
            }

            val highlightChannel = NotificationChannel(
                CHANNEL_HIGHLIGHTS,
                "Match Video Highlights",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Immediate notifications when official YouTube highlights are ready to watch."
                enableVibration(true)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Upcoming Match Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Countdown alerts 30 minutes before your club kicks off."
            }

            manager.createNotificationChannel(scoreChannel)
            manager.createNotificationChannel(highlightChannel)
            manager.createNotificationChannel(reminderChannel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        channelId: String,
        matchId: String = "",
        videoId: String = "",
        eventType: String = "score"
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_MATCH_ID", matchId)
            putExtra("EXTRA_VIDEO_ID", videoId)
            putExtra("EXTRA_EVENT_TYPE", eventType)
            if (matchId.isNotBlank()) {
                data = Uri.parse("drzfc://match/$matchId")
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIconBitmap = try {
            android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        } catch (_: Throwable) {
            null
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .apply {
                if (largeIconBitmap != null) {
                    setLargeIcon(largeIconBitmap)
                }
            }
            .setColor(androidx.core.content.ContextCompat.getColor(context, R.color.notification_brand))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = (matchId.hashCode() + eventType.hashCode() + (System.currentTimeMillis() % 1000).toInt()).let { if (it < 0) -it else it }
            notificationManager.notify(notificationId, builder.build())
            android.util.Log.d("NotificationHelper", "Notification posted successfully! id=$notificationId, channel=$channelId")
        } catch (t: Throwable) {
            android.util.Log.e("NotificationHelper", "Failed to post notification: ${t.message}", t)
        }
    }
}


