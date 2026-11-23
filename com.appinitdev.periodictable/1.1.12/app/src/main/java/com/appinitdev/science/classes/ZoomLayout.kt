class ZoomLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var scaleFactor = 1f
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val matrix = Matrix()
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }
    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.scale(scaleFactor, scaleFactor, width / 2f, height / 2f)
        super.onDraw(canvas)
        canvas.restore()
    }
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.1f, 5.0f) 
            invalidate()
            return true
        }
    }
}