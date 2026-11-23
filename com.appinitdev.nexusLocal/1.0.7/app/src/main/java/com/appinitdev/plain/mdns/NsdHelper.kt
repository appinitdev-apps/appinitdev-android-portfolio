
object NsdHelper {
    
    private val registering = AtomicBoolean(false)

     


    fun registerServices(context: Context, httpPort: Int?, httpsPort: Int?): Boolean {
        if (!registering.compareAndSet(false, true)) {
            LogCat.d("registerServices already in progress, skipping")
            return false
        }
        try {
            return registerServicesInternal(context, httpPort, httpsPort)
        } finally {
            registering.set(false)
        }
    }

    private fun registerServicesInternal(context: Context, httpPort: Int?, httpsPort: Int?): Boolean {
        unregisterService()

        val hasAnyPort = (httpPort != null && httpPort > 0) || (httpsPort != null && httpsPort > 0)
        if (!hasAnyPort) {
            LogCat.e("No active web service port, skip mDNS responder start")
            return false
        }

        val hostname = TempData.mdnsHostname
        return MdnsHostResponder.start(context, hostname)
    }

     


    fun unregisterService() {
        MdnsHostResponder.stop()
    }
}