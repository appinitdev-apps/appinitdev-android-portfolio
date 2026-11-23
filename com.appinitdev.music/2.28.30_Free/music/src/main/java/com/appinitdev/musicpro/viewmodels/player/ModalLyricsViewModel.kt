
@HiltViewModel(assistedFactory = ModalLyricsViewModel.Factory::class)
class ModalLyricsViewModel @AssistedInject constructor(
        application: Application,
        @Assisted private val audio: Audio?,
        private val lrcRepository: LrcRepository
) : WrappedViewModel(application) {

    private val lrcData: MutableLiveData<LrcData> by lazy {
        MutableLiveData<LrcData>().also {
            loadLrcData()
        }
    }

     



    private val _loadingStatus = MutableLiveData<LyricsLoadingStatus>(LyricsLoadingStatus.Idle)

     




    private var bakedLrcData: LrcData? = null

     







    private var loadingJob: Job? = null

     






    @Volatile
    private var lastLoadedPath: String? = null

     







    private val syncOffsetMs = MutableLiveData(0L)

     
    var syncOffset: Long = 0L

     
    private var pendingSyncDeltaMs: Long = 0L

     
    private val syncSaveHandler = Handler(Looper.getMainLooper())
    private val syncSaveRunnable = Runnable { persistSyncAdjustment() }

    fun getLrcData(): LiveData<LrcData> = lrcData

     
    fun getLoadingStatus(): LiveData<LyricsLoadingStatus> = _loadingStatus

     
    fun getSyncOffsetMs(): LiveData<Long> = syncOffsetMs

     






    private fun selectParser(content: String): ILyricsParser = when {
        WordLrcParser().canParse(content) -> WordLrcParser()
        LrcParser().canParse(content) -> LrcParser()
        else -> TxtParser()
    }

    fun loadLrcData() {
        val currentSongPath = (audio ?: MediaPlaybackManager.getCurrentSong())?.uri

        if (currentSongPath != null && currentSongPath == lastLoadedPath) {
            if (loadingJob?.isActive == true) {
                
                Log.d(TAG, "loadLrcData() skipped – job still in progress for same path.")
                return
            }
            if (lrcData.value != null) {
                
                Log.d(TAG, "loadLrcData() skipped – data already available for same path.")
                return
            }
        }

        
        loadingJob?.cancel()
        lastLoadedPath = currentSongPath

        
        syncOffset = 0L
        pendingSyncDeltaMs = 0L
        syncOffsetMs.value = 0L
        bakedLrcData = null
        syncSaveHandler.removeCallbacks(syncSaveRunnable)

        loadingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentSong = audio ?: MediaPlaybackManager.getCurrentSong()
                if (currentSong == null) {
                    lrcData.postValue(LrcData())
                    return@launch
                }

                val loadResult = lrcRepository.loadLrcFromFile(currentSong.uri)

                loadResult.onSuccess { lrcContent ->
                    if (lrcContent != null) {
                        Log.d(TAG, "Existing LRC file found for ${currentSong.title}, loading lyrics.")
                        try {
                            val lrcDataLoaded = selectParser(lrcContent).parse(lrcContent)
                            lrcData.postValue(lrcDataLoaded)
                        } catch (e: LyricsParseException) {
                            e.printStackTrace()
                            lrcData.postValue(LrcData())
                        }
                    } else {
                        Log.d(TAG, "No existing LRC file found for ${currentSong.title}, checking for TXT sidecar.")
                        val txtResult = lrcRepository.loadTxtFromFile(currentSong.uri)
                        val txtContent = txtResult.getOrNull()
                        if (!txtContent.isNullOrBlank()) {
                            Log.d(TAG, "TXT sidecar found for ${currentSong.title}, loading plain-text lyrics.")
                            try {
                                val txtLrcData = TxtParser().parse(txtContent)
                                lrcData.postValue(txtLrcData)
                            } catch (e: LyricsParseException) {
                                e.printStackTrace()
                                lrcData.postValue(LrcData())
                            }
                        } else {
                            Log.d(TAG, "No TXT sidecar found for ${currentSong.title}, attempting to fetch automatically.")
                            fetchAndSaveLrc(
                                    trackName = currentSong.title ?: currentSong.name,
                                    artistName = currentSong.artist ?: "",
                                    audioPath = currentSong.uri
                            )
                        }
                    }
                }.onFailure { exception ->
                    exception.printStackTrace()
                    lrcData.postValue(LrcData())
                }
            } finally {
                
                
                
            }
        }
    }

    private suspend fun fetchAndSaveLrc(trackName: String, artistName: String, audioPath: String) {
        
        withContext(Dispatchers.Main) { _loadingStatus.value = LyricsLoadingStatus.Searching(trackName) }

        val searchResult = lrcRepository.searchLyrics(trackName, artistName)

        searchResult.onSuccess { results ->
            val bestMatch = results.firstOrNull()
            val syncedLyrics = bestMatch?.syncedLyrics
            if (bestMatch != null && !syncedLyrics.isNullOrBlank()) {
                withContext(Dispatchers.Main) { _loadingStatus.value = LyricsLoadingStatus.Downloading(trackName) }
                try {
                    val lrcDataLoaded = withContext(Dispatchers.Default) {
                        LrcParser().parse(syncedLyrics)
                    }

                    lrcRepository.saveLrcToFile(syncedLyrics, audioPath)
                    Log.d(TAG, "Fetched and saved synced lyrics for $trackName by $artistName")
                    lrcData.postValue(lrcDataLoaded)
                } catch (e: LyricsParseException) {
                    e.printStackTrace()
                    lrcData.postValue(LrcData())
                }
            } else {
                lrcData.postValue(LrcData())
            }
        }.onFailure { exception ->
            exception.printStackTrace()
            lrcData.postValue(LrcData())
        }

        
        withContext(Dispatchers.Main) { _loadingStatus.value = LyricsLoadingStatus.Idle }
    }

    fun reloadLrcData() {
        loadingJob?.cancel()
        loadingJob = null
        lastLoadedPath = null 
        loadLrcData()
    }

     











    fun seekBy(deltaMs: Long) {
        val current = lrcData.value
        if (current == null || current.isEmpty) return

        
        pendingSyncDeltaMs += deltaMs
        syncOffsetMs.value = pendingSyncDeltaMs

        
        syncSaveHandler.removeCallbacks(syncSaveRunnable)
        syncSaveHandler.postDelayed(syncSaveRunnable, SYNC_SAVE_DEBOUNCE_MS)
    }

     







    private fun persistSyncAdjustment() {
        val delta = pendingSyncDeltaMs
        if (delta == 0L) return

        
        
        val base = bakedLrcData ?: lrcData.value ?: return
        val currentSong = audio ?: MediaPlaybackManager.getCurrentSong() ?: return

        
        
        
        val baked = base.shiftTimestamps(-delta)

        viewModelScope.launch(Dispatchers.IO) {
            val result = lrcRepository.saveLrcToFile(baked.toLrcString(), currentSong.uri)
            result.onSuccess {
                Log.d(TAG, "Sync ${delta}ms baked and saved to ${it.path}")
            }.onFailure {
                Log.e(TAG, "Failed to persist sync adjustment", it)
            }
        }

        
        bakedLrcData = baked
        pendingSyncDeltaMs = 0L

        
        
        syncOffsetMs.value = 0L
    }

    override fun onCleared() {
        super.onCleared()
        
        syncSaveHandler.removeCallbacks(syncSaveRunnable)
        persistSyncAdjustment()
    }

    fun deleteLrc(onSuccess: (() -> Unit)? = null) {
        val currentSong = audio ?: MediaPlaybackManager.getCurrentSong() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            lrcRepository.deleteLrcFile(currentSong.uri)

            withContext(Dispatchers.Main) {
                
                lrcData.value = LrcData()
                
                pendingSyncDeltaMs = 0L
                syncOffsetMs.value = 0L
                bakedLrcData = null
                syncSaveHandler.removeCallbacks(syncSaveRunnable)

                onSuccess?.invoke()
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(audio: Audio?): ModalLyricsViewModel
    }

    companion object {
        private const val TAG = "LyricsViewModel"
        private const val SYNC_SAVE_DEBOUNCE_MS = 1500L
    }
}
