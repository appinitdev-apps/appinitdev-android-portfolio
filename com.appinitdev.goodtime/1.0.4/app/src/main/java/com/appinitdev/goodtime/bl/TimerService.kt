class TimerService :
    Service(),
    KoinComponent {
    private val notificationManager: NotificationArchManager by inject()
    private val timerManager: TimerManager by inject()
    private val log: Logger by injectLogger("TimerService")
    override fun onBind(intent: Intent?) = null
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent == null || intent.action == null) {
            return START_NOT_STICKY
        }
        val data = timerManager.timerData.value
        when (intent.action) {
            Action.StartOrUpdate.name -> {
                notificationManager.clearFinishedNotification()
                startForeground(
                    NotificationArchManager.IN_PROGRESS_NOTIFICATION_ID,
                    notificationManager.buildInProgressNotification(data),
                )
            }
            Action.Reset.name -> {
                notificationManager.clearFinishedNotification()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            Action.Finished.name -> {
                val autoStart = intent.getBooleanExtra(EXTRA_FINISHED_AUTOSTART, false)
                if (!autoStart) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                notificationManager.notifyFinished(data, withActions = !autoStart)
                return START_NOT_STICKY
            }
            Action.Toggle.name -> timerManager.toggle()
            Action.AddOneMinute.name -> timerManager.addOneMinute()
            Action.Skip.name -> timerManager.next(finishActionType = FinishActionType.MANUAL_SKIP)
            Action.Next.name -> timerManager.next(finishActionType = FinishActionType.MANUAL_NEXT)
            Action.DoReset.name -> timerManager.reset()
        }
        return START_STICKY
    }
    companion object {
        enum class Action {
            StartOrUpdate,
            Reset,
            Finished,
            Toggle,
            AddOneMinute,
            Skip,
            Next,
            DoReset,
        }
        private const val EXTRA_FINISHED_AUTOSTART = "EXTRA_FINISHED_AUTOSTART"
        fun createIntentWithAction(
            context: Context,
            action: Action,
        ): Intent = Intent(context, TimerService::class.java).setAction(action.name)
        fun createFinishEvent(
            context: Context,
            autostart: Boolean = false,
        ): Intent =
            Intent(context, TimerService::class.java).apply {
                action = Action.Finished.name
                putExtra(EXTRA_FINISHED_AUTOSTART, autostart)
            }
    }
}
