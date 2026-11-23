class HttpServerService : LifecycleService() {
    private var serverState: HttpServerState = HttpServerState.OFF
    var mdnsRegister: MdnsRegister? = null
    private var serverJob: Job? = null
    private var lockManager: HttpServerLockManager? = null
    private var isStickyRestart: Boolean = false
    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationHelper.ensureDefaultChannel()
        lockManager = HttpServerLockManager(this)
        mdnsRegister = MdnsRegister(
            context = this,
            isActive = { serverState == HttpServerState.ON },
            hostnameProvider = { TempData.mdnsHostname },
            httpPortProvider = { TempData.httpPort.value },
            httpsPortProvider = { TempData.httpsPort.value },
        ).also { it.start() }
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        if (serverState == HttpServerState.STARTING || serverState == HttpServerState.ON) return
                        lockManager?.start()
                        serverJob?.cancel()
                        serverJob = coIO {
                            if (isStickyRestart) {
                                LogCat.d("START_STICKY restart — waiting 5s for port release")
                                delay(5_000)
                                isStickyRestart = false
                            }
                            startHttpServerAsync()
                        }
                    }
                    Lifecycle.Event.ON_STOP -> {
                        lockManager?.stop()
                        serverJob?.cancel()
                        serverJob = coIO {
                            stopHttpServerAsync()
                        }
                    }
                    else -> Unit
                }
            }
        })
    }
    @SuppressLint("InlinedApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) isStickyRestart = true
        super.onStartCommand(intent, flags, startId)
        try {
            val notification = NotificationHelper.createServiceNotification(
                this,
                Constants.ACTION_STOP_HTTP_SERVER,
                LocaleHelper.getString(Res.string.api_service_is_running),
                HttpServerManager.getNotificationContent()
            )
            try {
                ServiceCompat.startForeground(
                    this,
                    HttpServerManager.notificationId,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } catch (e: Exception) {
                LogCat.e("Error starting foreground service with specialUse: ${e.message}")
                try {
                    ServiceCompat.startForeground(
                        this,
                        HttpServerManager.notificationId,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                    )
                } catch (e2: Exception) {
                    LogCat.e("Error starting foreground service with dataSync: ${e2.message}")
                    startForeground(HttpServerManager.notificationId, notification)
                }
            }
        } catch (e: Exception) {
            LogCat.e("Failed to start foreground service: ${e.message}")
            e.printStackTrace()
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }
    private suspend fun startHttpServerAsync() {
        HttpServerStartHelper.startServer(this) { serverState = it }
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        LogCat.d("App task removed — keeping HttpServerService alive")
    }
    override fun onDestroy() {
        instance = null
        serverJob?.cancel()
        serverJob = null
        super.onDestroy()
        lockManager?.stop()
        lockManager = null
        mdnsRegister?.stop()
        mdnsRegister = null
        NsdHelper.unregisterService()
        PeerStatusManager.stop()
        try {
            HttpServerManager.server?.stop(0, 1000)
        } catch (_: Exception) {
        }
        HttpServerManager.server = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
    private suspend fun stopHttpServerAsync() {
        LogCat.d("stopHttpServer")
        try {
            NsdHelper.unregisterService()
            val client = HttpClientManager.httpClient()
            val r = client.get(UrlHelper.getShutdownUrl())
            if (r.status == HttpStatusCode.Gone) {
                LogCat.d("http server is stopped")
            }
        } catch (ex: Exception) {
            LogCat.e("Graceful shutdown failed: ${ex.message}")
            try {
                HttpServerManager.server?.stop(500, 1000)
                LogCat.d("Server force-stopped via stored reference")
            } catch (e: Exception) {
                LogCat.e("Force stop also failed: ${e.message}")
            }
        }
        HttpServerManager.server = null
        PeerStatusManager.stop()
        PNotificationListenerService.toggle(this, false)
        serverState = HttpServerState.OFF
    }
    companion object {
        @Volatile
        var instance: HttpServerService? = null
        fun isRunning(): Boolean = instance != null
    }
}
