
@HiltViewModel
class AlbumsViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository
) : WrappedViewModel(application) {

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            audioRepository.getAllAlbumsWithAggregation()
                .map { albumList -> albumList.sorted() }
                .distinctUntilChanged()  // Prevent identical consecutive emissions
                .catch { exception ->
                    Log.e(TAG, "Error loading albums", exception)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { sortedAlbums ->
                    _albums.value = sortedAlbums
                    Log.d(TAG, "loadData: ${sortedAlbums.size} albums loaded")
                }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            AlbumPreferences.ALBUM_SORT,
            AlbumPreferences.SORTING_STYLE,
            LibraryPreferences.MINIMUM_AUDIO_SIZE,
            LibraryPreferences.MINIMUM_AUDIO_LENGTH -> {
                loadData()
            }
        }
    }

    companion object {
        private const val TAG = "AlbumsViewModel"
    }
}