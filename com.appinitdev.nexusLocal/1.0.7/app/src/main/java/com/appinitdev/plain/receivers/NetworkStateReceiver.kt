
class NetworkStateReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            
            if (Permission.POST_NOTIFICATIONS.can(context)) {
                try {
                    val notificationId = HttpServerManager.notificationId
                    val existingNotification = notificationManager.activeNotifications.find { it.id == notificationId }
                    if (existingNotification != null) {
                        NotificationManagerCompat.from(context).notify(
                            notificationId, NotificationHelper.createServiceNotification(
                                context,
                                Constants.ACTION_STOP_HTTP_SERVER,
                                LocaleHelper.getString(Res.string.api_service_is_running),
                                HttpServerManager.getNotificationContent()
                            )
                        )
                    }
                } catch (ex: Exception) {
                    LogCat.e(ex.toString())
                }
            }
        }
    }
}