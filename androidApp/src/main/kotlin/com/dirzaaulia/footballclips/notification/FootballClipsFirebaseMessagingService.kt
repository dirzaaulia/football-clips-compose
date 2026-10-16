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
        val title = remoteMessage.notification?.title ?: data["title"] ?: "Football Highlights & Clips"
        val body = remoteMessage.notification?.body ?: data["body"] ?: ""
        val eventType = data["event_type"] ?: "score"
        val matchId = data["match_id"] ?: ""
        val videoId = data["video_id"] ?: ""

        val channelId = when (eventType) {
            "highlight" -> NotificationHelper.CHANNEL_HIGHLIGHTS
            "upcoming" -> NotificationHelper.CHANNEL_REMINDERS
            else -> NotificationHelper.CHANNEL_LIVE_SCORES
        }

        NotificationHelper.showNotification(
            context = this,
            title = title,
            body = body,
            channelId = channelId,
            matchId = matchId,
            videoId = videoId,
            eventType = eventType
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}

