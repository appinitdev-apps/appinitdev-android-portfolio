@AndroidEntryPoint
class ServerModeService : Service() {
    @Inject
    lateinit var songStatRepository: SongStatRepository
    private var httpServer: MusicHttpServer? = null
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopServerAndSelf()
            else -> startHttpServer()
        }
        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        shutdownServer()
        super.onDestroy()
    }
    private fun startHttpServer() {
        if (httpServer?.isAlive == true) return
        val port = ServerPreferences.getServerPort()
        val server = MusicHttpServer(applicationContext, port, songStatRepository)
        try {
            server.startServer()
            httpServer = server
            _isRunning.value = true
            val ip = resolveLocalIpAddress() ?: getString(R.string.server_unknown_ip)
            startForeground(NOTIFICATION_ID, buildNotification(ip, port))
            Log.i(TAG, "Server started on http://$ip:$port")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start HTTP server on port $port", e)
            _isRunning.value = false
            stopSelf()
        }
    }
    private fun stopServerAndSelf() {
        shutdownServer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    private fun shutdownServer() {
        httpServer?.stop()
        httpServer = null
        _isRunning.value = false
    }
    private fun buildNotification(ip: String, port: Int): Notification {
        val url = "http://$ip:$port"
        val openIntent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val openPending = PendingIntent.getActivity(
                this, 0, openIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = Intent(this, ServerModeService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
                this, 1, stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wifi)
            .setContentTitle(getString(R.string.server_notification_title))
            .setContentText(url)
            .setSubText(getString(R.string.server_notification_subtext))
            .setContentIntent(openPending)
            .addAction(R.drawable.ic_close, getString(R.string.server_stop_action), stopPending)
            .setOngoing(true)
            .setShowWhen(false)
            .setSilent(true)
            .build()
    }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.server_channel_name),
                NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.server_channel_description)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    private fun resolveLocalIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()
                ?.toList()
                ?.filter { it.name.startsWith("wlan") || it.name.startsWith("eth") }
                ?.flatMap { it.inetAddresses.toList() }
                ?.firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                ?.hostAddress
        } catch (e: Exception) {
            Log.w(TAG, "Could not resolve local IP address", e)
            null
        }
    }
    companion object {
        private const val TAG = "ServerModeService"
        private const val CHANNEL_ID = "felicity_server_mode"
        private const val NOTIFICATION_ID = 9001
        private const val ACTION_STOP = "app.simple.felicity.ACTION_SERVER_STOP"
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
        fun start(context: Context) {
            val intent = Intent(context, ServerModeService::class.java)
            context.startForegroundService(intent)
        }
        fun stop(context: Context) {
            val intent = Intent(context, ServerModeService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
