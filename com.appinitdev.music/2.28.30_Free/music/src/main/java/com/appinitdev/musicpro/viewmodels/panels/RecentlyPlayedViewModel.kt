
 








@HiltViewModel
class RecentlyPlayedViewModel @Inject constructor(
        application: Application,
        private val songStatRepository: SongStatRepository
) : WrappedViewModel(application) {

    private val _songs = MutableStateFlow<List<AudioWithStat>>(emptyList())
    val songs: StateFlow<List<AudioWithStat>> = _songs.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            songStatRepository.getRecentlyPlayedWithStat()
                .catch { e ->
                    Log.e(TAG, "Error loading recently played songs", e)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    _songs.value = list
                    Log.d(TAG, "loadData: ${list.size} recently played songs loaded")
                }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            LibraryPreferences.MINIMUM_AUDIO_SIZE,
            LibraryPreferences.MINIMUM_AUDIO_LENGTH -> loadData()
        }
    }

    companion object {
        private const val TAG = "RecentlyPlayedViewModel"
    }
}
