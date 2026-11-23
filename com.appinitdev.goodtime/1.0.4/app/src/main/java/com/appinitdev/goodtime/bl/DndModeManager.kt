
class DndModeManager(
    private val notificationManager: NotificationArchManager,
    settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope,
) : EventListener {
    private val isDndDuringWorkFlow =
        settingsRepository.settings.map { it.uiSettings.dndDuringWork }

    private var job: Job? = null

    private var dndEnabledBeforeStart = false
    private var wasPaused = false

    override fun onEvent(event: Event) {
        when (event) {
            is Event.Start -> {
                if (event.isFocus) {
                    if (!wasPaused) {
                        maybeEnterDndMode()
                    }
                } else { // break session
                    maybeExitDndMode()
                }
                wasPaused = false
            }

            is Event.Pause -> {
                wasPaused = true
            }

            is Event.Reset, is Event.Finished -> {
                maybeExitDndMode()
                wasPaused = false
            }

            else -> { // do nothing
            }
        }
    }

    private fun maybeEnterDndMode() {
        job?.cancel()
        job =
            coroutineScope.launch {
                val isDndDuringWork = isDndDuringWorkFlow.first()
                if (!isDndDuringWork) return@launch

                val wasDndEnabled = notificationManager.isDndModeEnabled()
                dndEnabledBeforeStart = wasDndEnabled
                if (!wasDndEnabled) {
                    withContext(Dispatchers.Main) {
                        delay(2500) // don't enter immediately to not affect ongoing sounds/vibrations
                        notificationManager.toggleDndMode(true)
                    }
                }
            }
    }

    private fun maybeExitDndMode() {
        job?.cancel()
        job =
            coroutineScope.launch {
                val isDndDuringWork = isDndDuringWorkFlow.first()
                if (!isDndDuringWork) return@launch

                if (!dndEnabledBeforeStart && notificationManager.isDndModeEnabled()) {
                    withContext(Dispatchers.Main) {
                        notificationManager.toggleDndMode(false)
                    }
                }
            }
    }
}
