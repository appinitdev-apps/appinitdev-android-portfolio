class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private data class DrawPath(val path: Path, val color: Int, val strokeWidth: Float)
    private val pathList = mutableListOf<DrawPath>()
    private var currentPath = Path()
    private var currentColor = Color.BLACK
    private var currentStrokeWidth = 8f
    private var lastX = 0f
    private var lastY = 0f
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        for (drawPath in pathList) {
            paint.color = drawPath.color
            paint.strokeWidth = drawPath.strokeWidth
            canvas.drawPath(drawPath.path, paint)
        }
        if (!currentPath.isEmpty) {
            paint.color = currentColor
            paint.strokeWidth = currentStrokeWidth
            canvas.drawPath(currentPath, paint)
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPath.moveTo(x, y)
                lastX = x
                lastY = y
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPath.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                lastX = x
                lastY = y
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentPath.lineTo(x, y)
                pathList.add(DrawPath(Path(currentPath), currentColor, currentStrokeWidth))
                currentPath.reset()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    fun setColor(color: Int) {
        currentColor = color
    }
    fun setStrokeWidth(width: Float) {
        currentStrokeWidth = width
    }
    fun undo() {
        if (pathList.isNotEmpty()) {
            pathList.removeAt(pathList.size - 1)
            invalidate()
        }
    }
    fun clear() {
        pathList.clear()
        currentPath.reset()
        invalidate()
    }
    fun isCanvasEmpty(): Boolean = pathList.isEmpty() && currentPath.isEmpty
    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            if (width > 0) width else 800,
            if (height > 0) height else 800,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }
}
