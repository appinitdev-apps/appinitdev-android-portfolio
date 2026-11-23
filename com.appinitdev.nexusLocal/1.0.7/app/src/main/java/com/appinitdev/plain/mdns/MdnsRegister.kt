
 



class MdnsRegister(
    context: Context,
    private val isActive: () -> Boolean,
    private val hostnameProvider: () -> String,
    private val httpPortProvider: () -> Int,
    private val httpsPortProvider: () -> Int,
) {
    private val appContext: Context = context.applicationContext

    private var reregisterJob: Job? = null
    private var hotspotWatcher: MdnsHotspotWatcher? = null

     
    @Volatile
    private var lastRegisteredIfaces: Set<String> = emptySet()

    fun start() {
        hotspotWatcher = MdnsHotspotWatcher(appContext) { schedule("hotspotStateChanged") }.also { it.start() }
    }

    fun stop() {
        reregisterJob?.cancel()
        reregisterJob = null
        lastRegisteredIfaces = emptySet()

        hotspotWatcher?.stop()
        hotspotWatcher = null
    }

    fun schedule(reason: String) {
        if (!isActive()) return

        
        
        
        
        val currentIfaces = candidateInterfaces()
            .map { (iface, ip) -> "${iface.name}:${ip.hostAddress}" }
            .toSet()
        if (currentIfaces.isNotEmpty() && currentIfaces == lastRegisteredIfaces && reregisterJob?.isActive == true) {
            return
        }

        reregisterJob?.cancel()
        reregisterJob = coIO {
            delay(2000) 
            if (!isActive()) return@coIO

            
            val freshIfaces = candidateInterfaces()
                .map { (iface, ip) -> "${iface.name}:${ip.hostAddress}" }
                .toSet()

            
            
            if (freshIfaces.isEmpty()) {
                if (lastRegisteredIfaces.isNotEmpty()) {
                    LogCat.d("mDNS teardown ($reason): no interfaces, clearing registration state")
                    NsdHelper.unregisterService()
                    lastRegisteredIfaces = emptySet()
                }
                return@coIO
            }

            if (freshIfaces == lastRegisteredIfaces) return@coIO

            val hostname = hostnameProvider().trim()
            val httpPort = httpPortProvider()
            val httpsPort = httpsPortProvider()
            val httpOk = httpPort in 1..65535
            val httpsOk = httpsPort in 1..65535
            if (hostname.isEmpty() || (!httpOk && !httpsOk)) return@coIO

            LogCat.d("mDNS re-register ($reason): $freshIfaces")
            runCatching {
                NsdHelper.registerServices(
                    context = appContext,
                    httpPort = if (httpOk) httpPort else null,
                    httpsPort = if (httpsOk) httpsPort else null,
                )
            }
                .onSuccess { ok ->
                    if (ok) {
                        lastRegisteredIfaces = freshIfaces
                    } else {
                        LogCat.e("mDNS re-register returned false, resetting registration state")
                        lastRegisteredIfaces = emptySet()
                    }
                }
                .onFailure {
                    LogCat.e("mDNS re-register failed: ${it.message}")
                    lastRegisteredIfaces = emptySet()
                }
        }
    }
}
