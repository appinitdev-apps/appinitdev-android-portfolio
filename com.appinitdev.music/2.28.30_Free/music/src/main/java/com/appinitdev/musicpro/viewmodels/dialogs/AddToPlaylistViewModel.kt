
 












@HiltViewModel(assistedFactory = AddToPlaylistViewModel.Factory::class)
class AddToPlaylistViewModel @AssistedInject constructor(
        @Assisted val audio: Audio,
        private val playlistRepository: PlaylistRepository
) : ViewModel() {

     






    data class PlaylistDialogState(
            val playlists: List<Playlist>,
            val preCheckedIds: Set<Long>,
            val songCounts: Map<Long, Int>
    )

    private val _state = MutableStateFlow<PlaylistDialogState?>(null)

     



    val state: StateFlow<PlaylistDialogState?> = _state.asStateFlow()

    private val _saveComplete = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

     



    val saveComplete: SharedFlow<Unit> = _saveComplete.asSharedFlow()

    init {
        observePlaylists()
    }

     




    private fun observePlaylists() {
        viewModelScope.launch {
            val audioHash = audio.hash
            playlistRepository.getAllPlaylistsWithSongs()
                .collect { playlistsWithSongs ->
                    val playlists = playlistsWithSongs.map { it.playlist }
                    val preCheckedIds = playlistsWithSongs
                        .filter { pws -> pws.songs.any { song -> song.hash == audioHash } }
                        .map { it.playlist.id }
                        .toSet()
                    val songCounts = playlistsWithSongs.associate { it.playlist.id to it.songs.size }
                    _state.emit(PlaylistDialogState(playlists, preCheckedIds, songCounts))
                }
        }
    }

     






    fun saveMembership(checkedIds: Set<Long>) {
        val audioHash = audio.hash
        viewModelScope.launch(Dispatchers.IO) {
            val allPlaylists = playlistRepository.getAllPlaylists().first()
            allPlaylists.forEach { playlist ->
                val wasIn = playlistRepository.isSongInPlaylist(playlist.id, audioHash)
                val isNowIn = checkedIds.contains(playlist.id)
                when {
                    !wasIn && isNowIn -> playlistRepository.addSong(playlist.id, audioHash)
                    wasIn && !isNowIn -> playlistRepository.removeSong(playlist.id, audioHash)
                }
            }
            _saveComplete.emit(Unit)
        }
    }

     
    @AssistedFactory
    interface Factory {
         





        fun create(audio: Audio): AddToPlaylistViewModel
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "AddToPlaylistViewModel"
    }
}

