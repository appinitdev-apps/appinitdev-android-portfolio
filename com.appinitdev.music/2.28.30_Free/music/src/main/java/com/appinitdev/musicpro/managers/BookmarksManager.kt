@Singleton
class BookmarksManager @Inject constructor(
        private val bookmarkRepository: BookmarkRepository
) {
    private val TAG = "BookmarksManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _bookmarks = MutableStateFlow<List<AudioBookmark>>(emptyList())
    val bookmarks: StateFlow<List<AudioBookmark>> = _bookmarks.asStateFlow()
    @Volatile
    private var currentHash: Long? = null
    private var observeJob: Job? = null
    init {
        scope.launch {
            MediaPlaybackManager.songPositionFlow.collect {
                loadBookmarks()
            }
        }
    }
    fun loadBookmarks() {
        val currentSong = MediaPlaybackManager.getCurrentSong()
        if (currentSong == null) {
            currentHash = null
            _bookmarks.value = emptyList()
            return
        }
        val hash = currentSong.hash
        if (hash == currentHash && observeJob?.isActive == true) {
            Log.d(TAG, "loadBookmarks() skipped – already watching hash $hash.")
            return
        }
        currentHash = hash
        observeJob?.cancel()
        observeJob = scope.launch(Dispatchers.IO) {
            Log.d(TAG, "Observing bookmarks for hash $hash (${currentSong.title}).")
            bookmarkRepository.getBookmarksFlow(hash).collect { list ->
                if (hash == currentHash) {
                    _bookmarks.value = list
                    Log.d(TAG, "Bookmarks updated: ${list.size} entries for hash $hash.")
                }
            }
        }
    }
    suspend fun addBookmark(timestampMs: Long): Boolean {
        val hash = currentHash ?: return false
        val added = bookmarkRepository.addBookmark(hash, timestampMs)
        if (!added) {
            Log.d(TAG, "Bookmark at ${timestampMs}ms rejected — too close to an existing one.")
        }
        return added
    }
    suspend fun removeBookmarkNear(timestampMs: Long) {
        val hash = currentHash ?: return
        bookmarkRepository.removeBookmarkNear(hash, timestampMs)
    }
    suspend fun removeBookmark(bookmark: AudioBookmark) {
        bookmarkRepository.removeBookmark(bookmark)
    }
    suspend fun clearBookmarks() {
        val hash = currentHash ?: return
        bookmarkRepository.clearBookmarks(hash)
    }
    fun currentTimestamps(): Set<Long> {
        return _bookmarks.value.map { it.timestampMs }.toSet()
    }
}
