package com.dirzaaulia.footballclips.data.notification

interface NotificationTopicManager {
    fun subscribeClubTopic(clubId: String)
    fun unsubscribeClubTopic(clubId: String)
    fun syncClubSubscriptions(savedClubIds: Set<String>)
}

class NoOpNotificationTopicManager : NotificationTopicManager {
    override fun subscribeClubTopic(clubId: String) = Unit
    override fun unsubscribeClubTopic(clubId: String) = Unit
    override fun syncClubSubscriptions(savedClubIds: Set<String>) = Unit
}
