
@AndroidEntryPoint
class Lyrics : MediaFragment(), AddLyrics.Companion.OnLyricsCreatedListener {

    private lateinit var binding: FragmentLyricsBinding

     







    private var currentAudioPath: String? = null

     




    private var pendingWaveformAudio: Audio? = null

     
    private val textSizeHandler = Handler(Looper.getMainLooper())
    private val textSizeRunnable = Runnable { applyTextSize() }

     
    private val waveformViewModel: WaveformViewModel by viewModels()

     



    private val lyricsViewModel: LyricsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLyricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireHiddenMiniPlayer()
        requireImmersiveMode()
        setAlignment()
        applyTextSize()
        updateState()

        binding.lrc.setOnLrcClickListener { timeInMillis, _ ->
            MediaPlaybackManager.seekTo(timeInMillis)
        }

        binding.settings.setOnClickListener {
            childFragmentManager.showLyricsMenu().setOnMenuListener(object : LyricsMenu.Companion.LyricsMenuListener {
                override fun onTimeMinusClicked() {
                    lyricsViewModel.seekBy(-SEEK_JUMP_MS)
                    
                }

                override fun onTimePlusClicked() {
                    lyricsViewModel.seekBy(SEEK_JUMP_MS)
                    
                }

                override fun onLyricsDelete() {
                    withSureDialog { sure ->
                        if (sure) {
                            lyricsViewModel.deleteLrc {
                                binding.lrc.reset()
                                Log.d(TAG, "Lyrics deleted successfully.")
                            }
                        }
                    }
                }

                override fun onLrcEdit() {
                    val currentAudio = MediaPlaybackManager.getCurrentSong() ?: return
                    openFragment(LrcEditor.newInstance(currentAudio), LrcEditor.TAG)
                }

                override fun onAddLyrics() {
                    val currentAudio = MediaPlaybackManager.getCurrentSong() ?: return
                    childFragmentManager.showAddLyrics(currentAudio)
                        .setOnLyricsCreatedListener(this@Lyrics)
                }
            })
        }

        binding.next.setOnClickListener {
            MediaPlaybackManager.next()
        }

        binding.previous.setOnClickListener {
            MediaPlaybackManager.previous()
        }

        binding.play.setOnClickListener {
            MediaPlaybackManager.flipState()
        }

        binding.search.setOnClickListener {
            openFragment(LyricsSearch.newInstance(), LyricsSearch.TAG)
        }

        
        
        viewLifecycleOwner.lifecycleScope.launch {
            lyricsViewModel.lrcData.collect { lrcData ->
                if (lrcData == null || lrcData.isEmpty) {
                    Log.d(TAG, "No lyrics found for the current song.")
                    binding.lrc.setEmptyLrcData()
                } else {
                    Log.d(TAG, "Loaded lyrics with ${lrcData.size()} lines.")
                    binding.lrc.setLrcData(
                            lrcData, MediaPlaybackManager.getSeekPosition() + lyricsViewModel.syncOffset)
                }
            }
        }

        
        
        viewLifecycleOwner.lifecycleScope.launch {
            lyricsViewModel.loadingStatus.collect { status ->
                val message = when (status) {
                    is LyricsLoadingStatus.Searching -> getString(R.string.searching_lyrics)
                    is LyricsLoadingStatus.Downloading -> getString(R.string.downloading_lyrics)
                    is LyricsLoadingStatus.Idle -> getString(R.string.no_lyrics_found)
                }
                binding.lrc.setEmptyText(message)
            }
        }

        
        
        parentFragmentManager.setFragmentResultListener(
                LyricsSearch.REQUEST_KEY_LYRICS_SAVED, viewLifecycleOwner) { _, _ ->
            Log.d(TAG, "Lyrics saved signal received, reloading lrc data.")
            lyricsViewModel.reloadLrcData()
        }

        
        
        parentFragmentManager.setFragmentResultListener(
                LrcEditor.REQUEST_KEY_LRC_SAVED, viewLifecycleOwner) { _, _ ->
            Log.d(TAG, "LRC editor saved signal received, reloading lrc data.")
            lyricsViewModel.reloadLrcData()
        }

        binding.seekbar.setLeftLabelProvider { progress, _, _ ->
            DateUtils.formatElapsedTime(progress.div(1000))
        }

        binding.seekbar.setRightLabelProvider { _, _, max ->
            DateUtils.formatElapsedTime(max.div(1000))
        }

        binding.seekbar.setOnSeekListener(object : WaveformSeekbar.OnSeekListener {
            override fun onSeekEnd(positionMs: Long) {
                MediaPlaybackManager.seekTo(positionMs)
            }
        })

        binding.seekbar.setOnFlingEndListener { positionMs ->
            MediaPlaybackManager.seekTo(positionMs)
        }

