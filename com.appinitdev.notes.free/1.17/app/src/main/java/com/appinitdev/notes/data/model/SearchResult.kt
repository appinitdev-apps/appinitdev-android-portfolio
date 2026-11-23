class SearchResult(
    private val scope: CoroutineScope,
    var baseNoteDao: BaseNoteDao,
    transform: (List<BaseNote>) -> List<Item>,
) : LiveData<List<Item>>() {
    private var job: Job? = null
    private var liveData: LiveData<List<BaseNote>>? = null
    private val observer = Observer<List<BaseNote>> { list -> value = transform(list) }
    init {
        value = emptyList()
    }
    fun fetch(keyword: String, folder: Folder, label: String?) {
        job?.cancel()
        liveData?.removeObserver(observer)
        job =
            scope.launch {
                liveData = baseNoteDao.getBaseNotesByKeyword(keyword, folder, label)
                liveData?.observeForever(observer)
            }
    }
}
