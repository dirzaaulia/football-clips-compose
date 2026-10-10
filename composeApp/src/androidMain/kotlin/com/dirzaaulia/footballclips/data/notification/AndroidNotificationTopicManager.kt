package com.dirzaaulia.footballclips.data.notification

import com.google.firebase.messaging.FirebaseMessaging

class AndroidNotificationTopicManager : NotificationTopicManager {
    private val subscribedClubs = mutableSetOf<String>()

    override fun subscribeClubTopic(clubId: String) {
        if (clubId.isNotBlank()) {
            FirebaseMessaging.getInstance().subscribeToTopic("club_$clubId")
            subscribedClubs.add(clubId)
        }
    }

    override fun unsubscribeClubTopic(clubId: String) {
        if (clubId.isNotBlank()) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("club_$clubId")
            subscribedClubs.remove(clubId)
        }
    }

    override fun syncClubSubscriptions(savedClubIds: Set<String>) {
        val toRemove = subscribedClubs - savedClubIds
        toRemove.forEach { unsubscribeClubTopic(it) }

        val toAdd = savedClubIds - subscribedClubs
        toAdd.forEach { subscribeClubTopic(it) }
    }
}