        waveformViewModel.getWaveformData().observe(viewLifecycleOwner) { amplitudes ->
            binding.seekbar.setAmplitudes(amplitudes)
        }
    }

    private fun setAlignment(animate: Boolean = false) {
        when (LyricsPreferences.getLrcAlignment()) {
            LyricsPreferences.LEFT -> binding.lrc.setTextAlignment(FelicityLrcView.Alignment.LEFT, animate)
            LyricsPreferences.CENTER -> binding.lrc.setTextAlignment(FelicityLrcView.Alignment.CENTER, animate)
            LyricsPreferences.RIGHT -> binding.lrc.setTextAlignment(FelicityLrcView.Alignment.RIGHT, animate)
        }
    }

     
    private fun applyTextSize() {
        val normal = LyricsPreferences.getLrcTextSize()
        binding.lrc.setTextSizes(normal, normal * LRC_HIGHLIGHT_TIMES)
    }

     




    private fun scheduleTextSizeUpdate() {
        textSizeHandler.removeCallbacks(textSizeRunnable)
        textSizeHandler.postDelayed(textSizeRunnable, TEXT_SIZE_DEBOUNCE_MS)
    }

    private fun updatePlayButtonState(isPlaying: Boolean) {
        if (isPlaying) {
            binding.play.setPlaying()
        } else {
            binding.play.setPaused()
        }
    }

    private fun updateState() {
        val audio = MediaPlaybackManager.getCurrentSong() ?: return
        currentAudioPath = audio.uri
        binding.title.text = audio.getProperTitle()
        binding.artists.text = audio.getProperArtists()
        binding.cover.loadArtCoverWithPayload(audio)
        binding.lrc.setDuration(audio.duration)
        binding.seekbar.setDuration(audio.duration)
        binding.seekbar.setProgress(MediaPlaybackManager.getSeekPosition(), animate = false)
        updatePlayButtonState(MediaPlaybackManager.isPlaying())

        
        
        loadWaveformWhenReady(audio)
    }

     












    private fun loadWaveformWhenReady(audio: Audio) {
        if (MediaPlaybackManager.isPlaying() || MediaPlaybackManager.isPlayerReady()) {
            waveformViewModel.loadWaveform(audio)
            pendingWaveformAudio = null
        } else {
            pendingWaveformAudio = audio
        }
    }

    override val wantsMiniPlayerVisible: Boolean
        get() = false

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            LyricsPreferences.LRC_ALIGNMENT -> setAlignment(animate = true)
            LyricsPreferences.LRC_TEXT_SIZE -> scheduleTextSizeUpdate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textSizeHandler.removeCallbacks(textSizeRunnable)
    }

    override fun onResume() {
        super.onResume()
        
        
        if (lyricsViewModel.lrcData.value?.isEmpty == true) {
            lyricsViewModel.reloadLrcData()
        }
    }

    override fun onSeekChanged(seek: Long) {
        super.onSeekChanged(seek)
        binding.lrc.updateTime(seek + lyricsViewModel.syncOffset)
        binding.seekbar.setProgress(seek, animate = true)
    }

    override fun onAudio(audio: Audio) {
        super.onAudio(audio)

        val isSameSong = audio.uri == currentAudioPath
        currentAudioPath = audio.uri

        if (!isSameSong) {
            lyricsViewModel.loadLrcData()
            val forward = MediaPlaybackManager.lastNavigationDirection
            binding.title.setTextWithEffect(audio.getProperTitle(), forward)
            binding.artists.setTextWithEffect(audio.getProperArtists(), forward, 50L)
            binding.cover.loadArtCoverWithPayload(audio)
            binding.lrc.setDuration(audio.duration)
            binding.seekbar.setDurationWithReset(audio.duration)
        }

        
        binding.seekbar.setProgress(MediaPlaybackManager.getSeekPosition(), animate = true)

        
        
        loadWaveformWhenReady(audio)
    }

    override fun onPlaybackStateChanged(state: Int) {
        super.onPlaybackStateChanged(state)
        when (state) {
            MediaConstants.PLAYBACK_READY -> {
                
                
                
                pendingWaveformAudio?.let { audio ->
                    waveformViewModel.loadWaveform(audio)
                    pendingWaveformAudio = null
                }
            }
            MediaConstants.PLAYBACK_PLAYING -> {
                updatePlayButtonState(true)
                
                
                pendingWaveformAudio?.let { audio ->
                    waveformViewModel.loadWaveform(audio)
                    pendingWaveformAudio = null
                }
            }
            MediaConstants.PLAYBACK_PAUSED -> {
                updatePlayButtonState(false)
            }
        }
    }

    override fun onLyricsCreated() {
        
        lyricsViewModel.reloadLrcData()
    }

    companion object {
        fun newInstance(): Lyrics {
            val args = Bundle()
            val fragment = Lyrics()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "LyricsFragment"

        private const val LRC_HIGHLIGHT_TIMES = 1.2F

         
        private const val TEXT_SIZE_DEBOUNCE_MS = 150L
        private const val SEEK_JUMP_MS = 500L
    }
}