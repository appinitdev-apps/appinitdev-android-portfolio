@HiltViewModel(assistedFactory = PlaylistViewerViewModel.Factory::class)
class PlaylistViewerViewModel @AssistedInject constructor(
        @Assisted val playlist: Playlist,
        private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val _data = MutableStateFlow<PageData?>(null)
    val data: StateFlow<PageData?> = _data.asStateFlow()
    private val _currentPlaylist = MutableStateFlow(playlist)
    val currentPlaylist: StateFlow<Playlist> = _currentPlaylist.asStateFlow()
    init {
        loadPlaylistData()
    }
    private fun loadPlaylistData() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            combine(
                    playlistRepository.getPlaylistByIdFlow(playlist.id),
                    playlistRepository.getPlaylistPageData(playlist)
            ) { updatedPlaylist, pageData ->
                Pair(updatedPlaylist ?: playlist, pageData)
            }
                .catch { e -> Log.e(TAG, "Error loading playlist page data", e) }
                .flowOn(Dispatchers.IO)
                .collect { (currentPlaylist, pageData) ->
                    val loadTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "loadPlaylistData: '${currentPlaylist.name}', sort=${currentPlaylist.sortOrder}")
                    Log.d(TAG, "  - Songs: ${pageData.songs.size}, load time: $loadTime ms")
                    _currentPlaylist.value = currentPlaylist
                    _data.value = pageData.copy(songs = pageData.songs.sortedByPlaylist(currentPlaylist))
                }
        }
    }
    private fun List<Audio>.sortedByPlaylist(playlist: Playlist): List<Audio> {
        if (playlist.sortOrder == -1) return this
        val asc = playlist.sortStyle == CommonPreferencesConstants.ASCENDING
        return when (playlist.sortOrder) {
            CommonPreferencesConstants.BY_TITLE ->
                if (asc) sortedBy { it.title?.lowercase() } else sortedByDescending { it.title?.lowercase() }
            CommonPreferencesConstants.BY_ARTIST ->
                if (asc) sortedBy { it.artist?.lowercase() } else sortedByDescending { it.artist?.lowercase() }
            CommonPreferencesConstants.BY_ALBUM ->
                if (asc) sortedBy { it.album?.lowercase() } else sortedByDescending { it.album?.lowercase() }
            CommonPreferencesConstants.BY_DURATION ->
                if (asc) sortedBy { it.duration } else sortedByDescending { it.duration }
            CommonPreferencesConstants.BY_DATE_ADDED ->
                if (asc) sortedBy { it.dateAdded } else sortedByDescending { it.dateAdded }
            else -> this
        }
    }
    fun resort() = Unit
    @AssistedFactory
    interface Factory {
        fun create(playlist: Playlist): PlaylistViewerViewModel
    }
    companion object {
        private const val TAG = "PlaylistViewerViewModel"
    }
}
