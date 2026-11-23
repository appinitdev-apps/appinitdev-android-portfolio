
 
object DlnaRenderer {

     
    val deviceUuid: String by lazy { UUID.randomUUID().toString() }

    private val CANDIDATE_PORTS = listOf(7878, 7879, 7880)
    private var lastPort: Int? = null
    private var scope: CoroutineScope? = null
     
    private var activeServerSocket: ServerSocket? = null

    fun start(context: Context) {
        if (DlnaRendererState.isRunning.value) return
        DlnaRendererState.startError.value = ""
        val serverSocket = openServerSocket()
        if (serverSocket == null) {
            val msg = "Failed to bind on ports ${CANDIDATE_PORTS.joinToString()}"
            LogCat.e("DlnaRenderer: $msg")
            DlnaRendererState.startError.value = msg
            return
        }
        val port = serverSocket.localPort
        lastPort = port
        activeServerSocket = serverSocket
        DlnaRendererState.port.value = port
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope!!.launch {
            try {
                launch { DlnaHttpServer.run(serverSocket) }
                launch { DlnaSsdpAdvertiser.run(context) }
            } catch (e: Exception) {
                LogCat.e("DlnaRenderer startup error: ${e.message}")
                DlnaRendererState.isRunning.value = false
            }
        }
        DlnaRendererState.isRunning.value = true
        LogCat.d("DlnaRenderer started on port $port uuid=$deviceUuid")
    }

    fun stop() {
        
        
        activeServerSocket?.close()
        activeServerSocket = null
        scope?.cancel()
        scope = null
        DlnaRendererState.isRunning.value = false
        DlnaRendererState.reset()
        LogCat.d("DlnaRenderer stopped")
    }

     




    private fun openServerSocket(): ServerSocket? {
        val candidates = lastPort
            ?.let { listOf(it) + CANDIDATE_PORTS.filter { p -> p != it } }
            ?: CANDIDATE_PORTS
        for (port in candidates) {
            try {
                val ss = ServerSocket()
                ss.reuseAddress = true
                ss.bind(java.net.InetSocketAddress(port))
                return ss
            } catch (_: Exception) {
                LogCat.d("DlnaRenderer: port $port unavailable, trying next")
            }
        }
        return null
    }
}
