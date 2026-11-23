@HiltViewModel(assistedFactory = FolderViewerViewModel.Factory::class)
class FolderViewerViewModel @AssistedInject constructor(
        @Assisted private val folder: Folder,
        private val audioRepository: AudioRepository,
) : ViewModel() {
    private val _data = MutableStateFlow<PageData?>(null)
    val data: StateFlow<PageData?> = _data.asStateFlow()
    private var rawSongs: List<Audio> = emptyList()
    init {
        loadFolderData()
    }
    private fun loadFolderData() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            audioRepository.getFolderPageData(folder)
                .catch { exception ->
                    Log.e(TAG, "Error loading folder data", exception)
                    emit(PageData())
                }
                .flowOn(Dispatchers.IO)
                .collect { pageData ->
                    val loadTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "loadFolderData: Loaded data for folder: ${folder.name}")
                    Log.d(TAG, "  - Songs:   ${pageData.songs.size}")
                    Log.d(TAG, "  - Albums:  ${pageData.albums.size}")
                    Log.d(TAG, "  - Artists: ${pageData.artists.size}")
                    Log.d(TAG, "  - Genres:  ${pageData.genres.size}")
                    Log.d(TAG, "  - Load time: $loadTime ms")
                    rawSongs = pageData.songs
                    _data.value = pageData.copy(songs = rawSongs.sortedForFolderPage())
                }
        }
    }
    fun resort() {
        val current = _data.value ?: return
        viewModelScope.launch(Dispatchers.Default) {
            _data.value = current.copy(songs = rawSongs.sortedForFolderPage())
            Log.d(TAG, "resort: re-sorted ${rawSongs.size} songs for folder page")
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(folder: Folder): FolderViewerViewModel
    }
    companion object {
        private const val TAG = "FolderViewerViewModel"
    }
}
