
 









@HiltViewModel(assistedFactory = AddMultipleToPlaylistViewModel.Factory::class)
class AddMultipleToPlaylistViewModel @AssistedInject constructor(
        @Assisted val audioHashes: LongArray,
        private val playlistRepository: PlaylistRepository
) : ViewModel() {

     





    data class State(
            val playlists: List<Playlist>,
            val songCounts: Map<Long, Int>
    )

    private val _state = MutableStateFlow<State?>(null)

     



    val state: StateFlow<State?> = _state.asStateFlow()

    private val _saveComplete = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

     



    val saveComplete: SharedFlow<Unit> = _saveComplete.asSharedFlow()

    init {
        observePlaylists()
    }

     



    private fun observePlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllPlaylistsWithSongs().collect { list ->
                val playlists = list.map { it.playlist }
                val songCounts = list.associate { it.playlist.id to it.songs.size }
                _state.emit(State(playlists, songCounts))
            }
        }
    }

     





    fun saveToPlaylists(checkedPlaylistIds: Set<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val hashes = audioHashes.toList()
            checkedPlaylistIds.forEach { playlistId ->
                playlistRepository.addSongs(playlistId, hashes)
            }
            _saveComplete.emit(Unit)
        }
    }

    @AssistedFactory
    interface Factory {
         




        fun create(audioHashes: LongArray): AddMultipleToPlaylistViewModel
    }
}
