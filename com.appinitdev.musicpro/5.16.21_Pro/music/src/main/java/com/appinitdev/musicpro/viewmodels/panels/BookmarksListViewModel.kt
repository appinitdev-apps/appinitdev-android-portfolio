
/**
 * ViewModel for the Bookmarks panel.
 *
 * Keeps a live [StateFlow] of every audio track that has at least one bookmark,
 * with each track's full bookmark list attached. The list updates automatically
 * whenever a bookmark is added or removed anywhere in the app.
 *
 * @author Hamza417
 */
@HiltViewModel
class BookmarksListViewModel @Inject constructor(
        application: Application,
        private val bookmarkRepository: BookmarkRepository
) : WrappedViewModel(application) {

    private val _bookmarks = MutableStateFlow<List<AudioWithBookmarks>>(emptyList())

    /**
     * Live list of all audio tracks that have at least one bookmark, sorted by title.
     * Each item pairs an [com.appinitdev.musicpro.repository.models.Audio] with its ordered
     * bookmark list so the adapter can render both levels without extra queries.
     */
    val bookmarks: StateFlow<List<AudioWithBookmarks>> = _bookmarks.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            bookmarkRepository.getAllWithAudio()
                .flowOn(Dispatchers.IO)
                .catch { /* silently ignore errors rather than crashing the panel */ }
                .collect { list -> _bookmarks.value = list }
        }
    }

    /** Removes all bookmarks for the track identified by [audioHash]. */
    fun clearBookmarks(audioHash: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkRepository.clearBookmarks(audioHash)
        }
    }

    /** Removes a single bookmark entry. */
    fun deleteBookmark(bookmark: com.appinitdev.musicpro.repository.models.AudioBookmark) {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkRepository.removeBookmark(bookmark)
        }
    }
}

