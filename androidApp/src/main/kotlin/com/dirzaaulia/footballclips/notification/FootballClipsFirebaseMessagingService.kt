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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FootballClipsFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = remoteMessage.notification?.title ?: data["title"] ?: "Football Clips"
        val body = remoteMessage.notification?.body ?: data["body"] ?: ""
        val eventType = data["event_type"] ?: "score"
        val matchId = data["match_id"] ?: ""
        val videoId = data["video_id"] ?: ""

        val channelId = when (eventType) {
            "highlight" -> CHANNEL_HIGHLIGHTS
            "upcoming" -> CHANNEL_REMINDERS
            else -> CHANNEL_LIVE_SCORES
        }

        createNotificationChannels()
        showNotification(title, body, channelId, matchId, videoId, eventType)
    }

    @Deprecated("Deprecated in Java")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        matchId: String,
        videoId: String,
        eventType: String
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_MATCH_ID", matchId)
            putExtra("EXTRA_VIDEO_ID", videoId)
            putExtra("EXTRA_EVENT_TYPE", eventType)
            if (matchId.isNotBlank()) {
                data = Uri.parse("drzfc://match/$matchId")
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIcon = R.mipmap.ic_launcher

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (matchId.hashCode() + eventType.hashCode()).let { if (it < 0) -it else it }
        notificationManager.notify(notificationId, builder.build())
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

    companion object {
        const val CHANNEL_LIVE_SCORES = "fc_channel_live_scores"
        const val CHANNEL_HIGHLIGHTS = "fc_channel_highlights"
        const val CHANNEL_REMINDERS = "fc_channel_reminders"
    }
}
