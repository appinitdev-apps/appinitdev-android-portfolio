@HiltViewModel(assistedFactory = MetadataSearchViewModel.Factory::class)
class MetadataSearchViewModel @AssistedInject constructor(
        application: Application,
        @Assisted val audio: Audio,
        private val savedStateHandle: SavedStateHandle,
        private val musicBrainzRepository: MusicBrainzRepository
) : WrappedViewModel(application) {
    private val _searchResults = MutableLiveData<List<MusicBrainzRecordingResult>>(emptyList())
    val searchResults: LiveData<List<MusicBrainzRecordingResult>> get() = _searchResults
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading
    val titleKeyword: String
        get() = savedStateHandle[KEY_TITLE] ?: (audio.title?.takeIf { it.isNotBlank() } ?: audio.name)
    val artistKeyword: String
        get() = savedStateHandle[KEY_ARTIST] ?: (audio.artist?.takeIf { it.isNotBlank() } ?: "")
    init {
        triggerAutoSearch()
    }
    private fun triggerAutoSearch() {
        performSearch(titleKeyword, artistKeyword)
    }
    fun setUserTitle(title: String) {
        savedStateHandle[KEY_TITLE] = title
    }
    fun setUserArtist(artist: String) {
        savedStateHandle[KEY_ARTIST] = artist
    }
    fun searchWithCurrentKeyword() {
        val title = titleKeyword.trim()
        if (title.isBlank()) {
            warning.postValue(getString(R.string.search))
            return
        }
        performSearch(title, artistKeyword.trim())
    }
    private fun performSearch(trackName: String, artistName: String) {
        if (trackName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            Log.d(TAG, "Searching MusicBrainz recordings: '$trackName' by '$artistName'")
            val results = musicBrainzRepository.searchRecordings(trackName, artistName)
            _searchResults.postValue(results)
            if (results.isEmpty()) {
                warning.postValue(getString(R.string.no_lyrics_found))
            }
            Log.d(TAG, "Recording search returned ${results.size} result(s)")
            _isLoading.postValue(false)
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(audio: Audio): MetadataSearchViewModel
    }
    companion object {
        private const val TAG = "MetadataSearchViewModel"
        private const val KEY_TITLE = "metadata_search_title"
        private const val KEY_ARTIST = "metadata_search_artist"
    }
}
