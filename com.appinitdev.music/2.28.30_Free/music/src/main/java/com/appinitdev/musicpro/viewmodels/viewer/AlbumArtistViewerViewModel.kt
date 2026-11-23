@HiltViewModel(assistedFactory = AlbumArtistViewerViewModel.Factory::class)
class AlbumArtistViewerViewModel @AssistedInject constructor(
        @Assisted private val albumArtist: Artist,
        private val audioRepository: AudioRepository,
        private val musicBrainzRepository: MusicBrainzRepository
) : ViewModel() {
    private val _data = MutableStateFlow<PageData?>(null)
    val data: StateFlow<PageData?> = _data.asStateFlow()
    private val _artistInfo = MutableStateFlow<MusicBrainzArtistInfo?>(null)
    val artistInfo: StateFlow<MusicBrainzArtistInfo?> = _artistInfo.asStateFlow()
    private var rawSongs: List<Audio> = emptyList()
    init {
        loadAlbumArtistData()
        loadArtistInfo()
    }
    private fun loadAlbumArtistData() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            audioRepository.getAlbumArtistPageData(albumArtist)
                .catch { exception ->
                    Log.e(TAG, "Error loading album artist data", exception)
                    emit(PageData())
                }
                .flowOn(Dispatchers.IO)
                .collect { pageData ->
                    val loadTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "loadAlbumArtistData: Loaded data for album artist: ${albumArtist.name}")
                    Log.d(TAG, "  - Songs:  ${pageData.songs.size}")
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
            Log.d(TAG, "resort: re-sorted ${rawSongs.size} songs for album artist page")
        }
    }
    private fun loadArtistInfo() {
        if (!LibraryPreferences.isMusicBrainzEnabled()) {
            Log.d(TAG, "MusicBrainz is disabled, skipping fetch for: ${albumArtist.name}")
            return
        }
        val name = albumArtist.name ?: return
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
        fun create(albumArtist: Artist): AlbumArtistViewerViewModel
    }
    companion object {
        private const val TAG = "AlbumArtistViewerViewModel"
    }
}
