package com.dirzaaulia.footballclips.notification

import com.dirzaaulia.footballclips.data.local.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FootballClipsFirebaseMessagingService : FirebaseMessagingService() {

    private val preferenceManager: PreferenceManager by inject()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = remoteMessage.notification?.title ?: data["title"] ?: "Football Highlights & Clips"
        val body = remoteMessage.notification?.body ?: data["body"] ?: ""
        val eventType = data["event_type"] ?: "score"
        val matchId = data["match_id"] ?: ""
        val videoId = data["video_id"] ?: ""
        val homeTeam = data["home_team"] ?: ""
        val awayTeam = data["away_team"] ?: ""
        val homeId = data["home_id"] ?: ""
        val awayId = data["away_id"] ?: ""
        val score = data["score"] ?: ""

        scope.launch {
            val notifyGoals = preferenceManager.notifyLiveGoals.first()
            val notifyKickoff = preferenceManager.notifyKickoffWhistle.first()
            val notifyHighlights = preferenceManager.notifyHighlights.first()
            val favoriteClubs = preferenceManager.favoriteClubs.first()

            val shouldPostSystemNotification = when (eventType) {
                "score" -> notifyGoals
                "kickoff", "fulltime", "upcoming" -> notifyKickoff
                "highlight" -> notifyHighlights
                else -> true
            }

            if (shouldPostSystemNotification) {
                val channelId = when (eventType) {
                    "highlight" -> NotificationHelper.CHANNEL_HIGHLIGHTS
                    "upcoming" -> NotificationHelper.CHANNEL_REMINDERS
                    else -> NotificationHelper.CHANNEL_LIVE_SCORES
                }

                NotificationHelper.showNotification(
                    context = this@FootballClipsFirebaseMessagingService,
                    title = title,
                    body = body,
                    channelId = channelId,
                    matchId = matchId,
                    videoId = videoId,
                    eventType = eventType,
                )
            }

            // In-app Goal celebration banner: STRICTLY for user's favorite "My Clubs" only!
            val isFavoriteClubMatch = (homeId.isNotBlank() && favoriteClubs.contains(homeId)) ||
                (awayId.isNotBlank() && favoriteClubs.contains(awayId))

            if (eventType == "score" && isFavoriteClubMatch) {
                InAppNotificationBus.postGoalEvent(
                    InAppGoalEvent(
                        matchId = matchId,
                        title = title,
                        body = body,
                        homeTeam = homeTeam,
                        awayTeam = awayTeam,
                        homeId = homeId,
                        awayId = awayId,
                        score = score,
                        timestamp = System.currentTimeMillis(),
                    ),
                )
            }
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            try {
                val favoriteClubs = preferenceManager.favoriteClubs.first()
                for (cid in favoriteClubs) {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("club_$cid")
                }
            } catch (_: Exception) {
            }
        }
    }
}
