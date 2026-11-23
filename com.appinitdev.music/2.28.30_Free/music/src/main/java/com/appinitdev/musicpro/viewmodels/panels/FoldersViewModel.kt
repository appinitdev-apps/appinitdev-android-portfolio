
@HiltViewModel
class FoldersViewModel @Inject constructor(
        application: Application,
        private val audioRepository: AudioRepository
) : WrappedViewModel(application) {

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            audioRepository.getAllFoldersWithAggregation()
                .map { folderList -> folderList.sorted() }
                .distinctUntilChanged()
                .catch { exception ->
                    Log.e(TAG, "Error loading folders", exception)
                    emit(emptyList())
                }
                .flowOn(Dispatchers.IO)
                .collect { sortedFolders ->
                    _folders.value = sortedFolders
                    Log.d(TAG, "loadData: ${sortedFolders.size} folders loaded")
                }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            FoldersPreferences.FOLDER_SORT_STYLE,
            FoldersPreferences.SORT_ORDER -> {
                Log.d(TAG, "onSharedPreferenceChanged: Sorting order changed, updating folders list")
                loadData()
            }
        }
    }

    companion object {
        private const val TAG = "FoldersViewModel"
    }
}

