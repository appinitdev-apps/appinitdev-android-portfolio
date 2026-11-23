class MediaAwareRippleLinearLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : DynamicRippleLinearLayout(context, attrs), MediaStateListener {
    private val delegate = MediaAwareDelegate(this, context)
    var enableDragHandle: Boolean
        get() = delegate.enableDragHandle
        set(value) {
            delegate.enableDragHandle = value
        }
    var enableGridMode: Boolean
        get() = delegate.enableGridMode
        set(value) {
            delegate.enableGridMode = value
        }
    init {
        setWillNotDraw(false)
    }
    fun setAudioID(audioID: Long) = delegate.setAudioID(audioID)
    fun isDragHandleRegion(x: Float) = delegate.isDragHandleRegion(x)
    override fun onAudioChange(audio: Audio?) = delegate.onAudioChange(audio)
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        delegate.onSizeChanged(w, h)
    }
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        delegate.onLayout()
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        delegate.onDraw(canvas)
    }
    override fun dispatchDraw(canvas: Canvas) {
        delegate.dispatchDraw(canvas) { super.dispatchDraw(canvas) }
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        delegate.onAttachedToWindow()
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        delegate.onDetachedFromWindow()
    }
}