
object WearableSyncManager {

    const val ACTION_WEAR_TIMER_UPDATE = "com.apps.adrcotfas.goodtime.WEAR_TIMER_UPDATE"
    const val EXTRA_STATE = "extra_state"
    const val EXTRA_LABEL = "extra_label"
    const val EXTRA_REMAINING_SEC = "extra_remaining_sec"
    const val EXTRA_TIMER_TYPE = "extra_timer_type"

    fun broadcastTimerUpdate(
        context: Context,
        data: DomainTimerData,
        remainingSeconds: Long
    ) {
        val intent = Intent(ACTION_WEAR_TIMER_UPDATE).apply {
            putExtra(EXTRA_STATE, data.state.name)
            putExtra(EXTRA_LABEL, data.getLabelName())
            putExtra(EXTRA_REMAINING_SEC, remainingSeconds)
            putExtra(EXTRA_TIMER_TYPE, data.type.name)
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }
}
