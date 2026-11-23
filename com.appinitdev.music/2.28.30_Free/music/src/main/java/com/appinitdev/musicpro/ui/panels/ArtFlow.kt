
class ArtFlow : MediaFragment() {

    private lateinit var binding: FragmentArtflowBinding
    private val songsViewModel: SongsViewModel by viewModels({ requireActivity() })
    private val coverCache by lazy { ArtFlowCoverCache(context = requireContext().applicationContext, maxMemoryCacheSizeMB = 50) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentArtflowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireLightBarIcons()
        requireImmersiveMode()
        requireTransparentMiniPlayer()

        WindowUtil.getStatusBarHeightWhenAvailable(binding.topMenuContainer) { height ->
            binding.topMenuContainer.setPadding(
                    binding.topMenuContainer.paddingLeft,
                    height,
                    binding.topMenuContainer.paddingRight,
                    binding.topMenuContainer.paddingBottom
            )
        }

        // Observe StateFlow with proper lifecycle handling for immediate updates
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                songsViewModel.songs.collect { audioList ->
                    updateCarousel(audioList)
                }
            }
        }

        // Set up scroll listeners once (outside the collect block)
        binding.coverflow.addScrollListener(object : ArtFlowRenderer.ScrollListener {
            override fun onCenteredIndexChanged(index: Int) {
                songsViewModel.setCarouselPosition(index)
                // Preload covers around the new position - reduced radius and size for memory efficiency
                coverCache.preloadAround(index, radius = 8, maxDimension = 512)
            }

            override fun onScrollOffsetChanged(offset: Float) {
                // No-op
            }

            override fun onSnapFinished(finalIndex: Int) {
                // No-op
            }

            override fun onSnapStarted(targetIndex: Int) {
                // No-op
            }
        })

        // Set up cover click listener once
        binding.coverflow.setOnCoverClickListener(object : OnCoverClickListener {
            override fun onCenteredCoverClick(index: Int, itemId: Any?) {
                songsViewModel.setCarouselPosition(index)
                songsViewModel.songs.value.let { songs ->
                    if (index in songs.indices) {
                        setMediaItems(songs, index)
                    }
                }
            }

            override fun onSideCoverSelected(index: Int, itemId: Any?) {
                songsViewModel.setCarouselPosition(index)
            }
        })

        // Set up all click listeners once
        binding.arrowLeft.setOnClickListener {
            binding.coverflow.scrollToIndex(binding.coverflow.getCenteredIndex() - 10)
        }

        binding.arrowRight.setOnClickListener {
            binding.coverflow.scrollToIndex(binding.coverflow.getCenteredIndex() + 10)
        }

        binding.filter.setOnClickListener {
            childFragmentManager.showSongsSort()
        }

        binding.menu.setOnClickListener {
            childFragmentManager.showCarouselMenu()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coverCache.release()
    }

    /**
     * Updates the carousel with a new list of audio items
     */
    private fun updateCarousel(audioList: List<Audio>) {
        // Update cache with new audio list
        coverCache.setAudioList(audioList)

        val provider = AlbumArtProvider(audioList)
        binding.coverflow.setDataProvider(provider)
        binding.coverflow.scrollToIndex(songsViewModel.getCarouselPosition()).also {
            if (songsViewModel.getCarouselPosition().isNotZero()) {
                binding.coverflow.reloadTextures()
            }
        }

        // Start preloading covers around the current position - reduced for memory efficiency
        coverCache.preloadAround(songsViewModel.getCarouselPosition(), radius = 8, maxDimension = 512, debounceMs = 0L)
    }

    override fun onAudio(audio: Audio) {
        super.onAudio(audio)

    }

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        when (state) {
            MediaConstants.PLAYBACK_PLAYING -> {

            }
            MediaConstants.PLAYBACK_PAUSED -> {

            }
        }
    }

    override fun getTransitionType(): TransitionType {
        return TransitionType.SLIDE
    }

    inner class AlbumArtProvider(private val audioList: List<Audio>) : ArtFlowDataProvider {
        override fun getItemCount(): Int {
            return audioList.size
        }

        override fun loadArtwork(index: Int, maxDimension: Int): Bitmap? {
            // Return only what is already in the memory cache.
            // loadSync is intentionally NOT used here: calling synchronous disk I/O from
            // inside the renderer's decode thread for every item that passes through the
            // viewport during a fast scroll causes the observable freeze (every passed-by
            // index would be loaded regardless of whether it is still visible).
            // The debounced preloadAround jobs populate the cache in the background;
            // the renderer will pick up each bitmap on the next frame after it arrives.
            return coverCache.getOrNull(index)
        }
    }

    companion object {
        fun newInstance(): ArtFlow {
            val args = Bundle()
            val fragment = ArtFlow()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "ArtFlow"
    }
}