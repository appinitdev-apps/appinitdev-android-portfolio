
 









@HiltViewModel
class AlwaysSkippedViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository
) : WrappedViewModel(application) {

    private val _skippedSongs = MutableStateFlow<List<Audio>>(emptyList())

     
    val skippedSongs: StateFlow<List<Audio>> = _skippedSongs.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadSkippedSongs()
    }

    private fun loadSkippedSongs() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            audioRepository.getAlwaysSkippedAudio()
                .catch { e ->
                    Log.e(TAG, "Oops — failed to load always-skipped songs", e)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    _skippedSongs.value = list
                    Log.d(TAG, "Always-skipped songs updated: ${list.size} songs in the hall of shame")
                }
        }
    }

    companion object {
        private const val TAG = "AlwaysSkippedViewModel"
    }
}

