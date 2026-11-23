class PlainAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        LogCat.d("PlainAccessibilityService connected")
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }
    override fun onInterrupt() {
    }
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        LogCat.d("PlainAccessibilityService destroyed")
    }
    fun dispatchControl(control: ScreenMirrorControlInput, screenWidth: Int, screenHeight: Int) {
        when (control.action) {
            ScreenMirrorControlAction.TAP -> {
                val x = (control.x ?: return) * screenWidth
                val y = (control.y ?: return) * screenHeight
                dispatchTap(x, y)
            }
            ScreenMirrorControlAction.LONG_PRESS -> {
                val x = (control.x ?: return) * screenWidth
                val y = (control.y ?: return) * screenHeight
                val duration = control.duration ?: 500L
                dispatchLongPress(x, y, duration)
            }
            ScreenMirrorControlAction.SWIPE -> {
                val startX = (control.x ?: return) * screenWidth
                val startY = (control.y ?: return) * screenHeight
                val endX = (control.endX ?: return) * screenWidth
                val endY = (control.endY ?: return) * screenHeight
                val duration = control.duration ?: 300L
                dispatchSwipe(startX, startY, endX, endY, duration)
            }
            ScreenMirrorControlAction.SCROLL -> {
                val x = (control.x ?: return) * screenWidth
                val y = (control.y ?: return) * screenHeight
                val deltaY = control.deltaY ?: 0f
                val scrollDistance = deltaY.coerceIn(-500f, 500f)
                dispatchSwipe(x, y, x, y + scrollDistance, 200L)
            }
            ScreenMirrorControlAction.BACK -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
            ScreenMirrorControlAction.HOME -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            ScreenMirrorControlAction.RECENTS -> {
                performGlobalAction(GLOBAL_ACTION_RECENTS)
            }
            ScreenMirrorControlAction.LOCK_SCREEN -> {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
            ScreenMirrorControlAction.KEY -> {
                LogCat.d("Key action not yet supported: ${control.key}")
            }
        }
    }
    private fun dispatchTap(x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
    private fun dispatchLongPress(x: Float, y: Float, duration: Long) {
        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, duration.coerceAtLeast(500))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
    private fun dispatchSwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long) {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        val stroke = GestureDescription.StrokeDescription(path, 0, duration.coerceAtLeast(50))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
    companion object {
        @Volatile
        var instance: PlainAccessibilityService? = null
        fun isEnabled(context: Context = MainApp.instance): Boolean {
            return instance != null
        }
        @Volatile
        private var cachedScreenSize: Point? = null
        fun getScreenSize(context: Context): Point {
            return cachedScreenSize ?: run {
                val size = getRealScreenSize(context)
                cachedScreenSize = size
                size
            }
        }
        private fun getRealScreenSize(context: Context): Point {
            return getMirrorRealScreenSize(context)
        }
        fun invalidateScreenSizeCache() {
            cachedScreenSize = null
        }
        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
