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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                songsViewModel.songs.collect { audioList ->
                    updateCarousel(audioList)
                }
            }
        }
        binding.coverflow.addScrollListener(object : ArtFlowRenderer.ScrollListener {
            override fun onCenteredIndexChanged(index: Int) {
                songsViewModel.setCarouselPosition(index)
                coverCache.preloadAround(index, radius = 8, maxDimension = 512)
            }
            override fun onScrollOffsetChanged(offset: Float) {
            }
            override fun onSnapFinished(finalIndex: Int) {
            }
            override fun onSnapStarted(targetIndex: Int) {
            }
        })
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
    private fun updateCarousel(audioList: List<Audio>) {
        coverCache.setAudioList(audioList)
        val provider = AlbumArtProvider(audioList)
        binding.coverflow.setDataProvider(provider)
        binding.coverflow.scrollToIndex(songsViewModel.getCarouselPosition()).also {
            if (songsViewModel.getCarouselPosition().isNotZero()) {
                binding.coverflow.reloadTextures()
            }
        }
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