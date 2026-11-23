@Suppress("unused")
class BlurShadow(private val context: Context) : BitmapTransformation() {
    private var blurRadius = 0f
    private var elevation = 0f
    private var angle = 0f
    private var colour: Int
    private var shadowEffectEnabled: Boolean = false
    @IntDef(EAST, NORTHEAST, NORTH, NORTHWEST, WEST, SOUTHWEST, SOUTH, SOUTHEAST)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Direction
    fun setBlurRadius(blurRadius: Float): BlurShadow {
        this.blurRadius = blurRadius
        return this
    }
    fun setElevation(elevation: Float): BlurShadow {
        this.elevation = elevation
        return this
    }
    fun setAngle(angle: Float): BlurShadow {
        this.angle = angle
        return this
    }
    fun setDirection(@Direction d: Int): BlurShadow {
        angle = getAngle(d)
        return this
    }
    fun setShadowColour(@ColorInt colour: Int): BlurShadow {
        this.colour = colour
        return this
    }
    fun setShadowColourRes(@ColorRes res: Int): BlurShadow {
        colour = ContextCompat.getColor(context, res)
        return this
    }
    private fun getAngle(@Direction d: Int): Float {
        return when (d) {
            EAST -> 0f
            NORTHEAST -> 45f
            NORTH -> 90f
            NORTHWEST -> 135f
            WEST -> 180f
            SOUTHWEST -> 225f
            SOUTH -> 270f
            SOUTHEAST -> 315f
            else -> throw IllegalArgumentException("Invalid Direction")
        }
    }
    override fun transform(pool: BitmapPool, source: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val bitmap = createBitmap(source.width, source.height)
        val shadow: Bitmap
        val shadowX = elevation * cos(Math.toRadians(angle.toDouble())).toFloat()
        val shadowY = -(elevation * sin(Math.toRadians(angle.toDouble())).toFloat())
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = if (shadowEffectEnabled) {
                ColorMatrixColorFilter(ColorMatrix().apply {
                    setScale(SHADOW_SCALE_RGB, SHADOW_SCALE_RGB, SHADOW_SCALE_RGB, SHADOW_SCALE_ALPHA)
                })
            } else {
                PorterDuffColorFilter(colour, PorterDuff.Mode.SRC_IN)
            }
            isAntiAlias = true
        }
        if (blurRadius <= MAX_BLUR_RADIUS) {
            shadow = if (shadowEffectEnabled) {
                Toolkit.blur(source, blurRadius.toInt())
            } else {
                createBitmap(source.width, source.height)
            }
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(shadow, 0F, 0F, shadowPaint)
            canvas.drawBitmap(source, 0f, 0f, null)
        } else {
            val scaleFactor = MAX_BLUR_RADIUS / blurRadius
            @Suppress("UNUSED_VARIABLE") val scaledWidth = 1.coerceAtLeast((source.width.toFloat() * scaleFactor).roundToInt())
            @Suppress("UNUSED_VARIABLE") val scaledHeight = 1.coerceAtLeast((source.height.toFloat() * scaleFactor).roundToInt())
            val scaled = source.scale(source.width, source.height)
            shadow = if (shadowEffectEnabled) {
                Toolkit.blur(scaled, MAX_BLUR_RADIUS.toInt())
            } else {
                createBitmap(source.width, source.height)
            }
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(shadow, shadowX, shadowY, shadowPaint)
            canvas.drawBitmap(source, 0f, 0f, null)
        }
        shadow.recycle()
        return bitmap
    }
    override fun equals(other: Any?): Boolean {
        if (other is BlurShadow) {
            return blurRadius == other.blurRadius
                    && elevation == other.elevation
                    && angle == other.angle
                    && colour == other.colour
                    && shadowEffectEnabled == other.shadowEffectEnabled
        }
        return false
    }
    override fun hashCode(): Int {
        return Util.hashCode(ID.hashCode(),
                             Util.hashCode(blurRadius,
                                           Util.hashCode(elevation,
                                                         Util.hashCode(angle,
                                                                       Util.hashCode(colour,
                                                                                     Util.hashCode(if (shadowEffectEnabled) 1 else 0))))))
    }
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        val messages = ArrayList<ByteArray>()
        messages.add(ID_BYTES)
        messages.add(ByteBuffer.allocate(java.lang.Float.SIZE / java.lang.Byte.SIZE).putFloat(blurRadius).array())
        messages.add(ByteBuffer.allocate(java.lang.Float.SIZE / java.lang.Byte.SIZE).putFloat(elevation).array())
        messages.add(ByteBuffer.allocate(java.lang.Float.SIZE / java.lang.Byte.SIZE).putFloat(angle).array())
        messages.add(ByteBuffer.allocate(Integer.SIZE / java.lang.Byte.SIZE).putInt(colour).array())
        messages.add(ByteBuffer.allocate(1).put(if (shadowEffectEnabled) 1.toByte() else 0.toByte()).array())
        for (c in messages.indices) {
            messageDigest.update(messages[c])
        }
    }
    companion object {
        private const val ID = "app.simple.inure.glide.transformations.Shadow"
        private val ID_BYTES = ID.toByteArray()
        const val MAX_BLUR_RADIUS = 25.0f
        const val DEFAULT_SHADOW_SIZE = 20.0F
        private const val SHADOW_SCALE_RGB = 0.85f
        private const val SHADOW_SCALE_ALPHA = 0.8f
        const val EAST = 0
        const val NORTHEAST = 1
        const val NORTH = 2
        const val NORTHWEST = 3
        const val WEST = 4
        const val SOUTHWEST = 5
        const val SOUTH = 6
        const val SOUTHEAST = 7
    }
    init {
        colour = Color.argb(60, 0, 0, 0)
        shadowEffectEnabled = AppearancePreferences.isShadowEffectOn()
    }
}
