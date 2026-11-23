
class ServiceStopBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            Constants.ACTION_START_HTTP_SERVER -> {
                coIO {
                    val storedToken = AdbTokenPreference.getAsync()
                    if (intent.getStringExtra("token") != storedToken) return@coIO
                    WebPreference.putAsync(true)
                    ContextCompat.startForegroundService(context, Intent(context, HttpServerService::class.java))
                }
            }

            Constants.ACTION_STOP_HTTP_SERVER -> coIO {
                val callerUid = Binder.getCallingUid()
                val appUid = context.applicationInfo.uid
                if (callerUid != appUid) {
                    
                    val storedToken = AdbTokenPreference.getAsync()
                    if (intent.getStringExtra("token") != storedToken) return@coIO
                }
                WebPreference.putAsync(false)
                HttpServerManager.stopServiceAsync(context)
            }

            Constants.ACTION_STOP_SCREEN_MIRROR -> {
                ScreenMirrorService.instance?.stop()
                ScreenMirrorService.instance = null
            }
            
            Constants.ACTION_REPOST_HTTP_NOTIFICATION -> {
                if (HttpServerService.isRunning()) {
                    ContextCompat.startForegroundService(context, Intent(context, HttpServerService::class.java))
                }
            }
        }
    }
}
