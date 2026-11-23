
@HiltViewModel(assistedFactory = YearViewerViewModel.Factory::class)
class YearViewerViewModel @AssistedInject constructor(
        @Assisted private val yearGroup: YearGroup,
        private val audioRepository: AudioRepository,
) : ViewModel() {

    private val _data = MutableStateFlow<PageData?>(null)
    val data: StateFlow<PageData?> = _data.asStateFlow()

    /** Raw unsorted songs fetched from the repository. Re-sorting does not require a DB trip. */
    private var rawSongs: List<Audio> = emptyList()

    init {
        loadYearData()
    }

    private fun loadYearData() {
        viewModelScope.launch {
            audioRepository.getYearPageData(yearGroup)
                .catch { exception ->
                    Log.e(TAG, "Error loading year data for: ${yearGroup.year}", exception)
                    emit(PageData())
                }
                .flowOn(Dispatchers.IO)
                .collect { pageData ->
                    Log.d(TAG, "loadYearData: Loaded ${pageData.songs.size} songs for year: ${yearGroup.year}")
                    rawSongs = pageData.songs
                    _data.value = pageData.copy(songs = rawSongs.sortedForYearPage())
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
            _data.value = current.copy(songs = rawSongs.sortedForYearPage())
            Log.d(TAG, "resort: re-sorted ${rawSongs.size} songs for year page")
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(yearGroup: YearGroup): YearViewerViewModel
    }

    companion object {
        private const val TAG = "YearViewerViewModel"
    }
}
