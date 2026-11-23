abstract class BasePlayerFragment : MediaFragment() {
    private var imagePageAdapter: ImagePageAdapter? = null
    private var swipeDownListener: SwipeDownToCloseListener? = null
    private val waveformViewModel: WaveformViewModel by viewModels()
    private val lyricsViewModel: LyricsViewModel by viewModels()
    private val bookmarksViewModel: BookmarksViewModel by viewModels()
    private var pendingWaveformAudio: Audio? = null
    private var lastLoadedAudioId: Long = -1L
    protected abstract val pager: FelicityPager
    protected abstract val count: TextView
    protected abstract val queue: View
    protected abstract val search: View
    protected abstract val menu: View
    protected abstract val repeat: ImageView
    protected abstract val pcmInfo: TextView
    protected abstract val seekbar: WaveformSeekbar
    protected abstract val lyrics: View
    protected abstract val favorite: FavoriteButton
    protected abstract val equalizer: View
    protected abstract val visualizerButton: View
    protected abstract val visualizer: FelicityVisualizer
    protected abstract val title: TextView
    protected abstract val artist: TextView
    protected abstract val album: TextView
    protected abstract val shuffle: HighlightTextView
    protected abstract val lrc: LrcLineView
    protected abstract val mediaControls: FelicityMediaControls
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeDownListener = SwipeDownToCloseListener(this, requireView())
        requireView().setOnTouchListener(swipeDownListener)
        requireHiddenMiniPlayer()
        requireImmersiveMode()
        updateState()
        setVisualizerState()
        pager.setOnVerticalDragListener(object : FelicityPager.OnVerticalDragListener {
            override fun onVerticalDrag(totalDeltaY: Float, event: MotionEvent) {
                swipeDownListener?.passExternalDrag(event, event.rawY - totalDeltaY)
            }
            override fun onVerticalDragEnd(totalDeltaY: Float, velocityY: Float, event: MotionEvent) {
                swipeDownListener?.endExternalDrag(event.rawY - totalDeltaY)
            }
        })
        pager.setAdapter(
                ImagePageAdapter(
                        count = MediaPlaybackManager.getSongs().size,
                        provider = { pos, iv ->
                            val audio = MediaPlaybackManager.getSongs()[pos]
                            iv.loadArtCover(audio,
                                            shadow = false,
                                            crop = true,
                                            roundedCorners = false,
                                            blur = false,
                                            greyscale = AlbumArtPreferences.isGreyscaleEnabled(),
                                            darken = false)
                        },
                        canceller = { iv ->
                            Glide.with(iv).clear(iv)
                        }
                ).also { imagePageAdapter = it },
        )
        val initialPosition = MediaPlaybackManager.getCurrentSongPosition()
        pager.setCurrentItem(initialPosition, smoothScroll = false)
        count.text = buildString {
            append(initialPosition + 1)
            append("/")
            append(MediaPlaybackManager.getSongs().size)
        }
        pager.addOnPageChangeListener(object : FelicityPager.OnPageChangeListener {
            override fun onPageSelected(position: Int, fromUser: Boolean) {
                super.onPageSelected(position, fromUser)
                if (fromUser) {
                    MediaPlaybackManager.updatePosition(position)
                }
            }
        })
        mediaControls.setMediaControlListener(object : FelicityMediaControls.Companion.MediaControlListener {
            override fun onPreviousClick() {
                MediaPlaybackManager.previous()
            }
            override fun onNextClick() {
                MediaPlaybackManager.next()
            }
            override fun onPlayClick() {
                val baseActivity = activity as? com.appinitdev.musicpro.extensions.activities.BaseActivity
                if (baseActivity != null && !MediaPlaybackManager.isPlaying()
                        && baseActivity.mediaController == null) {
                    baseActivity.ensureServiceRunning {
                        MediaPlaybackManager.flipState()
                    }
                } else {
                    MediaPlaybackManager.flipState()
                }
            }
            override fun onForwardStep() {
                MediaPlaybackManager.seekRelative(SEEK_PER_PULSE_MS)
            }
            override fun onRewindStep() {
                MediaPlaybackManager.seekRelative(SEEK_PER_PULSE_MS.toNegative())
            }
        })
        queue.setOnClickListener {
            openFragment(PlayingQueue.newInstance(), PlayingQueue.TAG)
        }
        count.setOnClickListener {
            queue.callOnClick()
        }
        search.setOnClickListener {
            openFragment(Search.newInstance(), Search.TAG)
        }
        menu.setOnClickListener {
            when (UserInterfacePreferences.getPlayerInterface()) {
                UserInterfacePreferences.PLAYER_INTERFACE_DEFAULT -> {
                    openSongsMenu(
                            audios = MediaPlaybackManager.getSongs(),
                            position = MediaPlaybackManager.getCurrentSongPosition(),
                            imageView = pager.getCurrentImageView(),
                            showBookmarks = true
                    )
                }
                UserInterfacePreferences.PLAYER_INTERFACE_FADED -> {
                    openSongsMenu(
                            audios = MediaPlaybackManager.getSongs(),
                            position = MediaPlaybackManager.getCurrentSongPosition(),
                            showBookmarks = true,
                            imageView = null 
                    )
                }
            }
        }
        repeat.setOnClickListener {
            val current = PlayerPreferences.getRepeatMode()
            val next = when (current) {
                MediaConstants.REPEAT_OFF -> MediaConstants.REPEAT_QUEUE
                MediaConstants.REPEAT_QUEUE -> MediaConstants.REPEAT_ONE
                else -> MediaConstants.REPEAT_OFF
            }
            PlayerPreferences.setRepeatMode(next)
            updateRepeatButtonIcon(next)
        }
        pcmInfo.setOnClickListener {
            if (AudioPreferences.isHiresOutputEnabled().not()) {
                showAudioPipeline(anchorView = pcmInfo)
            } else {
                showWarning("All processors are disabled. PCM info is not available in 32-bit output mode.")
            }
        }
        pcmInfo.setOnLongClickListener {
            PlayerPreferences.setPcmInfoMode((PlayerPreferences.getPcmInfoMode() + 1) % 3)
            true
        }
        viewLifecycleOwner.lifecycleScope.launch {
            MediaPlaybackManager.repeatModeFlow.collect { repeatMode ->
                updateRepeatButtonIcon(repeatMode)
            }
        }
        updateRepeatButtonIcon(PlayerPreferences.getRepeatMode())
        seekbar.setOnSeekListener(object : WaveformSeekbar.OnSeekListener {
            override fun onSeekEnd(positionMs: Long) {
                MediaPlaybackManager.seekTo(positionMs)
            }
        })
        seekbar.setOnFlingEndListener { positionMs ->
            MediaPlaybackManager.seekTo(positionMs)
        }
        seekbar.setOnBarTapListener { positionMs ->
            openBookmarkMenu(positionMs)
        }
        seekbar.setLeftLabelProvider { progress, _, _ ->
            DateUtils.formatElapsedTime(progress / 1000L)
        }
        seekbar.setRightLabelProvider { _, _, max ->
            DateUtils.formatElapsedTime(max / 1000L)
        }
        waveformViewModel.getWaveformData().observe(viewLifecycleOwner) { amplitudes ->
            seekbar.setAmplitudes(amplitudes)
        }
        lyrics.setOnClickListener {
            openFragment(Lyrics.newInstance(), Lyrics.TAG)
        }
        favorite.setOnClickListener {
            toggleFavorite()
        }
        equalizer.setOnClickListener {
            if (AudioPreferences.isHiresOutputEnabled().not()) {
                openFragment(Equalizer.newInstance(), Equalizer.TAG)
            } else {
                showWarning("All processors are disabled. EQ is not available in 32-bit output mode.")
            }
        }
        visualizerButton.setOnClickListener {
            if (AudioPreferences.isHiresOutputEnabled().not()) {
                childFragmentManager.showVisualizerConfig()
            } else {
                showWarning("All processors are disabled. Visualizers are not available in 32-bit output mode.")
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            lyricsViewModel.lrcData.collect { lrcData ->
                if (lrcData == null || lrcData.isEmpty) {
                    Log.d(TAG, "No lyrics found for the current song.")
                } else {
                    Log.d(TAG, "Loaded lyrics with ${lrcData.size()} lines.")
                    lrc.setLrcData(
                            lrcData, MediaPlaybackManager.getSeekPosition() + lyricsViewModel.syncOffset)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            bookmarksViewModel.bookmarks.collect { bookmarkList ->
                seekbar.bookmarks = bookmarkList.map { it.timestampMs }.toSet()
            }
        }
    }
    override fun getShuffleButton(): HighlightTextView? {
        return shuffle
    }
    override fun onShuffleClicked() {
        super.onShuffleClicked()
        shuffleMediaItems(songs = MediaPlaybackManager.getSongs())
    }
    private fun setVisualizerState() {
        if (PlayerPreferences.isVisualizerEnabled()) {
            VisualizerManager.processor?.setDirectOutput(
                    visualizer.bufferA,
                    visualizer.bufferB,
                    visualizer.isBufferAFront,
                    visualizer
            )
        } else {
            VisualizerManager.processor?.clearDirectOutput()
        }
        visualizer.visibility = if (PlayerPreferences.isVisualizerEnabled()) View.VISIBLE else View.GONE
    }
    private fun updateState() {
        val audio = MediaPlaybackManager.getCurrentSong() ?: return
        lastLoadedAudioId = audio.id
        title.text = audio.getProperTitle()
        artist.text = audio.getProperArtists()
        album.text = audio.getProperAlbum()
        pcmInfo.text = PcmInfoFormatter.formatPcmInfo(audio)
        seekbar.setDuration(audio.duration)
        seekbar.setProgress(MediaPlaybackManager.getSeekPosition(), animate = false)
        updatePlayButtonState(MediaPlaybackManager.isPlaying())
        updateFavoriteIcon(audio)
        loadWaveformWhenReady(audio)
    }
    private fun updatePlayButtonState(isPlaying: Boolean) {
        mediaControls.setPlaying(isPlaying)
    }
    private fun updateRepeatButtonIcon(repeatMode: Int) {
        when (repeatMode) {
            MediaConstants.REPEAT_ONE -> {
                repeat.setImageResource(R.drawable.ic_repeat_one)
                repeat.alpha = 1f
            }
            MediaConstants.REPEAT_QUEUE -> {
                repeat.setImageResource(R.drawable.ic_repeat)
                repeat.alpha = 1f
            }
            else -> { 
                repeat.setImageResource(R.drawable.ic_repeat)
                repeat.alpha = 0.4f
            }
        }
    }
    private fun loadWaveformWhenReady(audio: Audio) {
        if (MediaPlaybackManager.isPlaying() || MediaPlaybackManager.isPlayerReady()) {
            waveformViewModel.loadWaveform(audio)
            pendingWaveformAudio = null
        } else {
            pendingWaveformAudio = audio
        }
    }
    override fun onDestroyView() {
        VisualizerManager.processor?.clearDirectOutput()
        super.onDestroyView()
        imagePageAdapter = null
    }
    override fun onSongListChanged(songs: List<Audio>) {
        super.onSongListChanged(songs)
        val adapter = imagePageAdapter ?: return
        val currentPos = MediaPlaybackManager.getCurrentSongPosition()
        adapter.updateCount(songs.size)
        pager.notifyDataSetChanged()
        pager.setCurrentItem(currentPos, smoothScroll = false)
        count.text = buildString {
            append(currentPos + 1)
            append("/")
            append(songs.size)
        }
    }
    override fun onPositionChanged(position: Int) {
        super.onPositionChanged(position)
        Log.i(LOG_TAG, "Position changed to $position")
        if (pager.currentScrollState != FelicityPager.SCROLL_STATE_DRAGGING) {
            if (pager.getCurrentItem() != position) {
                pager.setCurrentItem(position, true)
            }
        }
        count.text = buildString {
            append(position + 1)
            append("/")
            append(MediaPlaybackManager.getSongs().size)
        }
    }
    override fun onAudio(audio: Audio) {
        super.onAudio(audio)
        val forward = MediaPlaybackManager.lastNavigationDirection
        val isSameSong = audio.id == lastLoadedAudioId
        if (!isSameSong) {
            lastLoadedAudioId = audio.id
            title.setTextWithEffect(audio.getProperTitle(), forward)
            artist.setTextWithEffect(audio.getProperArtists(), forward, 50L)
            album.setTextWithEffect(audio.getProperAlbum(), forward, 100L)
            pcmInfo.text = PcmInfoFormatter.formatPcmInfo(audio)
            seekbar.setDurationWithReset(audio.duration)
            lrc.clear()
            lyricsViewModel.loadLrcData()
            bookmarksViewModel.loadBookmarks()
        }
        seekbar.setProgress(MediaPlaybackManager.getSeekPosition(), animate = true)
        updateFavoriteIcon(audio)
        loadWaveformWhenReady(audio)
    }
    override fun onSeekChanged(seek: Long) {
        super.onSeekChanged(seek)
        seekbar.setProgress(seek, animate = true)
        lrc.updateTime(seek + lyricsViewModel.syncOffset)
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
    override val wantsMiniPlayerVisible: Boolean
        get() = false
    private fun updateFavoriteIcon(audio: Audio) {
        favorite.setFavorite(audio.isFavorite, true)
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            PlayerPreferences.VISUALIZER_ENABLED -> {
                setVisualizerState()
            }
            PlayerPreferences.WAVEFORM_MODE -> {
                seekbar.waveformMode = PlayerPreferences.getWaveformMode()
            }
            PlayerPreferences.PCM_INFO_MODE -> {
                val audio = MediaPlaybackManager.getCurrentSong() ?: return
                pcmInfo.setTextWithFade(PcmInfoFormatter.formatPcmInfo(audio))
            }
        }
    }
    override fun onSongMenuBookmarksClicked(audio: Audio) {
        openBookmarkMenu(MediaPlaybackManager.getSeekPosition())
    }
    private fun openBookmarkMenu(timestampMs: Long) {
        val formattedTime = DateUtils.formatElapsedTime(timestampMs / 1000L)
        val currentBookmarks = bookmarksViewModel.bookmarks.value
        val onViewCreated: (DialogBookmarkMenuBinding) -> Unit = { binding ->
            binding.timestampLabel.text = getString(R.string.bookmark_at, formattedTime)
            val hasNearby = currentBookmarks.any { kotlin.math.abs(it.timestampMs - timestampMs) < 1_000L }
            binding.removeBookmark.alpha = if (hasNearby) 1f else 0.4f
            binding.removeBookmark.isEnabled = hasNearby
            binding.addBookmark.alpha = if (hasNearby) 0.4f else 1f
            binding.addBookmark.isEnabled = !hasNearby
        }
        val onDialogInflated: (DialogBookmarkMenuBinding, () -> Unit, () -> Unit) -> Unit = { binding, dismiss, _ ->
            binding.addBookmark.setOnClickListener {
                bookmarksViewModel.addBookmark(timestampMs) { added ->
                    if (!added) {
                        showWarning(getString(R.string.bookmark_too_close))
                    }
                }
                dismiss()
            }
            binding.removeBookmark.setOnClickListener {
                bookmarksViewModel.removeBookmarkNear(timestampMs)
                dismiss()
            }
            binding.viewBookmarks.setOnClickListener {
                dismiss()
                openBookmarksList()
            }
        }
        SimpleDialog.Builder(
                container = requireContainerView(),
                inflateBinding = DialogBookmarkMenuBinding::inflate)
            .onViewCreated(onViewCreated)
            .onDialogInflated(onDialogInflated)
            .onDismiss {   }
            .setWidthRatio(getDialogWidthRation())
            .build()
            .show()
    }
    private fun openBookmarksList() {
        val bookmarks = bookmarksViewModel.bookmarks.value.sortedBy { it.timestampMs }
        openBookmarksList(
                bookmarks = bookmarks,
                onTimestampClicked = { bookmark, dismiss ->
                    MediaPlaybackManager.seekTo(bookmark.timestampMs)
                    dismiss()
                },
                onDelete = { bookmark ->
                    bookmarksViewModel.removeBookmark(bookmark)
                }
        )
    }
    companion object {
        const val TAG = "BasePlayer"
        private const val LOG_TAG = "BasePlayerFragment"
    }
}
