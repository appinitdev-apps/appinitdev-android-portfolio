@HiltViewModel
class HomeViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository,
        private val songStatRepository: SongStatRepository
) : WrappedViewModel(application) {
    private val _favorites = MutableStateFlow<List<Audio>>(emptyList())
    val favorites: StateFlow<List<Audio>> = _favorites.asStateFlow()
    private val _recentlyPlayed = MutableStateFlow<List<Audio>>(emptyList())
    val recentlyPlayed: StateFlow<List<Audio>> = _recentlyPlayed.asStateFlow()
    private val _mostPlayed = MutableStateFlow<List<Audio>>(emptyList())
    val mostPlayed: StateFlow<List<Audio>> = _mostPlayed.asStateFlow()
    private val _recentlyAdded = MutableStateFlow<List<Audio>>(emptyList())
    val recentlyAdded: StateFlow<List<Audio>> = _recentlyAdded.asStateFlow()
    private val _recommended = MutableStateFlow<List<Audio>>(emptyList())
    val recommended: StateFlow<List<Audio>> = _recommended.asStateFlow()
    private val _libraryStats = MutableStateFlow<Pair<Int, Long>?>(null)
    val libraryStats: StateFlow<Pair<Int, Long>?> = _libraryStats.asStateFlow()
    private var recommendedJob: Job? = null
    private val _recommendedRefreshTrigger = MutableStateFlow(0)
    private var favoritesJob: Job? = null
    private var recentlyPlayedJob: Job? = null
    private var mostPlayedJob: Job? = null
    private var recentlyAddedJob: Job? = null
    init {
        startFavoritesFlow()
        startRecentlyPlayedFlow()
        startMostPlayedFlow()
        startRecentlyAddedFlow()
        startRecommendedFlow()
        startLibraryStatsFlow()
    }
    private fun startFavoritesFlow() {
        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            audioRepository.getFavoriteAudio()
                .catch { e -> Log.e(TAG, "Error loading favorites", e); emit(emptyList()) }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    _favorites.value = list.take(TAKE_COUNT)
                    Log.d(TAG, "favorites updated: ${list.size} songs")
                }
        }
    }
    private fun startRecentlyPlayedFlow() {
        recentlyPlayedJob?.cancel()
        recentlyPlayedJob = viewModelScope.launch {
            songStatRepository.getRecentlyPlayed()
                .catch { e -> Log.e(TAG, "Error loading recently played", e); emit(emptyList()) }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    _recentlyPlayed.value = list.take(TAKE_COUNT)
                    Log.d(TAG, "recentlyPlayed updated: ${list.size} songs")
                }
        }
    }
    private fun startMostPlayedFlow() {
        mostPlayedJob?.cancel()
        mostPlayedJob = viewModelScope.launch {
            songStatRepository.getMostPlayed()
                .catch { e -> Log.e(TAG, "Error loading most played", e); emit(emptyList()) }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    _mostPlayed.value = list.take(TAKE_COUNT)
                    Log.d(TAG, "mostPlayed updated: ${list.size} songs")
                }
        }
    }
    private fun startRecentlyAddedFlow() {
        recentlyAddedJob?.cancel()
        recentlyAddedJob = viewModelScope.launch {
            audioRepository.getRecentAudio()
                .catch { e -> Log.e(TAG, "Error loading recently added", e); emit(mutableListOf()) }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    _recentlyAdded.value = list.take(TAKE_COUNT)
                    Log.d(TAG, "recentlyAdded updated: ${list.size} songs")
                }
        }
    }
    private fun startRecommendedFlow() {
        recommendedJob?.cancel()
        recommendedJob = viewModelScope.launch(Dispatchers.IO) {
            combine(
                    songStatRepository.getMostPlayed(),
                    songStatRepository.getRecentlyPlayed(),
                    audioRepository.getAllAudio(),
                    _recommendedRefreshTrigger
            ) { mostPlayed, recentlyPlayed, allAudio, _ ->
                computeRecommended(mostPlayed, recentlyPlayed, allAudio)
            }
                .catch { e -> Log.e(TAG, "Error loading recommended section", e) }
                .collect { songs ->
                    if (songs.isNotEmpty()) {
                        _recommended.value = songs
                        Log.d(TAG, "recommended updated: ${songs.size} songs")
                    }
                }
        }
    }
    private fun computeRecommended(
            mostPlayedList: List<Audio>,
            recentlyPlayedList: List<Audio>,
            allAudio: List<Audio>
    ): List<Audio> {
        val mostPlayed = mostPlayedList.take(RECOMMENDED_MOST_PLAYED_COUNT)
        val mostPlayedIds = mostPlayed.map { it.id }.toHashSet()
        val recentlyPlayed = recentlyPlayedList
            .filterNot { it.id in mostPlayedIds }
            .take(RECOMMENDED_RECENTLY_PLAYED_COUNT)
        val composed = (mostPlayed + recentlyPlayed).distinctBy { it.id }
        return if (composed.size >= RECOMMENDED_MAX_COUNT) {
            composed.shuffled().take(RECOMMENDED_MAX_COUNT)
        } else {
            val existingIds = composed.map { it.id }.toHashSet()
            val filler = allAudio
                .filterNot { it.id in existingIds }
                .take(RECOMMENDED_MAX_COUNT - composed.size)
            (composed + filler).shuffled()
        }
    }
    private fun startLibraryStatsFlow() {
        viewModelScope.launch {
            audioRepository.getAllAudio()
                .catch { e -> Log.e(TAG, "Error computing library stats", e) }
                .flowOn(Dispatchers.IO)
                .collect { audioList ->
                    val totalMs = audioList.sumOf { it.duration }
                    _libraryStats.value = Pair(audioList.size, totalMs)
                    Log.d(TAG, "libraryStats updated: ${audioList.size} tracks")
                }
        }
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            LibraryPreferences.MINIMUM_AUDIO_SIZE,
            LibraryPreferences.MINIMUM_AUDIO_LENGTH -> {
                Log.d(TAG, "onSharedPreferenceChanged: Relevant preference changed, restarting flows")
                startFavoritesFlow()
                startRecentlyPlayedFlow()
                startMostPlayedFlow()
                startRecentlyAddedFlow()
            }
        }
    }
    companion object {
        private const val TAG = "HomeViewModel"
        private const val TAKE_COUNT = 18
        private const val RECOMMENDED_MAX_COUNT = 30
        private const val RECOMMENDED_MOST_PLAYED_COUNT = 8
        private const val RECOMMENDED_RECENTLY_PLAYED_COUNT = 6
    }
}