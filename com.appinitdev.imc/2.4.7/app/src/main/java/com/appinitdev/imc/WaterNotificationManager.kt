
class WaterNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "water_reminders_channel"
        const val CHANNEL_NAME = "Recordatorios de Hidratación"
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
                description = "Recordatorios automáticos periódicos para beber agua y cumplir tu meta diaria."
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleWaterReminder(intervalHours: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = intervalHours * 60L * 60L * 1000L
        val triggerTime = System.currentTimeMillis() + intervalMillis

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                intervalMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelWaterReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, WaterNotificationManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notif_titulo_agua))
            .setContentText(context.getString(R.string.notif_texto_agua))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            notificationManager.notify(9999, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
