@HiltViewModel(assistedFactory = PlaybackInfoViewModel.Factory::class)
class PlaybackInfoViewModel @AssistedInject constructor(
        application: Application,
        @Assisted private val audio: Audio,
        private val songStatRepository: SongStatRepository
) : AndroidViewModel(application) {
    private val _stat = MutableStateFlow<AudioStat?>(null)
    val stat: StateFlow<AudioStat?> = _stat.asStateFlow()
    init {
        loadStat()
    }
    private fun loadStat() {
        viewModelScope.launch(Dispatchers.IO) {
            _stat.emit(songStatRepository.getStatByHash(audio.hash))
        }
    }
    fun clearStats() {
        viewModelScope.launch(Dispatchers.IO) {
            songStatRepository.clearStats(audio.hash)
            _stat.emit(null)
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(audio: Audio): PlaybackInfoViewModel
    }
    companion object {
        private const val TAG = "PlaybackInfoViewModel"
    }
}
