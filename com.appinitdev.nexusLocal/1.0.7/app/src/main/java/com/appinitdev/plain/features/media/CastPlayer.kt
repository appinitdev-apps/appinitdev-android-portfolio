
object CastPlayer {
    var currentDevice: DlnaDevice? = null

    private val _items = MutableStateFlow<List<IMedia>>(emptyList())
    val items: StateFlow<List<IMedia>> = _items.asStateFlow()

    private val _currentUri = MutableStateFlow("")
    val currentUri: StateFlow<String> = _currentUri.asStateFlow()
    val isPlaying = MutableStateFlow(false)

    
    val progress = MutableStateFlow(0f) 
    val duration = MutableStateFlow(0f) 
    val supportsCallback = MutableStateFlow(false) 

    var sid: String = ""

    fun setItems(newItems: List<IMedia>) {
        _items.value = newItems
    }

    fun addItem(item: IMedia) {
        _items.value = _items.value + item
    }

    fun removeItem(item: IMedia) {
        _items.value = _items.value.filter { it.path != item.path }
    }

    fun removeItemAt(index: Int) {
        val currentList = _items.value.toMutableList()
        if (index in 0 until currentList.size) {
            currentList.removeAt(index)
            _items.value = currentList
        }
    }

    fun clearItems() {
        _items.value = emptyList()
        _currentUri.value = ""
        isPlaying.value = false
        progress.value = 0f
        duration.value = 0f
        supportsCallback.value = false
    }

    fun setCurrentUri(uri: String) {
        _currentUri.value = uri
    }

    fun reorderItems(fromIndex: Int, toIndex: Int) {
        val currentList = _items.value.toMutableList()
        if (fromIndex in 0 until currentList.size && toIndex in 0 until currentList.size) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            _items.value = currentList
        }
    }

     


    fun parseTimeToSeconds(timeString: String): Float {
        if (timeString.isEmpty() || timeString == "NOT_IMPLEMENTED") return 0f
        
        return try {
            val parts = timeString.split(":")
            if (parts.size >= 3) {
                val hours = parts[0].toFloat()
                val minutes = parts[1].toFloat()
                val seconds = parts[2].split(".")[0].toFloat() 
                hours * 3600 + minutes * 60 + seconds
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
    }

     


    fun updatePositionInfo(relTime: String, trackDuration: String) {
        progress.value = parseTimeToSeconds(relTime)
        duration.value = parseTimeToSeconds(trackDuration)
    }
}
