class SessionResetHandler(
    private val context: Context,
    private val log: Logger,
) : EventListener {
    override fun onEvent(event: Event) {
        when (event) {
            is Event.Finished -> scheduleReset()
            is Event.SendToBackground, Event.BringToForeground -> {
            }
            else -> cancel()
        }
    }
    private fun scheduleReset() {
        log.d { "Resetting the session after delay" }
        val uploadWorkRequest =
            OneTimeWorkRequestBuilder<ResetWorker>()
                .setInitialDelay(30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_ID,
            ExistingWorkPolicy.REPLACE,
            uploadWorkRequest,
        )
    }
    fun cancel() {
        log.d { "Canceling the reset worker" }
        WorkManager.getInstance(context).cancelUniqueWork(WORK_ID)
    }
    companion object {
        const val WORK_ID = "resetWorker"
    }
}
class ResetWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : Worker(appContext, workerParams) {
    private val timerManager: TimerManager by inject(TimerManager::class.java)
    override fun doWork(): Result {
        timerManager.reset(actionType = FinishActionType.MANUAL_DO_NOTHING)
        return Result.success()
    }
}
