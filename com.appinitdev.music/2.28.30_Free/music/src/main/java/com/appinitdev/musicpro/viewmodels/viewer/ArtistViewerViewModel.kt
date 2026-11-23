
@HiltViewModel(assistedFactory = ArtistViewerViewModel.Factory::class)
class ArtistViewerViewModel @AssistedInject constructor(
        @Assisted private val artist: Artist,
        private val audioRepository: AudioRepository,
        private val musicBrainzRepository: MusicBrainzRepository
) : ViewModel() {

    private val _data = MutableStateFlow<PageData?>(null)
    val data: StateFlow<PageData?> = _data.asStateFlow()

    private val _artistInfo = MutableStateFlow<MusicBrainzArtistInfo?>(null)

     



    val artistInfo: StateFlow<MusicBrainzArtistInfo?> = _artistInfo.asStateFlow()

     
    private var rawSongs: List<Audio> = emptyList()

    init {
        loadArtistData()
        loadArtistInfo()
    }

    private fun loadArtistData() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            audioRepository.getArtistPageData(artist)
                .catch { exception ->
                    Log.e(TAG, "Error loading artist data", exception)
                    emit(PageData())
                }
                .flowOn(Dispatchers.IO)
                .collect { pageData ->
                    val loadTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "loadArtistData: Loaded data for artist: ${artist.name}")
                    Log.d(TAG, "  - Audios: ${pageData.songs.size}")
                    Log.d(TAG, "  - Albums: ${pageData.albums.size}")
                    Log.d(TAG, "  - Genres: ${pageData.genres.size}")
                    Log.d(TAG, "  - Load time: $loadTime ms")

                    rawSongs = pageData.songs
                    _data.value = pageData.copy(songs = rawSongs.sortedForArtistPage())
                }
        }
    }

     



    fun resort() {
        val current = _data.value ?: return
        viewModelScope.launch(Dispatchers.Default) {
            _data.value = current.copy(songs = rawSongs.sortedForArtistPage())
            Log.d(TAG, "resort: re-sorted ${rawSongs.size} songs for artist page")
        }
    }

     




    private fun loadArtistInfo() {
        if (!LibraryPreferences.isMusicBrainzEnabled()) {
            Log.d(TAG, "MusicBrainz is disabled in preferences, skipping fetch for: ${artist.name}")
            return
        }
        val name = artist.name ?: return
        viewModelScope.launch {
            val info = musicBrainzRepository.fetchArtistInfo(name)
            _artistInfo.value = info
            if (info != null) {
                Log.d(TAG, "Loaded MusicBrainz info for: $name (type=${info.type}, country=${info.country})")
            } else {
                Log.d(TAG, "No MusicBrainz info available for: $name")
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(artist: Artist): ArtistViewerViewModel
    }

    companion object {
        private const val TAG = "ArtistViewerViewModel"
    }
}