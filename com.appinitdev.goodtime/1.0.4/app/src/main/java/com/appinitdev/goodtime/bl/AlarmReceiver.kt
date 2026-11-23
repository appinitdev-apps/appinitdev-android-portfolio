
class AlarmReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val timerManager: TimerManager by inject()

    @Suppress("DEPRECATION")
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager
            .newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.ON_AFTER_RELEASE
                    or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                "Goodtime:AlarmReceiver",
            ).apply {
                acquire(10.seconds.inWholeMilliseconds)
            }
        val isCountdown = timerManager.timerData.value.isCurrentSessionCountdown()
        if (isCountdown) {
            timerManager.finish()
        } else {
            // hard limit was reached for count-up
            timerManager.reset()
        }
    }
}
