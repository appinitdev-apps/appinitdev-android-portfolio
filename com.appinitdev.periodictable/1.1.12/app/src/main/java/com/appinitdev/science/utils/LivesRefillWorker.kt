class LivesRefillWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        try {
            val pre = LivesManager.getLives(applicationContext)
            val refilled = LivesManager.refillLivesIfNeeded(applicationContext)
            val post = LivesManager.getLives(applicationContext)
            val maxLives = LivesManager.getMaxLives(applicationContext)
            val prefs = applicationContext.getSharedPreferences("lives_notifications_prefs", Context.MODE_PRIVATE)
            val wasUnder10 = prefs.getBoolean("was_under_10", false)
            if (pre < 10) {
                prefs.edit().putBoolean("was_under_10", true).apply()
            }
            if (refilled && wasUnder10 && post >= maxLives) {
                NotificationHelper.sendLivesRefilledNotification(applicationContext)
                prefs.edit().putBoolean("was_under_10", false).apply()
            }
            LivesManager.ensureRefillWorkerScheduled(applicationContext)
            return Result.success()
        } catch (t: Throwable) {
            t.printStackTrace()
            return Result.retry()
        }
    }
}