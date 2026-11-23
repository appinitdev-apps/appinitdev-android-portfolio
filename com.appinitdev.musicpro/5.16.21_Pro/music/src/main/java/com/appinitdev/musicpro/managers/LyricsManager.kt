@Singleton
class LyricsManager @Inject constructor(
        private val lrcRepository: LrcRepository
) {
    private val TAG = "LyricsManager"
    private val EMPTY_LRC_DATA = LrcData().apply {
        addEntry(LrcEntry(0L, "No lyrics found for this song."))
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _lrcData = MutableStateFlow<LrcData?>(null)
    val lrcData: StateFlow<LrcData?> = _lrcData.asStateFlow()
    private val _loadingStatus = MutableStateFlow<LyricsLoadingStatus>(LyricsLoadingStatus.Idle)
    val loadingStatus: StateFlow<LyricsLoadingStatus> = _loadingStatus.asStateFlow()
    private val _syncOffsetMs = MutableStateFlow(0L)
    val syncOffsetMs: StateFlow<Long> = _syncOffsetMs.asStateFlow()
    var syncOffset: Long = 0L
        private set
    private var bakedLrcData: LrcData? = null
    private var pendingSyncDeltaMs: Long = 0L
    private val syncSaveHandler = Handler(Looper.getMainLooper())
    private val syncSaveRunnable = Runnable { persistSyncAdjustment() }
    @Volatile
    private var lastLoadedPath: String? = null
    private var loadingJob: Job? = null
    init {
        scope.launch {
            MediaPlaybackManager.songPositionFlow.collect {
                loadLrcData()
            }
        }
    }
    private fun selectParser(content: String): ILyricsParser = when {
        WordLrcParser().canParse(content) -> WordLrcParser()
        LrcParser().canParse(content) -> LrcParser()
        else -> TxtParser()
    }
    fun loadLrcData() {
        val currentSong = MediaPlaybackManager.getCurrentSong()
        val currentSongPath = currentSong?.uri
        if (currentSongPath != null && currentSongPath == lastLoadedPath) {
            if (loadingJob?.isActive == true) {
                Log.d(TAG, "loadLrcData() skipped – already loading '${currentSong.title}'.")
                return
            }
            if (_lrcData.value != null) {
                Log.d(TAG, "loadLrcData() skipped – lyrics already ready for '${currentSong.title}'.")
                return
            }
        }
        persistSyncAdjustment()
        loadingJob?.cancel()
        lastLoadedPath = currentSongPath
        syncOffset = 0L
        pendingSyncDeltaMs = 0L
        _syncOffsetMs.value = 0L
        bakedLrcData = null
        syncSaveHandler.removeCallbacks(syncSaveRunnable)
        if (currentSong == null) {
            _lrcData.value = LrcData()
            return
        }
        val path = currentSongPath ?: return
        loadingJob = scope.launch {
            try {
                doLoad(currentSong, path)
            } catch (e: Exception) {
                e.printStackTrace()
                emitIfStillRelevant(path, LrcData())
            }
        }
    }
    private suspend fun doLoad(song: Audio, songPath: String) {
        val lrcContent = withContext(Dispatchers.IO) {
            lrcRepository.loadLrcFromFile(song.uri).getOrNull()
        }
        if (!isStillRelevant(songPath)) return
        if (lrcContent != null) {
            Log.d(TAG, "LRC sidecar found for '${song.title}'.")
            parseThenEmit(lrcContent, songPath, fallback = LrcData()) { content ->
                selectParser(content).parse(content)
            }
            return
        }
        val txtContent = withContext(Dispatchers.IO) {
            lrcRepository.loadTxtFromFile(song.uri).getOrNull()
        }
        if (!isStillRelevant(songPath)) return
        if (!txtContent.isNullOrBlank()) {
            Log.d(TAG, "TXT sidecar found for '${song.title}'.")
            parseThenEmit(txtContent, songPath, fallback = LrcData()) { content ->
                TxtParser().parse(content)
            }
            return
        }
        if (LyricsPreferences.isAutoDownloadLyrics()) {
            Log.d(TAG, "No local lyrics for '${song.title}', fetching online.")
            emitIfStillRelevant(songPath, LrcData())
            fetchAndSaveLrc(
                    trackName = song.title ?: song.name,
                    artistName = song.artist ?: "",
                    audioPath = song.uri,
                    songPath = songPath
            )
        } else {
            Log.d(TAG, "No local lyrics and auto-download is off for '${song.title}'.")
            emitIfStillRelevant(songPath, LrcData())
        }
    }
    private suspend fun parseThenEmit(
            content: String,
            songPath: String,
            fallback: LrcData,
            parse: suspend (String) -> LrcData
    ) {
        try {
            val parsed = withContext(Dispatchers.Default) { parse(content) }
            emitIfStillRelevant(songPath, parsed)
        } catch (e: LyricsParseException) {
            e.printStackTrace()
            emitIfStillRelevant(songPath, fallback)
        }
    }
    private fun emitIfStillRelevant(songPath: String?, data: LrcData) {
        if (isStillRelevant(songPath)) {
            _lrcData.value = data
        } else {
            Log.d(TAG, "Discarding stale lyrics result for path='$songPath' (current='$lastLoadedPath').")
        }
    }
    private fun isStillRelevant(songPath: String?): Boolean = (songPath == lastLoadedPath)
    fun reloadLrcData() {
        loadingJob?.cancel()
        loadingJob = null
        lastLoadedPath = null
        loadLrcData()
    }
    fun refreshIfNoLyrics() {
        val currentLrc = _lrcData.value
        if (currentLrc != null && currentLrc.isEmpty) {
            reloadLrcData()
        }
    }
    private suspend fun fetchAndSaveLrc(
            trackName: String,
            artistName: String,
            audioPath: String,
            songPath: String
    ) {
        _loadingStatus.value = LyricsLoadingStatus.Searching(trackName)
        val result = lrcRepository.searchLyrics(trackName, artistName)
        if (!isStillRelevant(songPath)) {
            _loadingStatus.value = LyricsLoadingStatus.Idle
            return
        }
        result.onSuccess { results ->
            val syncedLyrics = results.firstOrNull()?.syncedLyrics
            if (!syncedLyrics.isNullOrBlank()) {
                _loadingStatus.value = LyricsLoadingStatus.Downloading(trackName)
                try {
                    val parsed = withContext(Dispatchers.Default) { LrcParser().parse(syncedLyrics) }
                    withContext(Dispatchers.IO) { lrcRepository.saveLrcToFile(syncedLyrics, audioPath) }
                    Log.d(TAG, "Downloaded and saved lyrics for '$trackName'.")
                    emitIfStillRelevant(songPath, parsed)
                } catch (e: LyricsParseException) {
                    e.printStackTrace()
                    emitIfStillRelevant(songPath, LrcData())
                }
            } else {
                Log.d(TAG, "No match found online for '$trackName'.")
                emitIfStillRelevant(songPath, LrcData())
            }
        }.onFailure { exception ->
            exception.printStackTrace()
            emitIfStillRelevant(songPath, LrcData())
        }
        _loadingStatus.value = LyricsLoadingStatus.Idle
    }
    fun seekBy(deltaMs: Long) {
        val current = _lrcData.value
        if (current == null || current.isEmpty) return
        pendingSyncDeltaMs += deltaMs
        syncOffset = pendingSyncDeltaMs
        _syncOffsetMs.value = pendingSyncDeltaMs
        syncSaveHandler.removeCallbacks(syncSaveRunnable)
        syncSaveHandler.postDelayed(syncSaveRunnable, SYNC_SAVE_DEBOUNCE_MS)
    }
    private fun persistSyncAdjustment() {
        val delta = pendingSyncDeltaMs
        if (delta == 0L) return
        val base = bakedLrcData ?: _lrcData.value ?: return
        val currentSong = MediaPlaybackManager.getCurrentSong() ?: return
        val baked = base.shiftTimestamps(-delta)
        scope.launch(Dispatchers.IO) {
            val result = lrcRepository.saveLrcToFile(baked.toLrcString(), currentSong.uri)
            result.onSuccess { Log.d(TAG, "Sync offset ${delta}ms baked and saved to ${it.path}.") }
                .onFailure { Log.e(TAG, "Failed to persist sync adjustment.", it) }
        }
        bakedLrcData = baked
        pendingSyncDeltaMs = 0L
        _lrcData.value = baked
        _syncOffsetMs.value = 0L
        syncOffset = 0L
    }
    fun deleteLrc(onSuccess: (() -> Unit)? = null) {
        val currentSong = MediaPlaybackManager.getCurrentSong() ?: return
        scope.launch {
            withContext(Dispatchers.IO) { lrcRepository.deleteLrcFile(currentSong.uri) }
            _lrcData.value = LrcData()
            pendingSyncDeltaMs = 0L
            syncOffset = 0L
            _syncOffsetMs.value = 0L
            bakedLrcData = null
            syncSaveHandler.removeCallbacks(syncSaveRunnable)
            onSuccess?.invoke()
        }
    }
    companion object {
        private const val SYNC_SAVE_DEBOUNCE_MS = 1500L
    }
}
sealed class LyricsLoadingStatus {
    data object Idle : LyricsLoadingStatus()
    data class Searching(val trackName: String) : LyricsLoadingStatus()
    data class Downloading(val trackName: String) : LyricsLoadingStatus()
}
