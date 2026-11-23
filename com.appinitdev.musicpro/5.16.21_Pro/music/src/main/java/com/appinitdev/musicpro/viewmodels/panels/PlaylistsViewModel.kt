
 












@HiltViewModel
class PlaylistsViewModel @Inject constructor(
        application: Application,
        private val playlistRepository: PlaylistRepository
) : WrappedViewModel(application) {

    private val _playlists = MutableStateFlow<List<PlaylistWithSongs>>(emptyList())
    val playlists: StateFlow<List<PlaylistWithSongs>> = _playlists.asStateFlow()

     
    private var rawList: List<PlaylistWithSongs> = emptyList()

    private var loadJob: Job? = null

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            playlistRepository.getAllPlaylistsWithSongs()
                .catch { e ->
                    Log.e(TAG, "Error loading playlists", e)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    rawList = list
                    _playlists.value = list.sorted()
                    Log.d(TAG, "Playlists loaded: ${list.size}")
                }
        }
    }

    private fun resort() {
        viewModelScope.launch(Dispatchers.Default) {
            _playlists.value = rawList.sorted()
        }
    }

     



    private fun List<PlaylistWithSongs>.sorted(): List<PlaylistWithSongs> {
        val ascending = PlaylistPreferences.getSortingStyle() == CommonPreferencesConstants.ASCENDING
        return when (PlaylistPreferences.getSongSort()) {
            CommonPreferencesConstants.BY_NAME ->
                if (ascending) sortedBy { it.playlist.name.lowercase() }
                else sortedByDescending { it.playlist.name.lowercase() }

            CommonPreferencesConstants.BY_DATE_ADDED ->
                if (ascending) sortedBy { it.playlist.dateCreated }
                else sortedByDescending { it.playlist.dateCreated }

            CommonPreferencesConstants.BY_DATE_MODIFIED ->
                if (ascending) sortedBy { it.playlist.dateModified }
                else sortedByDescending { it.playlist.dateModified }

            CommonPreferencesConstants.BY_NUMBER_OF_SONGS ->
                if (ascending) sortedBy { it.songs.size }
                else sortedByDescending { it.songs.size }

            else ->
                if (ascending) sortedBy { it.playlist.name.lowercase() }
                else sortedByDescending { it.playlist.name.lowercase() }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, s)
        when (s) {
            PlaylistPreferences.SONG_SORT,
            PlaylistPreferences.SORTING_STYLE -> resort()
        }
    }

    companion object {
        private const val TAG = "PlaylistsViewModel"
    }
}

