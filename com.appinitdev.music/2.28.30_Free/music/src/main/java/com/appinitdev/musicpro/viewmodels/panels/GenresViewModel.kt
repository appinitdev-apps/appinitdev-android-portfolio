
@HiltViewModel
class GenresViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository
) : WrappedViewModel(application) {

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            audioRepository.getAllGenresWithAggregation()
                .map { genreList -> genreList.sorted() }
                .distinctUntilChanged()  
                .catch { exception ->
                    Log.e(TAG, "Error loading genres", exception)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { sortedGenres ->
                    _genres.value = sortedGenres
                    Log.d(TAG, "loadData: ${sortedGenres.size} genres loaded")
                }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            GenresPreferences.GENRE_SORT_STYLE,
            GenresPreferences.SORT_ORDER -> {
                Log.d(TAG, "onSharedPreferenceChanged: Sorting order changed, updating genres list")
                loadData()
            }
        }
    }

    companion object {
        private const val TAG = "GenresViewModel"
    }
}