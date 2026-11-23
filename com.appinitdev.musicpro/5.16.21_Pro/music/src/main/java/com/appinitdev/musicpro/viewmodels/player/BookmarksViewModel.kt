@HiltViewModel
class BookmarksViewModel @Inject constructor(
        application: Application,
        private val bookmarksManager: BookmarksManager
) : WrappedViewModel(application) {
    val bookmarks: StateFlow<List<AudioBookmark>> = bookmarksManager.bookmarks
    fun currentTimestamps(): Set<Long> = bookmarksManager.currentTimestamps()
    fun addBookmark(timestampMs: Long, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val added = bookmarksManager.addBookmark(timestampMs)
            onResult?.invoke(added)
        }
    }
    fun removeBookmarkNear(timestampMs: Long) {
        viewModelScope.launch {
            bookmarksManager.removeBookmarkNear(timestampMs)
        }
    }
    fun removeBookmark(bookmark: AudioBookmark) {
        viewModelScope.launch {
            bookmarksManager.removeBookmark(bookmark)
        }
    }
    fun clearBookmarks() {
        viewModelScope.launch {
            bookmarksManager.clearBookmarks()
        }
    }
    fun loadBookmarks() = bookmarksManager.loadBookmarks()
}
