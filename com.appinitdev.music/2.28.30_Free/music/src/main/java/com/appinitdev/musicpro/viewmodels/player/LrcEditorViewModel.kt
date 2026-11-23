@HiltViewModel(assistedFactory = LrcEditorViewModel.Factory::class)
class LrcEditorViewModel @AssistedInject constructor(
        application: Application,
        @Assisted val audio: Audio,
        private val lrcRepository: LrcRepository
) : WrappedViewModel(application) {
    private val _entries: MutableLiveData<MutableList<LrcEntryModel>> = MutableLiveData(mutableListOf())
    private val _seekPositionMs: MutableLiveData<Long> = MutableLiveData(0L)
    private val _durationMs: MutableLiveData<Long> = MutableLiveData(audio.duration)
    private val _isPlaying: MutableLiveData<Boolean> = MutableLiveData(false)
    private val _saved: MutableLiveData<Boolean> = MutableLiveData(false)
    private var localPlayer: ExoPlayer? = null
    private val seekHandler = Handler(Looper.getMainLooper())
    private val seekRunnable: Runnable = object : Runnable {
        override fun run() {
            localPlayer?.let { player ->
                _seekPositionMs.value = player.currentPosition.coerceAtLeast(0L)
            }
            seekHandler.postDelayed(this, SEEK_POLL_INTERVAL_MS)
        }
    }
    init {
        if (MediaPlaybackManager.isPlaying()) {
            MediaPlaybackManager.pause()
        }
        initLocalPlayer()
        loadLrcEntries()
    }
    fun getEntries(): LiveData<MutableList<LrcEntryModel>> = _entries
    fun getSeekPosition(): LiveData<Long> = _seekPositionMs
    fun getDuration(): LiveData<Long> = _durationMs
    fun isPlayingLiveData(): LiveData<Boolean> = _isPlaying
    fun getSaved(): LiveData<Boolean> = _saved
    val currentPositionMs: Long
        get() = localPlayer?.currentPosition?.coerceAtLeast(0L) ?: 0L
    fun play() {
        localPlayer?.play()
    }
    fun pause() {
        localPlayer?.pause()
    }
    fun flipPlayback() {
        if (localPlayer?.isPlaying == true) pause() else play()
    }
    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _durationMs.value ?: 0L)
        localPlayer?.seekTo(clamped)
        _seekPositionMs.value = clamped
    }
    fun seekRelative(deltaMs: Long) {
        seekTo(currentPositionMs + deltaMs)
    }
    fun stampTimestamp(index: Int) {
        val list = _entries.value ?: return
        if (index !in list.indices) return
        list[index] = list[index].copy(timestampMs = currentPositionMs)
        _entries.value = list
    }
    fun removeEntry(index: Int) {
        val list = _entries.value ?: return
        if (index !in list.indices) return
        list.removeAt(index)
        _entries.value = list
    }
    fun addEmptyEntry() {
        val list = _entries.value ?: mutableListOf()
        list.add(LrcEntryModel(timestampMs = 0L, text = ""))
        _entries.value = list
    }
    fun pasteLines(text: String) {
        val list = _entries.value ?: mutableListOf()
        text.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { line -> list.add(LrcEntryModel(timestampMs = 0L, text = line)) }
        _entries.value = list
    }
    fun saveLrc() {
        val entries = _entries.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lrcData = LrcData()
                entries.sortedBy { it.timestampMs }.forEach { model ->
                    lrcData.addEntry(LrcEntry(model.timestampMs, model.text))
                }
                val result = lrcRepository.saveLrcToFile(lrcData.toLrcString(), audio.uri)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        _saved.value = true
                        Log.d(TAG, "LRC saved to ${result.getOrNull()?.path}")
                    } else {
                        Log.e(TAG, "Failed to save LRC", result.exceptionOrNull())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while saving LRC", e)
            }
        }
    }
    private fun initLocalPlayer() {
        localPlayer = ExoPlayer.Builder(getApplication()).build().also { player ->
            player.setMediaItem(MediaItem.fromUri(audio.uri.toUri()))
            player.prepare()
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val dur = player.duration.coerceAtLeast(audio.duration)
                        _durationMs.postValue(dur)
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.postValue(isPlaying)
                    if (isPlaying) startSeekPolling() else stopSeekPolling()
                }
            })
        }
    }
    private fun loadLrcEntries() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = lrcRepository.loadLrcFromFile(audio.uri)
            val lrcContent = result.getOrNull()
            if (!lrcContent.isNullOrBlank()) {
                try {
                    val lrcData = LrcParser().parse(lrcContent)
                    val models = lrcData.getEntries()
                        .map { entry -> LrcEntryModel(entry.timeInMillis, entry.text) }
                        .toMutableList()
                    _entries.postValue(models)
                } catch (e: LyricsParseException) {
                    Log.w(TAG, "Could not parse existing LRC file; starting with empty list.", e)
                    _entries.postValue(mutableListOf())
                }
            } else {
                _entries.postValue(mutableListOf())
            }
        }
    }
    private fun startSeekPolling() {
        seekHandler.removeCallbacks(seekRunnable)
        seekHandler.post(seekRunnable)
    }
    private fun stopSeekPolling() {
        seekHandler.removeCallbacks(seekRunnable)
    }
    override fun onCleared() {
        super.onCleared()
        stopSeekPolling()
        localPlayer?.release()
        localPlayer = null
    }
    @AssistedFactory
    interface Factory {
        fun create(audio: Audio): LrcEditorViewModel
    }
    companion object {
        private const val TAG = "LrcEditorViewModel"
        private const val SEEK_POLL_INTERVAL_MS = 100L
    }
}
