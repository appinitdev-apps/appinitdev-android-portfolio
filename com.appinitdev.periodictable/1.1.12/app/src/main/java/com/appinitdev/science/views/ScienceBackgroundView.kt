class ScienceBackgroundView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    private data class IconInstance(
        val x: Float,
        val y: Float,
        val rotation: Float, 
        val type: IconType,
        val size: Float      
    )
    private enum class IconType { PI, ATOM }
    private val icons = mutableListOf<IconInstance>()
    private val iconCount = 8 
    private val basePiSize = 90f      
    private val baseAtomRadius = 72f  
    private val piPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2299FF")
        alpha = 25
        style = Paint.Style.FILL
        textSize = basePiSize 
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    private val atomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#55C27E")
        alpha = 25
        style = Paint.Style.STROKE
        strokeWidth = 5f 
    }
    private var lastWidth = 0
    private var lastHeight = 0
    private fun getIconRadius(size: Float, type: IconType): Float {
        return when (type) {
            IconType.PI -> basePiSize * size * 0.7f 
            IconType.ATOM -> baseAtomRadius * size * 1.8f 
        }
    }
    private fun generateIcons(w: Int, h: Int) {
        icons.clear()
        val maxAttempts = 180 
        val minSize = 0.68f
        val maxSize = 1.8f
        val placedIcons = mutableListOf<IconInstance>()
        var tries = 0
        var placed = 0
        while (placed < iconCount && tries < iconCount * maxAttempts) {
            tries++
            val type = if (placed % 2 == 0) IconType.PI else IconType.ATOM
            val size = Random.nextFloat() * (maxSize - minSize) + minSize
            val radius = getIconRadius(size, type)
            val x = Random.nextFloat() * (w - 2 * radius) + radius
            val y = Random.nextFloat() * (h - 2 * radius) + radius
            val rotation = Random.nextFloat() * 60f - 30f 
            var overlaps = false
            for (other in placedIcons) {
                val dist = hypot(x - other.x, y - other.y)
                val minDist = radius + getIconRadius(other.size, other.type)
                if (dist < minDist) {
                    overlaps = true
                    break
                }
            }
            if (!overlaps) {
                placedIcons.add(IconInstance(x, y, rotation, type, size))
                placed++
            }
            if (tries > iconCount * maxAttempts) break
        }
        icons.clear()
        icons.addAll(placedIcons)
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        lastWidth = w
        lastHeight = h
        generateIcons(w, h)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (icons.isEmpty()) generateIcons(width, height)
        for (icon in icons) {
            canvas.save()
            canvas.translate(icon.x, icon.y)
            canvas.rotate(icon.rotation)
            canvas.scale(icon.size, icon.size)
            when (icon.type) {
                IconType.PI -> drawPiSymbol(canvas)
                IconType.ATOM -> drawAtom(canvas)
            }
            canvas.restore()
        }
    }
    private fun drawPiSymbol(canvas: Canvas) {
        canvas.drawText("π", -basePiSize/2, basePiSize/2, piPaint)
    }
    private fun drawAtom(canvas: Canvas) {
        val r = baseAtomRadius
        canvas.drawCircle(0f, 0f, r, atomPaint) 
        for (i in 0..2) {
            val angle = i * 45f
            canvas.save()
            canvas.rotate(angle)
            canvas.drawOval(RectF(-r*1.7f, -r*0.7f, r*1.7f, r*0.7f), atomPaint)
            canvas.restore()
        }
        val nucleusPaint = Paint(atomPaint).apply {
            style = Paint.Style.FILL
            alpha = 35 
        }
        canvas.drawCircle(0f, 0f, r/4, nucleusPaint)
        for (i in 0..2) {
            val theta = Math.toRadians((i * 120).toDouble())
            val ex = (r * 1.7 * cos(theta)).toFloat()
            val ey = (r * 0.7 * sin(theta)).toFloat()
            canvas.drawCircle(ex, ey, 6f, nucleusPaint)
        }
    }
}