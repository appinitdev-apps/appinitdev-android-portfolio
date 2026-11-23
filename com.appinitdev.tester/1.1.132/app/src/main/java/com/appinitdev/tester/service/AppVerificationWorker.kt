
class AppVerificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repo = AuthRepository()
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): androidx.work.ListenableWorker.Result = withContext(Dispatchers.IO) {
        try {
            val context = applicationContext
            
            
            try {
                setForeground(createForegroundInfo(context.getString(R.string.verificando_apps_y_progreso)))
            } catch (e: Exception) {
                Log.e("AppVerificationWorker", "Error setting foreground: ${e.message}")
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId.isNullOrBlank()) {
                Log.w("AppVerificationWorker", "Usuario no autenticado en ejecución de Worker")
                return@withContext androidx.work.ListenableWorker.Result.success()
            }
            
            
            val perfil = repo.getUserProfile(userId)
            if (perfil != null) {
                val maxAppsPosibles = DevAppsConfig.developerApps.size.toDouble()
                val scoreNivel = (perfil.nivel / maxAppsPosibles).coerceAtMost(1.0) * 5.0 * 0.30
                val scorePuntos = (log10(perfil.puntos.toDouble() + 1.0) / log10(501.0))
                    .coerceAtMost(1.0) * 5.0 * 0.25
                val numVotos = perfil.calificaciones.size
                val confianza = (numVotos.toDouble() / (numVotos + 5)).coerceIn(0.0, 1.0)
                val scoreEstrellas = ((perfil.promedioEstrellas * confianza) + (3.0 * (1.0 - confianza))) * 0.45
                val nuevaReputacion = (scoreNivel + scorePuntos + scoreEstrellas + perfil.puntosExtra).coerceIn(1.0, 5.0)
                
                repo.updateAllUserAppsReputation(userId, nuevaReputacion, perfil.fcmToken)
            }

            
            val apps = repo.getAppsWhereIMTesterValidated()
            if (apps.isEmpty()) {
                saveTodayCheck(context)
                return@withContext androidx.work.ListenableWorker.Result.success()
            }

            val hoy = startOfToday()
            var appsSinInstalar = 0
            
            apps.forEach { app ->
                val tester = app.testers[userId] ?: return@forEach
                if (tester.validado != "Aceptado" && app.urlGoogleGroup.isEmpty()) return@forEach

                val yaCompleto = tester.completado
                val instalada = isAppInstalled(context, app.paquete)
                
                if (!instalada && !yaCompleto) {
                    appsSinInstalar++
                }

                val ultimaRev = tester.ultimaRevision
                val diasPasados = if (ultimaRev == 0L) 1L else daysBetween(ultimaRev, hoy).toLong()

                val nuevosDias = when {
                    yaCompleto -> 14
                    !instalada -> 0
                    diasPasados <= 0L -> tester.dias
                    else -> (tester.dias + diasPasados.toInt()).coerceAtMost(14)
                }

                val acabaDeTerminar = !yaCompleto && nuevosDias >= 14
                val ganaPuntosProgreso = !yaCompleto && instalada && diasPasados > 0 && nuevosDias < 14

                val actualizaciones = mutableMapOf<String, Any>(
                    "testers.$userId.dias" to nuevosDias,
                    "testers.$userId.ultimaRevision" to hoy
                )

                if (acabaDeTerminar) {
                    actualizaciones["testers.$userId.completado"] = true
                }

                repo.updateApp(app.id, actualizaciones).onSuccess {
                    if (acabaDeTerminar) {
                        val diff = (14 - tester.dias).coerceAtLeast(0)
                        if (diff > 0) repo.incrementUserPoints(userId, diff.toLong())
                    } else if (ganaPuntosProgreso) {
                        repo.incrementUserPoints(userId, diasPasados)
                    }
                }
            }
            
            if (appsSinInstalar > 0) {
                showWarningNotification(context, appsSinInstalar)
            }
            
            saveTodayCheck(context)
            androidx.work.ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e("AppVerificationWorker", "Error: ${e.message}")
            androidx.work.ListenableWorker.Result.retry()
        }
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val channelId = "verification_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Verificación de Apps", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val largeIcon = android.graphics.BitmapFactory.decodeResource(applicationContext.resources, R.mipmap.ic_launcher)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(applicationContext.getString(R.string.optimizando_progreso))
            .setContentText(progress)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(2001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(2001, notification)
        }
    }

    private fun showWarningNotification(context: Context, count: Int) {
        val channelId = "tester_alerts_v2"
        val largeIcon = android.graphics.BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.apps_pendientes_de_instalacion))
            .setContentText(context.getString(R.string.tienes_apps_aceptadas_sin_instalar, count))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(largeIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(Random.nextInt(), notification)
    }

    private fun startOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
