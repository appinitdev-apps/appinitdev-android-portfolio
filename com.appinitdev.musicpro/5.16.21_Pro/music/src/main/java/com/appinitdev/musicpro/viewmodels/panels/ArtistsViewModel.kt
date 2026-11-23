
@HiltViewModel
class ArtistsViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository
) : WrappedViewModel(application) {

    private val _artists = MutableStateFlow<MutableList<Artist>>(mutableListOf())
    val artists: StateFlow<MutableList<Artist>> = _artists.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            audioRepository.getAllArtistsWithAggregation()
                .map { artistList -> artistList.sorted() }
                .distinctUntilChanged()  
                .catch { exception ->
                    Log.e(TAG, "Error loading artists", exception)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { sortedArtists ->
                    try {
                        _artists.value = sortedArtists as MutableList<Artist>
                    } catch (e: ClassCastException) {
                        Log.e(TAG, "Error casting sorted artists to MutableList", e)
                        _artists.value = sortedArtists.toMutableList()
                    }

                    Log.d(TAG, "loadData: ${sortedArtists.size} artists loaded")
                }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            ArtistPreferences.ARTIST_SORT,
            ArtistPreferences.SORTING_STYLE,
            LibraryPreferences.MINIMUM_AUDIO_SIZE,
            LibraryPreferences.MINIMUM_AUDIO_LENGTH -> {
                loadData()
            }
        }
    }

    companion object {
        private const val TAG = "ArtistsViewModel"
    }
}