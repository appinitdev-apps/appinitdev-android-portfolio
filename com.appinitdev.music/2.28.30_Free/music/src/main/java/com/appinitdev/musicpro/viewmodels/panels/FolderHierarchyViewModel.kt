
/**
 * ViewModel for a single level of the folder hierarchy.
 *
 * @param folderPath The path to display. Null means show top-level folders.
 */
@HiltViewModel(assistedFactory = FolderHierarchyViewModel.Factory::class)
class FolderHierarchyViewModel @AssistedInject constructor(
        application: Application,
        @Assisted val folderPath: String?,
        private val audioRepository: AudioRepository
) : WrappedViewModel(application) {

    private val _contents = MutableStateFlow(FolderHierarchyContents())
    val contents: StateFlow<FolderHierarchyContents> = _contents.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadContents()
    }

    private fun loadContents() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (folderPath == null) {
                audioRepository.getTopLevelFolders()
                    .distinctUntilChanged()
                    .catch { e ->
                        Log.e(TAG, "Error loading top-level folders", e)
                        emit(emptyList())
                    }
                    .flowOn(Dispatchers.IO)
                    .collect { folders ->
                        _contents.value = FolderHierarchyContents(
                                subFolders = folders.sortedHierarchy(),
                                songs = emptyList()
                        )
                    }
            } else {
                audioRepository.getFolderContents(folderPath)
                    .distinctUntilChanged()
                    .catch { e ->
                        Log.e(TAG, "Error loading folder contents for $folderPath", e)
                        emit(AudioRepository.FolderContents(emptyList(), emptyList()))
                    }
                    .flowOn(Dispatchers.IO)
                    .collect { folderContents ->
                        _contents.value = FolderHierarchyContents(
                                subFolders = folderContents.subFolders.sortedHierarchy(),
                                songs = folderContents.songs.sortedHierarchySongs()
                        )
                    }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            FolderHierarchyPreferences.SORT_STYLE,
            FolderHierarchyPreferences.SORT_ORDER -> {
                Log.d(TAG, "Sort changed, reloading")
                loadContents()
            }
        }
    }

    data class FolderHierarchyContents(
            val subFolders: List<Folder> = emptyList(),
            val songs: List<Audio> = emptyList()
    )

    @AssistedFactory
    interface Factory {
        fun create(folderPath: String?): FolderHierarchyViewModel
    }

    companion object {
        private const val TAG = "FolderHierarchyViewModel"
    }
}
