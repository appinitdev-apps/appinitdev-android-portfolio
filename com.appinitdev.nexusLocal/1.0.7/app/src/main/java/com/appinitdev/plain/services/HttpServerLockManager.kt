
private const val CHECK_INTERVAL_MS = 60_000L
private const val INACTIVITY_TIMEOUT_MS = 30 * 60_000L

 










internal class HttpServerLockManager(private val context: Context) {

    private val wakeLock: PowerManager.WakeLock = powerManager.newWakeLock(
        PowerManager.PARTIAL_WAKE_LOCK,
        "${BuildConfig.APPLICATION_ID}:http_server",
    )





    @Volatile private var lastActivityMs: Long = System.currentTimeMillis()
    @Volatile private var keepAwake: Boolean = true

    private var inactivityJob: Job? = null
    private var eventJob: Job? = null

    fun start() {
        acquireLocksOnly()
        eventJob = coIO {
            keepAwake = KeepAwakePreference.getAsync()
            scheduleInactivityTimer()
            Channel.sharedFlow.collect { event ->
                when (event) {
                    is WebRequestReceivedEvent -> lastActivityMs = System.currentTimeMillis()
                    is WindowFocusChangedEvent -> if (event.hasFocus) {
                        lastActivityMs = System.currentTimeMillis()
                        acquireLocks()
                    }
                    is PowerConnectedEvent -> {
                        inactivityJob?.cancel()
                        inactivityJob = null
                        acquireLocks()
                    }
                    is PowerDisconnectedEvent -> {
                        lastActivityMs = System.currentTimeMillis()
                        scheduleInactivityTimer()
                    }
                    is KeepAwakeChangedEvent -> {
                        keepAwake = event.enabled
                        if (keepAwake) {
                            inactivityJob?.cancel()
                            inactivityJob = null
                        } else {
                            lastActivityMs = System.currentTimeMillis()
                            scheduleInactivityTimer()
                        }
                    }
                }
            }
        }
    }

    fun stop() {
        eventJob?.cancel()
        eventJob = null
        inactivityJob?.cancel()
        inactivityJob = null
        releaseLocks()
    }

    private fun acquireLocksOnly() {
        runCatching {
            if (!wakeLock.isHeld) { wakeLock.acquire(); LogCat.d("WakeLock acquired") }
        }.onFailure { LogCat.e("WakeLock acquire failed: ${it.message}") }



    }

    private fun acquireLocks() {
        acquireLocksOnly()
        scheduleInactivityTimer()
    }

    fun releaseLocks() {
        inactivityJob?.cancel()
        inactivityJob = null
        if (wakeLock.isHeld) { wakeLock.release(); LogCat.d("WakeLock released") }

    }

     





    private fun scheduleInactivityTimer() {
        if (keepAwake || PlugInControlReceiver.isUSBConnected(context)) {
            inactivityJob?.cancel()
            inactivityJob = null
            return
        }
        if (inactivityJob?.isActive == true) return
        inactivityJob = coIO {
            while (isActive) {
                delay(CHECK_INTERVAL_MS)
                if (System.currentTimeMillis() - lastActivityMs >= INACTIVITY_TIMEOUT_MS) {
                    LogCat.d("Inactivity timeout: releasing locks")
                    releaseLocks()
                    return@coIO
                }
            }
        }
    }
}
