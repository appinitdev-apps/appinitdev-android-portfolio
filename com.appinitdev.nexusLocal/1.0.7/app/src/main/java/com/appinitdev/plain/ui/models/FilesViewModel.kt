data class BreadcrumbItem(var name: String, var path: String)
class FilesViewModel : ISearchableViewModel<DFile>, ISelectableViewModel<DFile>, ViewModel() {
    var rootPath = FileSystemHelper.getInternalStoragePath()
    private var _selectedPath = rootPath
    var selectedPath: String
        get() = _selectedPath
        set(value) {
            val isChanged = _selectedPath != value
            _selectedPath = value
            if (isChanged) {
                viewModelScope.launch(Dispatchers.IO) {
                    val breadcrumbsCopy = breadcrumbs.toList()
                    val fullPath = if (breadcrumbsCopy.isNotEmpty()) breadcrumbsCopy.last().path else value
                    LastFilePathPreference.putAsync(FilePathData(rootPath = rootPath, fullPath = fullPath, selectedPath = value))
                }
            }
        }
    val breadcrumbs = mutableStateListOf<BreadcrumbItem>()
    val selectedBreadcrumbIndex = mutableIntStateOf(0)
    var cutFiles = mutableListOf<DFile>()
    var copyFiles = mutableListOf<DFile>()
    var type: FilesType = FilesType.INTERNAL_STORAGE
    var offset = 0
    var limit: Int = 1000
    var total: Int = 0
    internal val navigationHistoryInternal = mutableStateListOf<String>()
    init { breadcrumbs.add(BreadcrumbItem(getRootDisplayName(), rootPath)) }
    val selectedFile = mutableStateOf<DFile?>(null)
    val showRenameDialog = mutableStateOf(false)
    override val showSearchBar = mutableStateOf(false)
    override val searchActive = mutableStateOf(false)
    override val queryText = mutableStateOf("")
    override val selectMode = mutableStateOf(false)
    override val selectedIds = mutableStateListOf<String>()
    private val _itemsFlow = MutableStateFlow<List<DFile>>(emptyList())
    override val itemsFlow: StateFlow<List<DFile>> = _itemsFlow.asStateFlow()
    val sortBy = mutableStateOf(FileSortBy.NAME_ASC)
    val showSortDialog = mutableStateOf(false)
    val isLoading = mutableStateOf(true)
    val showPasteBar = mutableStateOf(false)
    val showCreateFolderDialog = mutableStateOf(false)
    val showCreateFileDialog = mutableStateOf(false)
    val showFolderKanbanDialog = mutableStateOf(false)
    val isDeleting = mutableStateOf(false)
    internal fun updateItemsInternal(items: List<DFile>) { _itemsFlow.value = items }
    fun navigateToDirectory(context: Context, newPath: String) = navigateToDirectoryInternal(context, newPath)
    fun navigateBack(): Boolean = navigateBackInternal()
    suspend fun loadLastPathAsync(context: Context) = loadLastPathAsyncInternal(context)
    fun canNavigateBack(): Boolean = navigationHistoryInternal.isNotEmpty()
    fun initSelectedPath(rootPath: String, type: FilesType, fullPath: String, selectedPath: String) = initSelectedPathInternal(rootPath, type, fullPath, selectedPath)
    fun getRootDisplayName(): String = when (type) {
        FilesType.INTERNAL_STORAGE -> FileSystemHelper.getInternalStorageName()
        FilesType.APP -> LocaleHelper.getString(Res.string.app_data)
        FilesType.SDCARD -> LocaleHelper.getString(Res.string.sdcard)
        FilesType.USB_STORAGE -> LocaleHelper.getString(Res.string.usb_storage)
        FilesType.RECENTS -> LocaleHelper.getString(Res.string.recents)
    }
    fun updateRootBreadcrumb() { if (breadcrumbs.isNotEmpty()) breadcrumbs[0] = BreadcrumbItem(getRootDisplayName(), rootPath) }
    fun getQuery(): String = queryText.value.trim()
    suspend fun loadAsync(context: Context) {
        isLoading.value = true
        val showHiddenFiles = ShowHiddenFilesPreference.getAsync()
        val query = getQuery()
        val files = when {
            ZipBrowserHelper.isZipPath(selectedPath) -> ZipBrowserHelper.listEntries(selectedPath, sortBy.value)
            showSearchBar.value && query.isNotEmpty() -> FileSystemHelper.search(query, selectedPath, showHiddenFiles)
            type == FilesType.RECENTS -> FileMediaStoreHelper.getRecentFilesAsync(context)
            else -> FileSystemHelper.getFilesList(selectedPath, showHiddenFiles, sortBy.value)
        }
        _itemsFlow.value = files
        isLoading.value = false
    }
    fun deleteFiles(paths: Set<String>) {
        viewModelScope.launch {
            DialogHelper.showLoading()
            withIO {
                FilePathValidator.requireAllSafe(paths.toList())
                paths.forEach { File(it).deleteRecursively() }
                MainApp.instance.scanFileByConnection(paths.toTypedArray())
            }
            DialogHelper.hideLoading()
            _itemsFlow.update { it.toMutableStateList().apply { removeIf { i -> paths.contains(i.path) } } }
        }
    }
}
