class CleanTrashWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
        return try {
            val db = NotallyDatabase.getDatabase(context.applicationContext as ContextWrapper, observePreferences = false).value
            val cutoffTime = System.currentTimeMillis() - (THIRTY_DAYS_MS)
            runBlocking {
                db.getBaseNoteDao().deleteOldDeletedNotes(cutoffTime)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    companion object {
        const val CLEAN_TRASH_WORK_NAME = "CleanTrashWorker"
        private const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000L
        fun scheduleAutoTrashClean(context: Context) {
            val constraints = Constraints.Builder().build()
            val cleanTrashRequest = PeriodicWorkRequestBuilder<CleanTrashWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                CLEAN_TRASH_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cleanTrashRequest
            )
        }
    }
}
