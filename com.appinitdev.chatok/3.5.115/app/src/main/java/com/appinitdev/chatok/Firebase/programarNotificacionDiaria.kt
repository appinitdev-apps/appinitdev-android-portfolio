fun programarNotificacionDiaria(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(
        1, TimeUnit.DAYS
    )
        .setInitialDelay(delayHastaHora(10), TimeUnit.MILLISECONDS).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_notification_work",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}
fun delayHastaHora(hora: Int): Long {
    val ahora = Calendar.getInstance()
    val objetivo = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        if (before(ahora)) add(Calendar.DAY_OF_YEAR, 1)
    }
    return objetivo.timeInMillis - ahora.timeInMillis
}
fun solicitarPermisoNotificaciones(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }
    }
}
