package com.dirzaaulia.footballclips.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dirzaaulia.footballclips.notification.NotificationHelper

class DebugNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Live Score: Arsenal vs Chelsea"
        val body = intent.getStringExtra("body") ?: "GOAL! Bukayo Saka scores 23'. Arsenal 1 - 0 Chelsea"
        val eventType = intent.getStringExtra("event_type") ?: "score"
        val matchId = intent.getStringExtra("match_id") ?: "test_12345"
        val videoId = intent.getStringExtra("video_id") ?: ""

        val channelId = when (eventType) {
            "highlight" -> NotificationHelper.CHANNEL_HIGHLIGHTS
            "upcoming" -> NotificationHelper.CHANNEL_REMINDERS
            else -> NotificationHelper.CHANNEL_LIVE_SCORES
        }

        Log.d("DebugNotificationReceiver", "Triggering debug notification: $title - $body (channel: $channelId)")
        NotificationHelper.showNotification(
            context = context,
            title = title,
            body = body,
            channelId = channelId,
            matchId = matchId,
            videoId = videoId,
            eventType = eventType
        )
    }
}
