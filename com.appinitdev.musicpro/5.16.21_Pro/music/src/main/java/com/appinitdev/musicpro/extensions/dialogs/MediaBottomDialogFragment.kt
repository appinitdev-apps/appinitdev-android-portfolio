abstract class MediaBottomDialogFragment : ScopedBottomSheetFragment() {
    private var lastSavedSeekPosition = 0L
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            MediaPlaybackManager.songSeekPositionFlow.collect { position ->
                onSeekChanged(position)
                val song = MediaPlaybackManager.getCurrentSong()
                if (song != null) {
                    val threshold = maxOf(5000L, song.duration / 20) 
                    if (abs(position - lastSavedSeekPosition) > threshold) {
                        lastSavedSeekPosition = position
                        saveCurrentPlaybackState()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                MediaPlaybackManager.songPositionFlow.collect { position ->
                    Log.d(TAG, "Song position: $position")
                    MediaPlaybackManager.getCurrentSong()?.let { song ->
                        onAudio(song)
                    }
                    onPositionChanged(position)
                    saveCurrentPlaybackState()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            MediaPlaybackManager.songListFlow.collect { songs ->
                Log.d(TAG, "Song list updated: ${songs.size} songs")
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            MediaPlaybackManager.playbackStateFlow.collect { state ->
                onPlaybackStateChanged(state)
            }
        }
    }
    private fun saveCurrentPlaybackState() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            PlaybackStateManager.saveCurrentPlaybackState(requireContext(), TAG)
        }
    }
    protected fun setMediaItems(songs: List<Audio>, position: Int = 0) {
        MediaPlaybackManager.play()
        createSongHistoryDatabase(songs)
    }
    private fun createSongHistoryDatabase(songs: List<Audio>) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val audioDatabase = AudioDatabase.getInstance(requireContext())
            PlaybackStateManager.savePlaybackState(
                    db = audioDatabase,
                    queueHash = songs.map { it.hash },
                    index = MediaPlaybackManager.getCurrentSongPosition(),
                    position = MediaPlaybackManager.getSeekPosition(),
                    shuffle = false,
                    repeat = 0
            )
        }
    }
    open fun onPlaybackStateChanged(state: Int) {
        Log.d(TAG, "Playback state changed: $state")
    }
    open fun onAudio(audio: Audio) {
        Log.d(TAG, "New song played: ${audio.title} by ${audio.artist}")
    }
    open fun onPositionChanged(position: Int) {
        Log.d(TAG, "Position changed: $position")
    }
    open fun onSeekChanged(seek: Long) {
    }
    companion object {
        private const val TAG = "MediaDialogFragment"
    }
}