class DailyNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val title = applicationContext.getString(R.string.titulo)
        val body = applicationContext.getString(R.string.mensajeCuerpo)
        mostrarNotificacion(title, body)
        saveNotificationLocally(title, body)
        return Result.success()
    }
    private fun saveNotificationLocally(title: String, message: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val entity = NotificationEntity(
            from = title,
            message = message,
            time = System.currentTimeMillis(),
            userId = currentUser.uid
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                BaseApplication.database.chatDao().insertNotification(entity)
            } catch (e: Exception) {
                Log.e("DailyNotificationWorker", "Error saving notification", e)
            }
        }
    }
    private fun mostrarNotificacion(title: String, body: String) {
        val channelId = "daily_channel_v2"
        val notificationId = 1001
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones diarias",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = applicationContext.getString(R.string.recordatorios_diarios_de_la_app)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50, 100, 50, 100)
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
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification) 
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 100, 50, 100, 50, 100))
            .setAutoCancel(true)
            .build()
        manager.notify(notificationId, notification)
    }
}
