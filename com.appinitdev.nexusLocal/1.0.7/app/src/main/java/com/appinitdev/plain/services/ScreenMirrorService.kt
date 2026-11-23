class ScreenMirrorService : LifecycleService() {
    private var orientationEventListener: OrientationEventListener? = null
    private var isPortrait = true
    private var notificationId: Int = 0
    private lateinit var webRtcManager: ScreenMirrorWebRtcManager
    @Volatile
    private var running = false
    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()
        instance = this
        webRtcManager = ScreenMirrorWebRtcManager(
            context = this,
            getQuality = { qualityData },
            getIsPortrait = { isPortrait },
        )
        NotificationHelper.ensureDefaultChannel()
        isPortrait = isPortrait()
        orientationEventListener =
            object : OrientationEventListener(this) {
                override fun onOrientationChanged(orientation: Int) {
                    val newIsPortrait = isPortrait()
                    if (isPortrait != newIsPortrait) {
                        isPortrait = newIsPortrait
                        PlainAccessibilityService.invalidateScreenSizeCache()
                        webRtcManager.onOrientationChanged()
                    }
                }
            }
    }
    @SuppressLint("WrongConstant")
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)
        val resultCode = intent?.getIntExtra("code", -1) ?: -1
        val resultData: Intent? = intent?.parcelable("data")
        if (notificationId == 0) {
            notificationId = NotificationHelper.generateId()
        }
        val notification =
            NotificationHelper.createServiceNotification(
                this,
                Constants.ACTION_STOP_SCREEN_MIRROR,
                LocaleHelper.getString(Res.string.screen_mirror_service_is_running),
            )
        var mMediaProjection: MediaProjection? = null
        try {
            ServiceCompat.startForeground(this, notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } catch (se: SecurityException) {
            LogCat.e("screen mirror: startForeground failed (OEM AppOp fix): ${se.message}")
            if (resultCode == -1 && resultData != null) {
                mMediaProjection = runCatching { mediaProjectionManager.getMediaProjection(resultCode, resultData) }.getOrNull()
            }
            if (mMediaProjection == null) { stop(); return START_NOT_STICKY }
            try {
                ServiceCompat.startForeground(this, notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
            } catch (se2: SecurityException) {
                LogCat.e("screen mirror: startForeground still failed after AppOp fix: ${se2.message}")
                stop()
                return START_NOT_STICKY
            }
        }
        if (mMediaProjection == null && resultCode == -1 && resultData != null) {
            mMediaProjection = runCatching { mediaProjectionManager.getMediaProjection(resultCode, resultData) }.getOrElse {
                LogCat.e("screen mirror: getMediaProjection failed: ${it.message}")
                null
            }
        }
        if (mMediaProjection == null) {
            LogCat.e("MediaProjection is null — permission was denied or revoked by OS")
            stop()
            return START_NOT_STICKY
        }
        orientationEventListener?.enable()
        running = true
        val captureStarted = webRtcManager.initCapture(mMediaProjection)
        if (!captureStarted) {
            LogCat.e("screen mirror: initCapture failed (VirtualDisplay could not be created), stopping service")
            stop()
            return START_NOT_STICKY
        }
        sendEvent(
            WebSocketEvent(
                EventType.SCREEN_MIRRORING,
                ""
            ),
        )
        return START_NOT_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
        running = false
        webRtcManager.releaseAll()
        orientationEventListener?.disable()
        instance = null
    }
    fun isRunning(): Boolean = running
    fun handleWebRtcSignaling(clientId: String, message: WebRtcSignalingMessage) {
        webRtcManager.handleSignaling(clientId, message)
    }
    fun onQualityChanged() {
        webRtcManager.onQualityChanged()
    }
    fun stop() {
        running = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    companion object {
        @Volatile
        var instance: ScreenMirrorService? = null
        var qualityData = DScreenMirrorQuality()
    }
}
