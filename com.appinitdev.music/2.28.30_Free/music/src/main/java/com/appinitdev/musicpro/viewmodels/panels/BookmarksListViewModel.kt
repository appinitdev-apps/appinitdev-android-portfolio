@HiltViewModel
class BookmarksListViewModel @Inject constructor(
        application: Application,
        private val bookmarkRepository: BookmarkRepository
) : WrappedViewModel(application) {
    private val _bookmarks = MutableStateFlow<List<AudioWithBookmarks>>(emptyList())
    val bookmarks: StateFlow<List<AudioWithBookmarks>> = _bookmarks.asStateFlow()
    init {
        observe()
    }
    private fun observe() {
        viewModelScope.launch {
            bookmarkRepository.getAllWithAudio()
                .flowOn(Dispatchers.IO)
                .catch {   }
                .collect { list -> _bookmarks.value = list }
        }
    }
    fun clearBookmarks(audioHash: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkRepository.clearBookmarks(audioHash)
        }
    }
    fun deleteBookmark(bookmark: com.appinitdev.musicpro.repository.models.AudioBookmark) {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkRepository.removeBookmark(bookmark)
        }
    }
}
