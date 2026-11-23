
fun programarNotificacionDiaria(context: Context) {

    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .setRequiresCharging(false)
        .setRequiresBatteryNotLow(false)
        .build()

    val workRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(24, TimeUnit.HOURS)
        .setConstraints(constraints)
        .addTag("daily_notification_tag")
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_notification_work",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )

    // Tarea de verificación periódica cada 24 horas sin restricciones estrictas
    val verificationConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .setRequiresCharging(false)
        .setRequiresBatteryNotLow(false)
        .build()

    val verificationRequest = PeriodicWorkRequestBuilder<AppVerificationWorker>(24, TimeUnit.HOURS)
        .setConstraints(verificationConstraints)
        .addTag("app_verification_tag")
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "app_verification_work",
        ExistingPeriodicWorkPolicy.KEEP,
        verificationRequest
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


