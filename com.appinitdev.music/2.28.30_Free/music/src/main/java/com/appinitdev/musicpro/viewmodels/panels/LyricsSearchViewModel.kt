
 













@HiltViewModel
class LyricsSearchViewModel @Inject constructor(
        application: Application,
        private val savedStateHandle: SavedStateHandle,
        private val lrcRepository: LrcRepository,
        private val lyricsManager: LyricsManager
) : WrappedViewModel(application) {

    private val _searchResults = MutableLiveData<List<LrcLibResponse>>(emptyList())

     
    val searchResults: LiveData<List<LrcLibResponse>> get() = _searchResults

    private val _isLoading = MutableLiveData(false)

     
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _lrcSaved = MutableLiveData<Boolean>()

     



    val lrcSaved: LiveData<Boolean> get() = _lrcSaved

     





    val keyword: String
        get() = savedStateHandle[KEY_KEYWORD] ?: buildDefaultKeyword()

     



    val isKeywordUserModified: Boolean
        get() = savedStateHandle.get<Boolean>(KEY_USER_MODIFIED) ?: false

    init {
        
        
        if (!isKeywordUserModified) {
            savedStateHandle[KEY_KEYWORD] = buildDefaultKeyword()
        }
        
        triggerAutoSearch()
    }

     



    private fun triggerAutoSearch() {
        val audio = MediaPlaybackManager.getCurrentSong()
        if (audio != null) {
            val title = audio.getProperTitle()
            val artist = audio.artist ?: ""
            performSearch(title, artist)
        } else {
            val fallback = keyword
            if (fallback.isNotBlank()) {
                performSearch(fallback, "")
            }
        }
    }

     





    fun setUserKeyword(query: String) {
        savedStateHandle[KEY_KEYWORD] = query
        savedStateHandle[KEY_USER_MODIFIED] = true
    }

     




    fun searchWithCurrentKeyword() {
        val query = keyword.trim()
        if (query.isBlank()) {
            warning.postValue(getString(R.string.search))
            return
        }
        performSearch(query, "")
    }

     






    private fun performSearch(trackName: String, artistName: String) {
        if (trackName.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            Log.d(TAG, "Searching for lyrics: trackName='$trackName', artistName='$artistName'")

            val result = lrcRepository.searchLyrics(trackName, artistName)

            result.onSuccess { results ->
                _searchResults.postValue(results)
                if (results.isEmpty()) {
                    warning.postValue(getString(R.string.no_lyrics_found))
                }
                Log.d(TAG, "Search returned ${results.size} result(s)")
            }.onFailure { exception ->
                Log.e(TAG, "Search failed", exception)
                warning.postValue(exception.message ?: getString(R.string.no_lyrics_found))
                _searchResults.postValue(emptyList())
            }

            _isLoading.postValue(false)
        }
    }

     













    fun downloadAndSaveLrc(lrcResponse: LrcLibResponse) {
        val audio = MediaPlaybackManager.getCurrentSong()
        if (audio == null) {
            warning.postValue(getString(R.string.no_lyrics_found))
            return
        }

        val syncedLyrics = lrcResponse.syncedLyrics
        if (syncedLyrics.isNullOrBlank()) {
            warning.postValue(getString(R.string.no_lyrics_found))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            Log.d(TAG, "Saving LRC for: ${audio.title} -> ${audio.uri}")

            
            
            lrcRepository.deleteLrcFile(audio.uri)

            val saveResult = lrcRepository.saveLrcToFile(syncedLyrics, audio.uri)

            saveResult.onSuccess {
                Log.d(TAG, "LRC saved successfully to ${it.path}")
                
                
                lyricsManager.reloadLrcData()
                _lrcSaved.postValue(true)
            }.onFailure { exception ->
                Log.e(TAG, "Failed to save LRC", exception)
                warning.postValue(getString(R.string.no_lyrics_found))
            }

            _isLoading.postValue(false)
        }
    }

     



    private fun buildDefaultKeyword(): String {
        val audio = MediaPlaybackManager.getCurrentSong() ?: return ""
        return audio.getProperTitle()
    }

    companion object {
        private const val TAG = "LyricsSearchViewModel"
        private const val KEY_KEYWORD = "lyrics_search_keyword"
        private const val KEY_USER_MODIFIED = "lyrics_search_user_modified"
    }
}

