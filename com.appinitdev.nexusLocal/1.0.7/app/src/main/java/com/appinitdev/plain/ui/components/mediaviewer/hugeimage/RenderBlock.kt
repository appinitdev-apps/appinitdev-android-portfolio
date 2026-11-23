
data class RenderBlock(
    var inBound: Boolean = false,
    var inSampleSize: Int = 1,
    var renderOffset: IntOffset = IntOffset.Zero,
    var renderSize: IntSize = IntSize.Zero,
    var sliceRect: Rect = Rect(0, 0, 0, 0),
    var bitmap: Bitmap? = null,
) {
    fun release() {
        bitmap?.recycle()
        bitmap = null
    }
}