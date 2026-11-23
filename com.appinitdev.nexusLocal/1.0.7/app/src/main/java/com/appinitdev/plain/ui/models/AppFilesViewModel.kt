
class AppFilesViewModel : ViewModel() {
    private val _itemsFlow = MutableStateFlow(mutableStateListOf<VAppFile>())
    val itemsFlow: StateFlow<List<VAppFile>> get() = _itemsFlow

    val showLoading = mutableStateOf(true)
    val offset = mutableIntStateOf(0)
    val limit = mutableIntStateOf(50)
    val noMore = mutableStateOf(false)
    val total = mutableIntStateOf(0)

    private suspend fun fetchPage(pageOffset: Int): List<VAppFile> {
        val appFileDao = AppDatabase.instance.appFileDao()
        val chatDao = AppDatabase.instance.chatDao()
        val files = appFileDao.getPage(limit.intValue, pageOffset)
        val nameMap = AppFileDisplayNameHelper.buildNameMap(chatDao.getAll())
        return files.map { file ->
            VAppFile(
                appFile = file,
                fileName = AppFileDisplayNameHelper.resolveDisplayName(file, nameMap),
            )
        }
    }

    suspend fun moreAsync() {
        offset.intValue += limit.intValue
        val items = fetchPage(offset.intValue)
        _itemsFlow.value.addAll(items)
        noMore.value = items.size < limit.intValue
        showLoading.value = false
    }

    suspend fun loadAsync() {
        offset.intValue = 0
        val appFileDao = AppDatabase.instance.appFileDao()
        total.intValue = appFileDao.count()
        val items = fetchPage(0)
        _itemsFlow.value = items.toMutableStateList()
        noMore.value = items.size < limit.intValue
        showLoading.value = false
    }
}
