
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

        
        for (bucket in _itemsFlow.value) {
            totalValue += bucket.itemCount
            sizeValue += bucket.size

            if (subItems.size < 4) {
                
                val validTopItems = bucket.topItems.filter { File(it).exists() }
                if (validTopItems.isNotEmpty()) {
                    subItems.add(validTopItems.first())
                }
            }
        }

        
        
        if (subItems.size < 4 && _itemsFlow.value.isNotEmpty()) {
            for (bucket in _itemsFlow.value) {
                val validTopItems = bucket.topItems.filter { File(it).exists() }
                if (validTopItems.size > 1) {
                    
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

     



    suspend fun preValidateFilesAsync() = withContext(Dispatchers.IO) {
        _itemsFlow.value.forEach { bucket ->
            
            bucket.topItems.removeAll { !File(it).exists() }
        }
    }
}
