
@HiltViewModel
class PlayingQueueViewModel @Inject constructor(
        application: Application) : WrappedViewModel(application) {

    private val _songs = MutableStateFlow<List<Audio>>(emptyList())
    val songs: StateFlow<List<Audio>> = _songs.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    init {
        observeQueue()
        observePosition()
    }

    private fun observeQueue() {
        viewModelScope.launch {
            MediaPlaybackManager.songListFlow.collect { audioList ->
                Log.d(TAG, "Queue updated: ${audioList.size} songs")
                _songs.value = audioList
            }
        }
    }

    private fun observePosition() {
        viewModelScope.launch {
            MediaPlaybackManager.songPositionFlow.collect { position ->
                Log.d(TAG, "Queue position updated: $position")
                _currentPosition.value = position
            }
        }
    }

    companion object {
        private const val TAG = "PlayingQueueViewModel"
    }
}

