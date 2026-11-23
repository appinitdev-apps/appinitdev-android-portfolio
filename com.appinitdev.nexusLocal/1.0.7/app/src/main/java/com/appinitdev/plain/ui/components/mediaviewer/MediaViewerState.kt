class MediaViewerState(
    offsetX: Float = DEFAULT_OFFSET_X,
    offsetY: Float = DEFAULT_OFFSET_Y,
    scale: Float = DEFAULT_SCALE,
    rotation: Float = DEFAULT_ROTATION,
) : CoroutineScope by MainScope() {
    val offsetX = Animatable(offsetX)
    val offsetY = Animatable(offsetY)
    val scale = Animatable(scale)
    val rotation = Animatable(rotation)
    var allowGestureInput by mutableStateOf(true)
    var defaultSize by mutableStateOf(IntSize(0, 0))
        internal set
    internal var containerSize by mutableStateOf(IntSize(0, 0))
    internal var maxScale by mutableFloatStateOf(1F)
    internal var fromSaver = false
    internal var resetTimeStamp by mutableLongStateOf(0L)
    internal val mountedFlow = MutableStateFlow(false)
    internal fun isRunning(): Boolean {
        return scale.isRunning
                || offsetX.isRunning
                || offsetY.isRunning
                || rotation.isRunning
    }
    suspend fun resetImmediately() {
        rotation.snapTo(DEFAULT_ROTATION)
        offsetX.snapTo(DEFAULT_OFFSET_X)
        offsetY.snapTo(DEFAULT_OFFSET_Y)
        scale.snapTo(DEFAULT_SCALE)
    }
    companion object {
        val SAVER: Saver<MediaViewerState, *> = listSaver(save = {
            listOf(it.offsetX.value, it.offsetY.value, it.scale.value, it.rotation.value)
        }, restore = {
            val state = MediaViewerState(
                offsetX = it[0],
                offsetY = it[1],
                scale = it[2],
                rotation = it[3],
            )
            state.fromSaver = true
            state
        })
    }
}
@Composable
fun rememberViewerState(
    offsetX: Float = DEFAULT_OFFSET_X,
    offsetY: Float = DEFAULT_OFFSET_Y,
    scale: Float = DEFAULT_SCALE,
    rotation: Float = DEFAULT_ROTATION,
): MediaViewerState = rememberSaveable(saver = MediaViewerState.SAVER) {
    MediaViewerState(offsetX, offsetY, scale, rotation)
}