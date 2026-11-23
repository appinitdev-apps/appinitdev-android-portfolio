
class DailyNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        mostrarNotificacion()
        return Result.success()
    }

    private fun mostrarNotificacion() {
        val channelId = "daily_channel_v2"
        val notificationId = 1001

        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal obligatorio Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones diarias",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = applicationContext.getString(R.string.recordatorios_diarios_de_la_app)
                enableVibration(true) // <-- AGREGA ESTO
                vibrationPattern = longArrayOf(0, 100, 50, 100, 50, 100) // <-- AGREGA ESTO
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = android.graphics.BitmapFactory.decodeResource(applicationContext.resources, R.mipmap.ic_launcher)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setContentTitle(applicationContext.getString(R.string.hoy_puedes_ganar_puntos))
            .setContentText(applicationContext.getString(R.string.sigue_probando_apps_y_sube_tu_reputaci_n))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Importante para que vibre/suene
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 100, 50, 100, 50, 100))
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }
}
