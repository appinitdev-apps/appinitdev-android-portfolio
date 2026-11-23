@HiltViewModel
class SongsViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository) : WrappedViewModel(application) {
    private var carouselPosition = 0
    private val _songs = MutableStateFlow<List<Audio>>(emptyList())
    val songs: StateFlow<List<Audio>> = _songs.asStateFlow()
    private var rawAudioList: List<Audio> = emptyList()
    private var loadJob: Job? = null
    init {
        loadData()
    }
    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            audioRepository.getAllAudio()
                .catch { exception ->
                    Log.e(TAG, "Error loading songs", exception)
                    emit(mutableListOf())
                }
                .flowOn(Dispatchers.IO)
                .collect { audioList ->
                    rawAudioList = audioList
                    val sortedSongs = audioList.sorted()
                    _songs.value = sortedSongs
                    Log.d(TAG, "loadData: ${sortedSongs.size} songs loaded")
                }
        }
    }
    private fun resort() {
        viewModelScope.launch(Dispatchers.Default) {
            val sortedSongs = rawAudioList.sorted()
            _songs.value = sortedSongs
            Log.d(TAG, "resort: ${sortedSongs.size} songs re-sorted")
        }
    }
    fun setCarouselPosition(position: Int) {
        carouselPosition = position
    }
    fun getCarouselPosition(): Int {
        return carouselPosition
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, s)
        when (s) {
            SongsPreferences.SONG_SORT,
            SongsPreferences.SORTING_STYLE -> {
                resort()
            }
            LibraryPreferences.MINIMUM_AUDIO_SIZE,
            LibraryPreferences.MINIMUM_AUDIO_LENGTH -> {
                loadData()
            }
        }
    }
    companion object {
        private const val TAG = "SongsViewModel"
    }
}
