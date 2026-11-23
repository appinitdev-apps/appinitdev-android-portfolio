
class AndroidNotificationService(private val context: Context) : NotificationService {
    // Helper to schedule ANC notifications
    override fun scheduleANCNotifications(visits: List<PregnancyEngine.ANCVisit>) {
        ANCNotificationManager.scheduleANCNotifications(context, visits)
        
        NotificationScheduler.scheduleNotification(
            context,
            context.getString(R.string.notif_sync_active_title),
            context.getString(R.string.notif_sync_active_desc, visits.size),
            500,
            101,
        )

        val currentTime = System.currentTimeMillis()
        val nextVisit = visits.firstOrNull { 
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val date = sdf.parse(it.date)
                date != null && date.time > currentTime
            } catch (_: Exception) { false }
        }

        if (nextVisit != null) {
            // Resolve the KMP StringResource to a real string for the notification
            val purposeText = ResourceUtils.getString(nextVisit.purpose)
            
            NotificationScheduler.scheduleNotification(
                context,
                context.getString(R.string.notif_upcoming_visit_title, nextVisit.week),
                context.getString(R.string.notif_upcoming_visit_desc, nextVisit.date, purposeText),
                2500,
                102
            )
        }

        NotificationScheduler.scheduleNotification(
            context,
            context.getString(R.string.notif_welcome_title),
            context.getString(R.string.notif_welcome_desc),
            4500,
            103
        )
    }

    override fun scheduleRiskFollowUp() {
        val fourHoursMillis: Long = 4 * 60 * 60 * 1000 
        NotificationScheduler.scheduleNotification(
            context,
            context.getString(R.string.notif_health_checkin_title),
            context.getString(R.string.notif_health_checkin_desc),
            fourHoursMillis,
            201 
        )

        NotificationScheduler.scheduleNotification(
            context,
            context.getString(R.string.notif_risk_followup_title),
            context.getString(R.string.notif_risk_followup_desc),
            3000,
            202
        )
    }
}
