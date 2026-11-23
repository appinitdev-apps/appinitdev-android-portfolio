
 














@HiltViewModel(assistedFactory = PlaylistSongsViewModel.Factory::class)
class PlaylistSongsViewModel @AssistedInject constructor(
        @Assisted val initialPlaylist: Playlist,
        private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Audio>>(emptyList())

     
    val songs: StateFlow<List<Audio>> = _songs.asStateFlow()

    private val _currentPlaylist = MutableStateFlow(initialPlaylist)

     
    val currentPlaylist: StateFlow<Playlist> = _currentPlaylist.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                    playlistRepository.getPlaylistByIdFlow(initialPlaylist.id),
                    playlistRepository.getSongsInPlaylistOrdered(initialPlaylist.id)
            ) { updatedPlaylist, rawSongs ->
                Pair(updatedPlaylist ?: initialPlaylist, rawSongs)
            }
                .catch { e -> Log.e(TAG, "Error loading playlist songs", e) }
                .flowOn(Dispatchers.IO)
                .collect { (playlist, rawSongs) ->
                    _currentPlaylist.value = playlist
                    _songs.value = rawSongs.sortedByPlaylist(playlist)
                    Log.d(TAG, "Loaded ${rawSongs.size} songs for '${playlist.name}'")
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

     
    @AssistedFactory
    interface Factory {
        fun create(initialPlaylist: Playlist): PlaylistSongsViewModel
    }

    companion object {
        private const val TAG = "PlaylistSongsViewModel"
    }
}

