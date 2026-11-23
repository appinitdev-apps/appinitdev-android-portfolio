class Milkdrop : MediaFragment() {
    private lateinit var binding: FragmentMilkdropBinding
    private val viewModel: MilkdropViewModel by viewModels(
            ownerProducer = { this }
    )
    private var pagerAdapter: AdapterMilkdropPager? = null
    private val fadeHandler = Handler(Looper.getMainLooper())
    private val fadeOutRunnable = Runnable {
        binding.presetPagerContainer
            .animate()
            .alpha(0.25f)
            .setDuration(FADE_DURATION_MS)
            .start()
    }
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentMilkdropBinding.inflate(inflater, container, false)
        return binding.root
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireLightBarIcons()
        requireImmersiveMode()
        requireHiddenMiniPlayer()
        setPresetPagerBackground()
        VisualizerManager.processor?.let { processor ->
            binding.milkdropSurface.connectProcessor(processor)
        }
        setupPresetPager()
        observeViewModel()
        val surfaceTouchListener = { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> showOverlay()
                MotionEvent.ACTION_UP -> v.performClick()
            }
            false
        }
        binding.milkdropSurface.setOnTouchListener(surfaceTouchListener)
        binding.root.setOnClickListener { showOverlay() }
        binding.shufflePreset.setOnClickListener {
            viewModel.toggleShuffle()
            showOverlay()
        }
        binding.seekbar.setLeftLabelProvider { progress, _, _ ->
            DateUtils.formatElapsedTime(progress.toLong().div(1000))
        }
        binding.seekbar.setRightLabelProvider { _, _, max ->
            DateUtils.formatElapsedTime(max.toLong().div(1000))
        }
        binding.seekbar.setOnSeekChangeListener(object : FelicitySeekbar.OnSeekChangeListener {
            override fun onProgressChanged(seekbar: FelicitySeekbar, progress: Float, fromUser: Boolean) {
                if (fromUser) {
                    MediaPlaybackManager.seekTo(progress.toLong())
                }
            }
        })
        binding.next.setOnClickListener {
            MediaPlaybackManager.next()
        }
        binding.previous.setOnClickListener {
            MediaPlaybackManager.previous()
        }
        binding.play.setOnClickListener {
            MediaPlaybackManager.flipState()
        }
        scheduleOverlayFadeOut()
    }
    private fun setupPresetPager() {
        pagerAdapter = AdapterMilkdropPager {
            childFragmentManager.showMilkdropPresets()
        }
        binding.presetPager.adapter = pagerAdapter
        binding.presetPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.loadPresetAtIndex(position)
            }
        })
    }
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.presets.collect { list ->
                    pagerAdapter?.submitList(list)
                    if (list.isNotEmpty()) {
                        val index = viewModel.currentIndex.value.coerceIn(0, list.lastIndex)
                        binding.presetPager.setCurrentItem(index, false)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentIndex.collect { index ->
                    val itemCount = pagerAdapter?.itemCount ?: 0
                    if (itemCount > 0 && index != binding.presetPager.currentItem) {
                        binding.presetPager.setCurrentItem(index, false)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.presetContent.collect { content ->
                    delay(250) 
                    content?.let {
                        loadCurrentPreset(it)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isShuffleEnabled.collect { enabled ->
                    binding.shufflePreset.alpha = if (enabled) 1.0f else 0.35f
                }
            }
        }
    }
    private fun showOverlay() {
        fadeHandler.removeCallbacks(fadeOutRunnable)
        binding.presetPagerContainer
            .animate()
            .alpha(1f)
            .setDuration(FADE_DURATION_MS)
            .start()
        scheduleOverlayFadeOut()
    }
    private fun scheduleOverlayFadeOut() {
        fadeHandler.postDelayed(fadeOutRunnable, OVERLAY_VISIBLE_MS)
    }
    private fun loadCurrentPreset(content: String) {
        binding.milkdropSurface.loadPreset(content, smooth = true)
    }
    private fun setPresetPagerBackground() {
        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, getCornerRadius())
            .build()
        val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        materialShapeDrawable.setStroke(0.5F, Color.DKGRAY)
        binding.presetPagerContainer.background = materialShapeDrawable
    }
    private fun updateState() {
        val audio = MediaPlaybackManager.getCurrentSong() ?: return
        binding.name.text = audio.getProperTitle()
        binding.artist.text = audio.getProperArtists()
        binding.seekbar.setMax(audio.duration.toFloat())
        binding.seekbar.setProgress(MediaPlaybackManager.getSeekPosition().toFloat(), fromUser = false, animate = true)
        updatePlayButtonState(MediaPlaybackManager.isPlaying())
    }
    private fun updatePlayButtonState(isPlaying: Boolean) {
        if (isPlaying) {
            binding.play.setPlaying()
        } else {
            binding.play.setPaused()
        }
    }
    override fun onAudio(audio: Audio) {
        super.onAudio(audio)
        val forward = MediaPlaybackManager.lastNavigationDirection
        binding.name.setTextWithEffect(audio.getProperTitle(), forward)
        binding.artist.setTextWithEffect(audio.getProperArtists(), forward, 50L)
        binding.seekbar.setMaxWithReset(audio.duration.toFloat())
        binding.seekbar.setProgress(MediaPlaybackManager.getSeekPosition().toFloat(), fromUser = false, animate = true)
    }
    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        when (state) {
            MediaConstants.PLAYBACK_PLAYING -> {
                updatePlayButtonState(true)
            }
            MediaConstants.PLAYBACK_PAUSED -> {
                updatePlayButtonState(false)
            }
        }
    }
    override fun onSeekChanged(seek: Long) {
        super.onSeekChanged(seek)
        binding.seekbar.setProgress(seek.toFloat(), fromUser = false, animate = true)
    }
    override val wantsMiniPlayerVisible: Boolean
        get() = false
    override fun onResume() {
        super.onResume()
        binding.milkdropSurface.onResume()
        VisualizerManager.processor?.let { processor ->
            binding.milkdropSurface.connectProcessor(processor)
        }
    }
    override fun onPause() {
        binding.milkdropSurface.onPause()
        super.onPause()
    }
    override fun onDestroyView() {
        fadeHandler.removeCallbacksAndMessages(null)
        pagerAdapter = null
        binding.milkdropSurface.disconnectProcessor()
        super.onDestroyView()
    }
    override fun onStartPredictiveBack() {
        super.onStartPredictiveBack()
        binding.milkdropSurface.onPause()
    }
    override fun onCancelPredictiveBack() {
        super.onCancelPredictiveBack()
        binding.milkdropSurface.onResume()
        VisualizerManager.processor?.let { processor ->
            binding.milkdropSurface.connectProcessor(processor)
        }
    }
    override fun getTransitionType(): TransitionType {
        return TransitionType.SLIDE
    }
    companion object {
        const val TAG = "Milkdrop"
        private const val FADE_DURATION_MS = 500L
        private const val OVERLAY_VISIBLE_MS = 5_000L
        fun newInstance(): Milkdrop = Milkdrop()
    }
}
