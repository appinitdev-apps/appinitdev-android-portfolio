class AutoBackupWorker(private val context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
        return (context.applicationContext as ContextWrapper).createBackup()
    }
}
