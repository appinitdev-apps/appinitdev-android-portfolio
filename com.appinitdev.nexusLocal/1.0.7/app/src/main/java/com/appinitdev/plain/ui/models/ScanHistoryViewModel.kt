class ScanHistoryViewModel : ViewModel() {
    private val _itemsFlow = MutableStateFlow(emptyList<String>())
    val itemsFlow: StateFlow<List<String>> get() = _itemsFlow
    fun fetch(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _itemsFlow.update {
                ScanHistoryPreference.getValueAsync()
            }
        }
    }
    fun delete(
        context: Context,
        value: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _itemsFlow.update {
                val mutableList = it.toMutableList()
                mutableList.remove(value)
                ScanHistoryPreference.putAsync(mutableList)
                mutableList
            }
        }
    }
}
