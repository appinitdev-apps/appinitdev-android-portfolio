
/**
 * Powers the Playback Info dialog by fetching and exposing stat data for a
 * given song. It also handles clearing those stats when the user decides to
 * give a song a clean slate.
 *
 * @author Hamza417
 */
@HiltViewModel(assistedFactory = PlaybackInfoViewModel.Factory::class)
class PlaybackInfoViewModel @AssistedInject constructor(
        application: Application,
        @Assisted private val audio: Audio,
        private val songStatRepository: SongStatRepository
) : AndroidViewModel(application) {

    /**
     * Holds the current stat snapshot. Starts as null until the first load
     * completes — think of it as waiting for the scoreboard to light up.
     */
    private val _stat = MutableStateFlow<AudioStat?>(null)
    val stat: StateFlow<AudioStat?> = _stat.asStateFlow()

    init {
        loadStat()
    }

    /**
     * Fetches the playback stat for the song from the database and pushes
     * it into the state flow so the UI can react to it.
     */
    private fun loadStat() {
        viewModelScope.launch(Dispatchers.IO) {
            _stat.emit(songStatRepository.getStatByHash(audio.hash))
        }
    }

    /**
     * Wipes all the tracked stats for this song — play count, skip count,
     * and last played are all reset. After clearing, the state is updated
     * to reflect the now-empty record (null, since the row was deleted).
     */
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

