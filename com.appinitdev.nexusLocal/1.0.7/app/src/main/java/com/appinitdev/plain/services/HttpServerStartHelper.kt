
 


object HttpServerStartHelper {

    suspend fun startServer(service: HttpServerService, onStateChanged: (HttpServerState) -> Unit) {
        LogCat.d("startHttpServer")
        onStateChanged(HttpServerState.STARTING)
        sendEvent(HttpServerStateChangedEvent(HttpServerState.STARTING))

        HttpServerManager.portsInUse.clear()
        HttpServerManager.httpServerError = ""

        HttpServerManager.stopPreviousServer()
        if (PortHelper.isPortInUse(TempData.httpPort.value) || PortHelper.isPortInUse(TempData.httpsPort.value)) {
            LogCat.d("Ports still in use after stopping previous server, waiting...")
            HttpServerManager.waitForPortsAvailable(TempData.httpPort.value, TempData.httpsPort.value)
            attemptServerStart(1)
        } else {
            attemptServerStart(2)
        }

        if (HttpServerManager.server != null) {
            PeerStatusManager.start()
        }
        val serverUp = HttpServerManager.checkServerAsync()
        if (serverUp) {
            handleSuccess(service, onStateChanged)
        } else {
            handleFailure(service, onStateChanged)
        }
    }

    private suspend fun attemptServerStart(maxRetries: Int) {
        for (attempt in 1..maxRetries) {
            var newServer: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
            try {
                newServer = HttpServerManager.createHttpServerAsync(MainApp.instance)
                newServer.start(wait = false)
                HttpServerManager.server = newServer
                break
            } catch (ex: Exception) {
                
                
                try { newServer?.stop(0, 0) } catch (_: Exception) {}
                LogCat.e(ex, "Server start attempt $attempt/$maxRetries failed: ${ex.message}")
                if (ex is java.net.BindException || ex.cause is java.net.BindException) {
                    if (attempt < maxRetries) {
                        HttpServerManager.stopPreviousServer()
                        HttpServerManager.waitForPortsAvailable(
                            TempData.httpPort.value, TempData.httpsPort.value, maxWaitMs = 3000,
                        )
                    }
                } else {
                    break
                }
            }
        }
    }

    private suspend fun handleSuccess(
        service: HttpServerService, onStateChanged: (HttpServerState) -> Unit,
    ) {
        HttpServerManager.httpServerError = ""
        HttpServerManager.portsInUse.clear()
        NsdHelper.registerServices(service, httpPort = TempData.httpPort.value, httpsPort = TempData.httpsPort.value)
        onStateChanged(HttpServerState.ON)
        sendEvent(HttpServerStateChangedEvent(HttpServerState.ON))
        PNotificationListenerService.toggle(service, Permission.NOTIFICATION_LISTENER.isEnabledAsync(service))
    }

    private fun handleFailure(
        service: HttpServerService, onStateChanged: (HttpServerState) -> Unit,
    ) {
        val serverWasRunning = HttpServerManager.server != null

        
        
        
        HttpServerManager.stopPreviousServer()

        if (!serverWasRunning) {
            
            if (PortHelper.isPortInUse(TempData.httpPort.value)) HttpServerManager.portsInUse.add(TempData.httpPort.value)
            if (PortHelper.isPortInUse(TempData.httpsPort.value)) HttpServerManager.portsInUse.add(TempData.httpsPort.value)
        }
        HttpServerManager.httpServerError = when {
            HttpServerManager.portsInUse.isNotEmpty() -> LocaleHelper.getStringF(
                if (HttpServerManager.portsInUse.size > 1) Res.string.http_port_conflict_errors
                else Res.string.http_port_conflict_error,
                "port", HttpServerManager.portsInUse.joinToString(", "),
            )
            serverWasRunning -> LocaleHelper.getString(Res.string.http_server_health_check_failed)
            HttpServerManager.httpServerError.isNotEmpty() ->
                LocaleHelper.getString(Res.string.http_server_failed) + " (${HttpServerManager.httpServerError})"
            else -> LocaleHelper.getString(Res.string.http_server_failed)
        }
        onStateChanged(HttpServerState.ERROR)
        sendEvent(HttpServerStateChangedEvent(HttpServerState.ERROR))
        PNotificationListenerService.toggle(service, false)
    }
}
