class MediaAwareDelegate(private val view: View, context: Context) : MediaStateListener {
    private var audioID: Long = -1L
    var isPlaying: Boolean = false
        private set
    var isInSelection: Boolean = false
        private set
    var enableDragHandle: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            cachedShaderW = -1f
            updateRightPadding()
            view.invalidate()
        }
    var enableGridMode: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            appliedIconCount = -1
            updateRightPadding()
            view.invalidate()
        }
    private var selectionScope: CoroutineScope? = null
    private val playDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_play)
    private val checkDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_check)
    private val dragHandleDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_drag_indicator)
    private val selectionBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val selectionClipPath = Path()
    private val selectionRect = RectF()
    private var cachedShaderW = -1f
    private var cachedShaderH = -1f
    private var cachedShaderColor = 0
    private var cachedIsPlaying = false
    private var cachedIsInSelection = false
    private var cachedDragHandle = false
    private var appliedIconCount = -1
    fun setAudioID(audioID: Long) {
        if (audioID == -1L) return
        this.audioID = audioID
        isPlaying = audioID == MediaPlaybackManager.getCurrentSongId()
        isInSelection = SelectionManager.selectedAudios.value.any { it.id == audioID }
        appliedIconCount = -1
        updateRightPadding()
        view.invalidate()
    }
    fun isDragHandleRegion(x: Float): Boolean {
        if (!enableDragHandle) return false
        return x >= view.width - view.height
    }
    override fun onAudioChange(audio: Audio?) {
        val shouldBePlaying = audio?.id == audioID
        if (isPlaying == shouldBePlaying) return
        isPlaying = shouldBePlaying
        updateRightPadding()
        view.invalidate()
    }
    fun onSizeChanged(w: Int, h: Int) {
        rebuildSelectionGeometry(w.toFloat(), h.toFloat(), isPlaying, isInSelection)
        updateRightPadding()
    }
    fun onLayout() {
        updateRightPadding()
    }
    fun updateRightPadding() {
        if (enableGridMode) return
        if (view.height == 0) return
        val h = view.height.toFloat()
        val iconSize = (h * 0.45f).toInt()
        val iconPadding = (h * 0.10f).toInt()
        val iconCount = (if (enableDragHandle) 1 else 0) +
                (if (isPlaying) 1 else 0) +
                (if (isInSelection) 1 else 0)
        if (iconCount == appliedIconCount) return
        appliedIconCount = iconCount
        view.setPadding(view.paddingLeft, view.paddingTop, iconCount * (iconSize + iconPadding), view.paddingBottom)
    }
    fun rebuildSelectionGeometry(w: Float, h: Float, playing: Boolean, inSelection: Boolean) {
        if (w <= 0f || h <= 0f) return
        val accentColor = ThemeManager.accent.secondaryAccentColor
        val maxAlpha = 60
        var slotCount = if (playing && inSelection) 2f else if (playing || inSelection) 1f else 0f
        if (enableDragHandle) slotCount += 1f
        if (slotCount == 0f) {
            cachedShaderW = w
            cachedShaderH = h
            cachedShaderColor = accentColor
            cachedIsPlaying = playing
            cachedIsInSelection = inSelection
            cachedDragHandle = enableDragHandle
            return
        }
        val indicatorW = h * slotCount
        val positions = floatArrayOf(0f, 0.15f, 0.30f, 0.45f, 0.65f, 0.80f, 1f)
        val colors = positions.map { t ->
            val alpha = (maxAlpha * t * t * t).toInt().coerceIn(0, maxAlpha)
            ColorUtils.setAlphaComponent(accentColor, alpha)
        }.toIntArray()
        selectionBgPaint.shader = LinearGradient(
                w - indicatorW, 0f,
                w, 0f,
                colors,
                positions,
                Shader.TileMode.CLAMP
        )
        val cornerR = AppearancePreferences.getCornerRadius()
        selectionRect.set(w - indicatorW, 0f, w, h)
        selectionClipPath.rewind()
        selectionClipPath.addRoundRect(selectionRect, cornerR, cornerR, Path.Direction.CW)
        cachedShaderW = w
        cachedShaderH = h
        cachedShaderColor = accentColor
        cachedIsPlaying = playing
        cachedIsInSelection = inSelection
        cachedDragHandle = enableDragHandle
    }
    fun onDraw(canvas: Canvas) {
        if (enableGridMode) return
        if (!isPlaying && !isInSelection && !enableDragHandle) return
        val w = view.width.toFloat()
        val h = view.height.toFloat()
        if (cachedShaderW != w || cachedShaderH != h ||
                cachedShaderColor != ThemeManager.accent.secondaryAccentColor ||
                cachedIsPlaying != isPlaying || cachedIsInSelection != isInSelection ||
                cachedDragHandle != enableDragHandle) {
            rebuildSelectionGeometry(w, h, isPlaying, isInSelection)
        }
        if (!selectionRect.isEmpty) {
            canvas.withClip(selectionClipPath) {
                drawRect(selectionRect, selectionBgPaint)
            }
        }
    }
    fun dispatchDraw(canvas: Canvas, superDispatch: () -> Unit) {
        val w = view.width.toFloat()
        val h = view.height.toFloat()
        if (w <= 0f || h <= 0f) {
            superDispatch()
            return
        }
        val hasActiveState = isPlaying || isInSelection
        val needsGeometry = cachedShaderW != w || cachedShaderH != h ||
                cachedShaderColor != ThemeManager.accent.secondaryAccentColor ||
                cachedIsPlaying != isPlaying || cachedIsInSelection != isInSelection ||
                cachedDragHandle != enableDragHandle
        if (needsGeometry) rebuildSelectionGeometry(w, h, isPlaying, isInSelection)
        if (enableGridMode && hasActiveState) {
            canvas.saveLayerAlpha(0f, 0f, w, h, GRID_CONTENT_ALPHA)
            superDispatch()
            canvas.restore()
            drawGridModeIcons(canvas, w, h)
        } else {
            superDispatch()
            if (isPlaying || isInSelection || enableDragHandle) {
                drawListModeIcons(canvas, w, h)
            }
        }
        updateRightPadding()
    }
    private fun drawGridModeIcons(canvas: Canvas, w: Float, h: Float) {
        val accentColor = ThemeManager.accent.secondaryAccentColor
        val iconSize = (h * 0.30f).toInt()
        val iconGap = (h * 0.08f).toInt()
        val visibleCount = (if (isPlaying) 1 else 0) + (if (isInSelection) 1 else 0)
        val totalW = visibleCount * iconSize + (visibleCount - 1) * iconGap
        val startLeft = ((w - totalW) / 2f).toInt()
        val iconTop = ((h - iconSize) / 2f).toInt()
        var slot = 0
        fun nextLeft(): Int = startLeft + slot++ * (iconSize + iconGap)
        if (isPlaying) {
            val left = nextLeft()
            playDrawable?.let {
                it.setTint(accentColor)
                it.setBounds(left, iconTop, left + iconSize, iconTop + iconSize)
                it.alpha = 255
                it.draw(canvas)
            }
        }
        if (isInSelection) {
            val left = nextLeft()
            checkDrawable?.let {
                it.setTint(accentColor)
                it.setBounds(left, iconTop, left + iconSize, iconTop + iconSize)
                it.alpha = 255
                it.draw(canvas)
            }
        }
    }
    private fun drawListModeIcons(canvas: Canvas, w: Float, h: Float) {
        val accentColor = ThemeManager.accent.secondaryAccentColor
        val iconSize = (h * 0.45f).toInt()
        val iconPadding = (h * 0.10f).toInt()
        val iconTop = ((h - iconSize) / 2f).toInt()
        var nextSlot = 0
        fun iconLeft(slot: Int) = (w - (slot + 1) * (iconSize + iconPadding)).toInt()
        if (enableDragHandle) {
            val left = iconLeft(nextSlot++)
            dragHandleDrawable?.let {
                it.setTint(accentColor)
                it.setBounds(left, iconTop, left + iconSize, iconTop + iconSize)
                it.alpha = 200
                it.draw(canvas)
            }
        }
        if (isInSelection) {
            val left = iconLeft(nextSlot++)
            checkDrawable?.let {
                it.setTint(accentColor)
                it.setBounds(left, iconTop, left + iconSize, iconTop + iconSize)
                it.alpha = 200
                it.draw(canvas)
            }
        }
        if (isPlaying) {
            val left = iconLeft(nextSlot)
            playDrawable?.let {
                it.setTint(accentColor)
                it.setBounds(left, iconTop, left + iconSize, iconTop + iconSize)
                it.alpha = 200
                it.draw(canvas)
            }
        }
    }
    fun onAttachedToWindow() {
        MediaPlaybackManager.registerListener(this)
        selectionScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        selectionScope?.launch {
            SelectionManager.selectedAudios.collect { selected ->
                val nowSelected = selected.any { it.id == audioID }
                if (nowSelected != isInSelection) {
                    isInSelection = nowSelected
                    updateRightPadding()
                    view.invalidate()
                }
            }
        }
    }
    fun onDetachedFromWindow() {
        MediaPlaybackManager.unregisterListener(this)
        selectionScope?.cancel()
        selectionScope = null
    }
    companion object {
        const val GRID_CONTENT_ALPHA = 80
    }
}
