@HiltViewModel
class DashboardViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository,
        private val songStatRepository: SongStatRepository
) : WrappedViewModel(application) {
    data class LibraryStats(val trackCount: Int, val totalHours: Long)
    data class RecommendedSpanConfig(val spanCount: Int, val bigCellPositions: List<Int>)
    var recommendedHeight: Int = -1
    private val _recentlyPlayed = MutableStateFlow<List<Audio>>(emptyList())
    val recentlyPlayed: StateFlow<List<Audio>> = _recentlyPlayed.asStateFlow()
    private val _recentlyAdded = MutableStateFlow<List<Audio>>(emptyList())
    val recentlyAdded: StateFlow<List<Audio>> = _recentlyAdded.asStateFlow()
    private val _favorites = MutableStateFlow<List<Audio>>(emptyList())
    val favorites: StateFlow<List<Audio>> = _favorites.asStateFlow()
    private val _recommended = MutableStateFlow<List<Audio>?>(null)
    val recommended: StateFlow<List<Audio>?> = _recommended.asStateFlow()
    private val _libraryStats = MutableStateFlow<LibraryStats?>(null)
    val libraryStats: StateFlow<LibraryStats?> = _libraryStats.asStateFlow()
    private val _topArtists = MutableStateFlow<List<Artist>>(emptyList())
    val topArtists: StateFlow<List<Artist>> = _topArtists.asStateFlow()
    private val _topAlbums = MutableStateFlow<List<Album>>(emptyList())
    val topAlbums: StateFlow<List<Album>> = _topAlbums.asStateFlow()
    private val _recommendedSpanConfig = MutableStateFlow<RecommendedSpanConfig?>(null)
    val recommendedSpanConfig: StateFlow<RecommendedSpanConfig?> = _recommendedSpanConfig.asStateFlow()
    private val _recommendedRefreshTrigger = MutableStateFlow(0)
    private var recommendedJob: Job? = null
    fun getVisiblePanelPanels(): List<Panel> {
        val panels = mutableListOf<Panel>()
        panels.add(Panel(R.string.songs, R.drawable.ic_song_16dp))
        panels.add(Panel(R.string.albums, R.drawable.ic_album_16dp))
        panels.add(Panel(R.string.artists, R.drawable.ic_people_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_ALBUM_ARTISTS))
            panels.add(Panel(R.string.album_artists, R.drawable.ic_artist_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_COMPOSERS))
            panels.add(Panel(R.string.composers, R.drawable.ic_composer_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_GENRES))
            panels.add(Panel(R.string.genres, R.drawable.ic_piano_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_YEAR))
            panels.add(Panel(R.string.year, R.drawable.ic_date_range_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_PLAYLISTS))
            panels.add(Panel(R.string.playlists, R.drawable.ic_list_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_PLAYING_QUEUE))
            panels.add(Panel(R.string.playing_queue, R.drawable.ic_queue_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_ADDED))
            panels.add(Panel(R.string.recently_added, R.drawable.ic_recently_added_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_RECENTLY_PLAYED))
            panels.add(Panel(R.string.recently_played, R.drawable.ic_history_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_MOST_PLAYED))
            panels.add(Panel(R.string.most_played, R.drawable.ic_equalizer_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_BOOKMARKS))
            panels.add(Panel(R.string.bookmarks, R.drawable.ic_bookmark_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FAVORITES)) {
            panels.add(
                    if (UserInterfacePreferences.isLikeIconInsteadOfThumb()) {
                        Panel(R.string.favorites, R.drawable.ic_thumb_up_16dp)
                    } else {
                        Panel(R.string.favorites, R.drawable.ic_favorite_filled_16dp)
                    }
            )
        }
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FOLDERS))
            panels.add(Panel(R.string.folders, R.drawable.ic_folder_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_FOLDERS_HIERARCHY))
            panels.add(Panel(R.string.folders_hierarchy, R.drawable.ic_tree_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_VISIBLE_ALWAYS_SKIPPED))
            panels.add(Panel(R.string.always_skipped, R.drawable.ic_skip_16dp))
        if (UserInterfacePreferences.isPanelVisible(UserInterfacePreferences.PANEL_MOST_SKIPPED))
            panels.add(Panel(R.string.most_skipped, R.drawable.ic_skip_16dp))
        return panels
    }
    init {
        startRecentlyPlayedFlow()
        startRecentlyAddedFlow()
        startFavoritesFlow()
        startRecommendedFlow()
        startLibraryStatsFlow()
        startTopArtistsFlow()
        startTopAlbumsFlow()
    }
    private fun startRecentlyPlayedFlow() {
        viewModelScope.launch {
            songStatRepository.getRecentlyPlayed()
                .catch { exception ->
                    Log.e(TAG, "Error loading recently played songs", exception)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    _recentlyPlayed.value = list
                    Log.d(TAG, "recentlyPlayed updated: ${list.size} songs")
                }
        }
    }
    private fun startRecentlyAddedFlow() {
        viewModelScope.launch {
            audioRepository.getRecentAudio()
                .catch { exception ->
                    Log.e(TAG, "Error loading recently added songs", exception)
                    emit(mutableListOf())
                }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    _recentlyAdded.value = list
                    Log.d(TAG, "recentlyAdded updated: ${list.size} songs")
                }
        }
    }
    private fun startFavoritesFlow() {
        viewModelScope.launch {
            audioRepository.getFavoriteAudio()
                .catch { exception ->
                    Log.e(TAG, "Error loading favorites", exception)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { list ->
                    _favorites.value = list
                    Log.d(TAG, "favorites updated: ${list.size} songs")
                }
        }
    }
    private fun startLibraryStatsFlow() {
        viewModelScope.launch {
            audioRepository.getAllAudio()
                .catch { e -> Log.e(TAG, "Error computing library stats", e) }
                .flowOn(Dispatchers.IO)
                .collect { audioList ->
                    val totalMs = audioList.sumOf { it.duration }
                    val totalHours = TimeUnit.MILLISECONDS.toHours(totalMs)
                    _libraryStats.value = LibraryStats(
                            trackCount = audioList.size,
                            totalHours = totalHours
                    )
                    Log.d(TAG, "libraryStats: ${audioList.size} tracks, $totalHours hours")
                }
        }
    }
    private fun startTopArtistsFlow() {
        viewModelScope.launch {
            combine(
                    songStatRepository.getMostPlayed(),
                    audioRepository.getAllAudio()
            ) { mostPlayed, allAudio ->
                computeTopArtists(mostPlayed, allAudio)
            }
                .catch { e -> Log.e(TAG, "Error computing top artists", e) }
                .flowOn(Dispatchers.IO)
                .collect { artists ->
                    _topArtists.value = artists
                    Log.d(TAG, "topArtists updated: ${artists.size}")
                }
        }
    }
    private fun startTopAlbumsFlow() {
        viewModelScope.launch {
            combine(
                    songStatRepository.getMostPlayed(),
                    audioRepository.getAllAudio()
            ) { mostPlayed, allAudio ->
                computeTopAlbums(mostPlayed, allAudio)
            }
                .catch { e -> Log.e(TAG, "Error computing top albums", e) }
                .flowOn(Dispatchers.IO)
                .collect { albums ->
                    _topAlbums.value = albums
                    Log.d(TAG, "topAlbums updated: ${albums.size}")
                }
        }
    }
    private fun computeTopArtists(mostPlayed: List<Audio>, allAudio: List<Audio>): List<Artist> {
        val artistPlayCounts = mostPlayed
            .mapNotNull { it.artist }
            .groupingBy { it }
            .eachCount()
        return artistPlayCounts.entries
            .sortedByDescending { it.value }
            .take(TOP_LIST_COUNT)
            .mapNotNull { (artistName, _) ->
                val artistSongs = allAudio.filter {
                    it.artist?.equals(artistName, ignoreCase = true) == true
                }
                if (artistSongs.isEmpty()) return@mapNotNull null
                Artist(
                        id = artistName.hashCode().toLong(),
                        name = artistName,
                        albumCount = artistSongs.mapNotNull { it.album }.distinct().size,
                        trackCount = artistSongs.size,
                        songPaths = artistSongs.map { it.uri }
                )
            }
    }
    private fun computeTopAlbums(mostPlayed: List<Audio>, allAudio: List<Audio>): List<Album> {
        val albumPlayCounts = mostPlayed
            .mapNotNull { it.album }
            .groupingBy { it }
            .eachCount()
        return albumPlayCounts.entries
            .sortedByDescending { it.value }
            .take(TOP_LIST_COUNT)
            .mapNotNull { (albumName, _) ->
                val albumSongs = allAudio.filter {
                    it.album?.equals(albumName, ignoreCase = true) == true
                }
                if (albumSongs.isEmpty()) return@mapNotNull null
                val firstSong = albumSongs.first()
                Album(
                        id = albumName.hashCode().toLong(),
                        name = albumName,
                        artist = firstSong.artist,
                        artistId = firstSong.artist?.hashCode()?.toLong() ?: 0L,
                        songCount = albumSongs.size,
                        songPaths = albumSongs.map { it.uri }
                )
            }
    }
    private fun startRecommendedFlow() {
        recommendedJob?.cancel()
        recommendedJob = viewModelScope.launch(Dispatchers.IO) {
            combine(
                    songStatRepository.getMostPlayed(),
                    songStatRepository.getRecentlyPlayed(),
                    audioRepository.getAllAudio(),
                    _recommendedRefreshTrigger
            ) { mostPlayed, recentlyPlayed, allAudio, _ ->
                computeRecommended(mostPlayed, recentlyPlayed, allAudio)
            }
                .catch { e -> Log.e(TAG, "Error loading recommended section", e) }
                .collect { songs ->
                    if (songs.isNotEmpty()) {
                        _recommended.value = songs
                        _recommendedSpanConfig.value = buildPortraitSpanConfig()
                        Log.d(TAG, "recommended updated: ${songs.size} songs")
                    }
                }
        }
    }
    private fun computeRecommended(
            mostPlayedList: List<Audio>,
            recentlyPlayedList: List<Audio>,
            allAudio: List<Audio>
    ): List<Audio> {
        val mostPlayed = mostPlayedList.take(RECOMMENDED_MOST_PLAYED_COUNT)
        val mostPlayedIds = mostPlayed.map { it.id }.toHashSet()
        val recentlyPlayed = recentlyPlayedList
            .filterNot { it.id in mostPlayedIds }
            .take(RECOMMENDED_RECENTLY_PLAYED_COUNT)
        val composed = (mostPlayed + recentlyPlayed).distinctBy { it.id }
        return if (composed.size >= RECOMMENDED_MAX_COUNT) {
            composed.shuffled().take(RECOMMENDED_MAX_COUNT)
        } else {
            val existingIds = composed.map { it.id }.toHashSet()
            val filler = allAudio
                .filterNot { it.id in existingIds }
                .take(RECOMMENDED_MAX_COUNT - composed.size)
            (composed + filler).shuffled()
        }
    }
    private fun buildPortraitSpanConfig(): RecommendedSpanConfig {
        val validPatterns = listOf(
                listOf(0, 4),
                listOf(1, 4),
                listOf(1, 3),
                listOf(0, 3)
        )
        return RecommendedSpanConfig(
                spanCount = RECOMMENDED_PORTRAIT_SPAN_COUNT,
                bigCellPositions = validPatterns.random()
        )
    }
    fun getSpanConfigForOrientation(orientation: Int): RecommendedSpanConfig {
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            RecommendedSpanConfig(
                    spanCount = RECOMMENDED_LANDSCAPE_SPAN_COUNT,
                    bigCellPositions = emptyList()
            )
        } else {
            _recommendedSpanConfig.value ?: buildPortraitSpanConfig()
        }
    }
    fun refreshRecommended() {
        _recommendedRefreshTrigger.value++
    }
    companion object {
        private const val TAG = "DashboardViewModel"
        private const val RECOMMENDED_MAX_COUNT = 6
        private const val RECOMMENDED_MOST_PLAYED_COUNT = 4
        private const val RECOMMENDED_RECENTLY_PLAYED_COUNT = 2
        private const val RECOMMENDED_PORTRAIT_SPAN_COUNT = 3
        private const val RECOMMENDED_LANDSCAPE_SPAN_COUNT = 5
        private const val TOP_LIST_COUNT = 10
    }
}
