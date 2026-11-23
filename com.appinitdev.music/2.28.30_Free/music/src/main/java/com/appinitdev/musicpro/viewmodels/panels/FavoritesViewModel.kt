
 






@HiltViewModel
class FavoritesViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository
) : WrappedViewModel(application) {

    private val _favorites = MutableStateFlow<List<Audio>>(emptyList())
    val favorites: StateFlow<List<Audio>> = _favorites.asStateFlow()

     
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
