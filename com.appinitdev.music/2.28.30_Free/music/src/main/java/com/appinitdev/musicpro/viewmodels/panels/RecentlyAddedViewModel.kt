@HiltViewModel
class RecentlyAddedViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository
) : WrappedViewModel(application) {
    private val _songs = MutableStateFlow<List<Audio>>(emptyList())
    val songs: StateFlow<List<Audio>> = _songs.asStateFlow()
    init {
        loadData()
    }
    private fun loadData() {
        viewModelScope.launch {
            audioRepository.getRecentAudio()
                .catch { exception ->
                    Log.e(TAG, "Error loading recently added songs", exception)
                    emit(mutableListOf())
                }
                .flowOn(Dispatchers.IO)
                .collect { audioList ->
                    _songs.value = audioList
                    Log.d(TAG, "loadData: ${audioList.size} recently added songs loaded")
                }
        }
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            LibraryPreferences.MINIMUM_AUDIO_SIZE,
            LibraryPreferences.MINIMUM_AUDIO_LENGTH -> {
                loadData()
            }
        }
    }
    companion object {
        private const val TAG = "RecentlyAddedViewModel"
    }
}
