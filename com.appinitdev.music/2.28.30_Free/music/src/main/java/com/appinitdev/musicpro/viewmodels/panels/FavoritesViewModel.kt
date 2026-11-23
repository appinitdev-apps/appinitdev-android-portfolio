
/**
 * ViewModel for the Favorites panel.
 * Loads favorite songs from the repository and applies [FavoritesPreferences]-driven
 * sort/order without a round-trip to the database when only the sort changes.
 *
 * @author Hamza417
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository
) : WrappedViewModel(application) {

    private val _favorites = MutableStateFlow<List<Audio>>(emptyList())
    val favorites: StateFlow<List<Audio>> = _favorites.asStateFlow()

    /** Raw list from the database kept for cheap in-memory re-sorts. */
    private var rawFavoriteList: List<Audio> = emptyList()

    private var loadJob: Job? = null

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            audioRepository.getFavoriteAudio()
                .catch { e ->
                    Log.e(TAG, "Error loading favorites", e)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    rawFavoriteList = list
                    _favorites.value = list.sortedFavorites()
                    Log.d(TAG, "Favorites loaded: ${list.size} songs")
                }
        }
    }

    /**
     * Re-sorts the cached list without querying the database again.
     * Called when only the sort field or direction changes.
     */
    private fun resort() {
        viewModelScope.launch(Dispatchers.Default) {
            _favorites.value = rawFavoriteList.sortedFavorites()
            Log.d(TAG, "resort: ${_favorites.value.size} favorites re-sorted")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, s)
        when (s) {
            FavoritesPreferences.SONG_SORT,
            FavoritesPreferences.SORTING_STYLE -> resort()
        }
    }

    companion object {
        private const val TAG = "FavoritesViewModel"
    }
}
