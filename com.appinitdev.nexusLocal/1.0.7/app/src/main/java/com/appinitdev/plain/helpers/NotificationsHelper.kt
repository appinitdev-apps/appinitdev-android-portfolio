object NotificationsHelper {
    suspend fun filterNotificationsAsync(context: Context): List<DNotification> {
        val filterData = NotificationFilterPreference.getValueAsync()
        val filteredNotifications = mutableListOf<DNotification>()
        for (notification in AndroidTempData.notifications) {
            val isAllowed = when (filterData.mode) {
                "allowlist" -> filterData.apps.contains(notification.appId)
                "blacklist" -> !filterData.apps.contains(notification.appId)
                else -> true
            }
            if (isAllowed) {
                filteredNotifications.add(notification)
            }
        }
        return filteredNotifications
    }
}