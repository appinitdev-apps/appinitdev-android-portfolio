
@HiltViewModel(assistedFactory = AlbumViewerViewModel.Factory::class)
class AlbumViewerViewModel @AssistedInject constructor(
        @Assisted private val album: Album,
        private val audioRepository: AudioRepository,
        private val musicBrainzRepository: MusicBrainzRepository
) : ViewModel() {

    private val _data = MutableStateFlow<PageData?>(null)
    val data: StateFlow<PageData?> = _data.asStateFlow()

    private val _albumInfo = MutableStateFlow<MusicBrainzAlbumInfo?>(null)

     




    val albumInfo: StateFlow<MusicBrainzAlbumInfo?> = _albumInfo.asStateFlow()

     
    private var rawSongs: List<Audio> = emptyList()

    init {
        loadAlbumData()
        loadAlbumInfo()
    }

    private fun loadAlbumData() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            audioRepository.getAlbumPageData(album)
                .catch { exception ->
                    Log.e(TAG, "Error loading album data", exception)
                    emit(PageData())
                }
                .flowOn(Dispatchers.IO)
                .collect { pageData ->
                    val loadTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "loadAlbumData: Loaded data for album: ${album.name}")
                    Log.d(TAG, "  - Audios: ${pageData.songs.size}")
                    Log.d(TAG, "  - Artists: ${pageData.artists.size}")
                    Log.d(TAG, "  - Genres: ${pageData.genres.size}")
                    Log.d(TAG, "  - Load time: $loadTime ms")

                    rawSongs = pageData.songs
                    _data.value = pageData.copy(songs = rawSongs.sortedForAlbumPage())
                }
        }
    }

     



    fun resort() {
        val current = _data.value ?: return
        viewModelScope.launch(Dispatchers.Default) {
            _data.value = current.copy(songs = rawSongs.sortedForAlbumPage())
            Log.d(TAG, "resort: re-sorted ${rawSongs.size} songs for album page")
        }
    }

     




    private fun loadAlbumInfo() {
        if (!LibraryPreferences.isMusicBrainzEnabled()) {
            Log.d(TAG, "MusicBrainz is disabled, skipping fetch for album: ${album.name}")
            return
        }
        val albumName = album.name ?: return
        val artistName = album.artist ?: ""
        viewModelScope.launch {
            val info = musicBrainzRepository.fetchAlbumInfo(albumName, artistName)
            _albumInfo.value = info
            if (info != null) {
                Log.d(TAG, "Loaded MusicBrainz info for album: $albumName (status=${info.status}, country=${info.country})")
            } else {
                Log.d(TAG, "No MusicBrainz info available for album: $albumName")
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(album: Album): AlbumViewerViewModel
    }

    companion object {
        private const val TAG = "AlbumViewerViewModel"
    }
}