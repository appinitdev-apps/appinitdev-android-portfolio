
@HiltViewModel(assistedFactory = GenreViewerViewModel.Factory::class)
class GenreViewerViewModel @AssistedInject constructor(
        @Assisted private val genre: Genre,
        private val audioRepository: AudioRepository,
) : ViewModel() {

    private val _data = MutableStateFlow<PageData?>(null)
    val data: StateFlow<PageData?> = _data.asStateFlow()

    /** Raw unsorted songs fetched from the repository. Re-sorting does not require a DB trip. */
    private var rawSongs: List<Audio> = emptyList()

    init {
        loadGenreData()
    }

    private fun loadGenreData() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            audioRepository.getGenrePageData(genre)
                .catch { exception ->
                    Log.e(TAG, "Error loading genre data", exception)
                    emit(PageData())
                }
                .flowOn(Dispatchers.IO)
                .collect { pageData ->
                    val loadTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "loadGenreData: Loaded data for genre: ${genre.name}")
                    Log.d(TAG, "  - Audios: ${pageData.songs.size}")
                    Log.d(TAG, "  - Albums: ${pageData.albums.size}")
                    Log.d(TAG, "  - Artists: ${pageData.artists.size}")
                    Log.d(TAG, "  - Load time: $loadTime ms")

                    rawSongs = pageData.songs
                    _data.value = pageData.copy(songs = rawSongs.sortedForGenrePage())
                }
        }
    }

    /**
     * Re-sorts the cached song list using the current [com.appinitdev.musicpro.preferences.PagePreferences]
     * and re-emits [PageData] without hitting the database.
     */
    fun resort() {
        val current = _data.value ?: return
        viewModelScope.launch(Dispatchers.Default) {
            _data.value = current.copy(songs = rawSongs.sortedForGenrePage())
            Log.d(TAG, "resort: re-sorted ${rawSongs.size} songs for genre page")
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(genre: Genre): GenreViewerViewModel
    }

    companion object {
        private const val TAG = "GenreViewerViewModel"
    }
}