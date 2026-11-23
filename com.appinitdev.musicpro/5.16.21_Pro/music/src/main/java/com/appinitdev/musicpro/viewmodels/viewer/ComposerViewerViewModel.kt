
 







@HiltViewModel(assistedFactory = ComposerViewerViewModel.Factory::class)
class ComposerViewerViewModel @AssistedInject constructor(
        @Assisted private val composer: Artist,
        private val audioRepository: AudioRepository,
        private val musicBrainzRepository: MusicBrainzRepository
) : ViewModel() {

    private val _data = MutableStateFlow<PageData?>(null)
    val data: StateFlow<PageData?> = _data.asStateFlow()

    private val _artistInfo = MutableStateFlow<MusicBrainzArtistInfo?>(null)

     




    val artistInfo: StateFlow<MusicBrainzArtistInfo?> = _artistInfo.asStateFlow()

     
    private var rawSongs: List<Audio> = emptyList()

    init {
        loadComposerData()
        loadArtistInfo()
    }

    private fun loadComposerData() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            audioRepository.getComposerPageData(composer)
                .catch { exception ->
                    Log.e(TAG, "Error loading composer data", exception)
                    emit(PageData())
                }
                .flowOn(Dispatchers.IO)
                .collect { pageData ->
                    val loadTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "loadComposerData: Loaded data for composer: ${composer.name}")
                    Log.d(TAG, "  - Songs: ${pageData.songs.size}")
                    Log.d(TAG, "  - Albums: ${pageData.albums.size}")
                    Log.d(TAG, "  - Genres: ${pageData.genres.size}")
                    Log.d(TAG, "  - Load time: $loadTime ms")

                    rawSongs = pageData.songs
                    _data.value = pageData.copy(songs = rawSongs.sortedForComposerPage())
                }
        }
    }

     



    fun resort() {
        val current = _data.value ?: return
        viewModelScope.launch(Dispatchers.Default) {
            _data.value = current.copy(songs = rawSongs.sortedForComposerPage())
            Log.d(TAG, "resort: re-sorted ${rawSongs.size} songs for composer page")
        }
    }

     




    private fun loadArtistInfo() {
        if (!LibraryPreferences.isMusicBrainzEnabled()) {
            Log.d(TAG, "MusicBrainz is disabled, skipping fetch for: ${composer.name}")
            return
        }
        val name = composer.name ?: return
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
        fun create(composer: Artist): ComposerViewerViewModel
    }

    companion object {
        private const val TAG = "ComposerViewerViewModel"
    }
}
