class PediatricNotificationManager(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "pediatric_reminders_channel"
        const val CHANNEL_NAME = "Recordatorios de Control Pediátrico"
    }
    init {
        createNotificationChannel()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones automáticas para seguimiento mensual y periódico de peso y talla."
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    fun schedulePediatricReminder(memberName: String, daysInFuture: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PediatricReminderReceiver::class.java).apply {
            putExtra("memberName", memberName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            memberName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerTime = System.currentTimeMillis() + (daysInFuture * 24L * 60L * 60L * 1000L)
        try {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
class PediatricReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val memberName = intent.getStringExtra("memberName") ?: "tu familiar"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, PediatricNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notif_titulo_pediatrico))
            .setContentText(context.getString(R.string.notif_texto_pediatrico, memberName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        try {
            notificationManager.notify(memberName.hashCode(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
