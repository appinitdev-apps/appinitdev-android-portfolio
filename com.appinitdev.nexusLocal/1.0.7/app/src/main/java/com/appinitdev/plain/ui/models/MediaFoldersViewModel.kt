
@OptIn(androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi::class)
class MediaFoldersViewModel : ViewModel() {
    private val _itemsFlow = MutableStateFlow<List<DMediaBucket>>(emptyList())
    val itemsFlow: StateFlow<List<DMediaBucket>> get() = _itemsFlow
    val totalBucket = mutableStateOf<DMediaBucket?>(null)

    val bucketsMapFlow: StateFlow<Map<String, DMediaBucket>> =
        _itemsFlow
            .map { list -> list.associateBy { it.id } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    var showLoading = mutableStateOf(true)
    var dataType = mutableStateOf(DataType.DEFAULT)

    fun loadAsync(context: Context) {
        val startTime = System.currentTimeMillis()
        _itemsFlow.value = (when (dataType.value) {
            DataType.IMAGE -> {
                ImageMediaStoreHelper.getBucketsAsync(context)
            }

            DataType.VIDEO -> {
                VideoMediaStoreHelper.getBucketsAsync(context)
            }

            DataType.AUDIO -> {
                if (isQPlus()) {
                    AudioMediaStoreHelper.getBucketsAsync(context)
                } else {
                    emptyList()
                }
            }

            DataType.DOC -> {
                DocMediaStoreHelper.getDocBucketsAsync(context)
            }

            else -> {
                emptyList()
            }
        }).toMutableStateList()

        var totalValue = 0
        var sizeValue = 0L
        val subItems = mutableSetOf<String>()

        // Take one top item from each folder until we have 4 items
        for (bucket in _itemsFlow.value) {
            totalValue += bucket.itemCount
            sizeValue += bucket.size

            if (subItems.size < 4) {
                // Add the first item from each folder's topItems if available
                val validTopItems = bucket.topItems.filter { File(it).exists() }
                if (validTopItems.isNotEmpty()) {
                    subItems.add(validTopItems.first())
                }
            }
        }

        // If we have fewer than 4 items and there's at least one folder with more items
        // take additional items from the first folder that has multiple items
        if (subItems.size < 4 && _itemsFlow.value.isNotEmpty()) {
            for (bucket in _itemsFlow.value) {
                val validTopItems = bucket.topItems.filter { File(it).exists() }
                if (validTopItems.size > 1) {
                    // Start from the second item (index 1) since we've already added the first one
                    for (i in 1 until validTopItems.size) {
                        if (subItems.size < 4) {
                            subItems.add(validTopItems[i])
                        } else {
                            break
                        }
                    }
                }

                if (subItems.size >= 4) {
                    break
                }
            }
        }

        totalBucket.value = DMediaBucket("all", LocaleHelper.getString(Res.string.all), totalValue, sizeValue, subItems.toMutableList())

        LoadingHelper.ensureMinimumLoadingTime(
            viewModel = this,
            startTime = startTime,
            updateLoadingState = { isLoading -> showLoading.value = isLoading }
        )
    }

    /**
     * 异步预验证文件存在性，可以在后台线程中调用
     * 这样可以提前过滤掉不存在的文件，减少UI线程的负担
     */
    suspend fun preValidateFilesAsync() = withContext(Dispatchers.IO) {
        _itemsFlow.value.forEach { bucket ->
            // 过滤掉不存在的文件
            bucket.topItems.removeAll { !File(it).exists() }
        }
    }
}
