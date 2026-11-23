class FeedSettingsViewModel : ViewModel() {
    var showIntervalDialog = mutableStateOf(false)
    var autoRefresh = mutableStateOf(true)
    var autoRefreshInterval = mutableIntStateOf(7200)
    var autoRefreshOnlyWifi = mutableStateOf(false)
    var showClearFeedsDialog = mutableStateOf(false)
    var clearFeedItemsTs = mutableLongStateOf(Constants.ONE_DAY * 7)
    fun loadSettings(context: Context) {
        viewModelScope.launch {
            autoRefresh.value = FeedAutoRefreshPreference.getAsync()
            autoRefreshInterval.intValue = FeedAutoRefreshIntervalPreference.getAsync()
            autoRefreshOnlyWifi.value = FeedAutoRefreshOnlyWifiPreference.getAsync()
        }
    }
    fun setAutoRefresh(context: Context, value: Boolean) {
        autoRefresh.value = value
        viewModelScope.launch(Dispatchers.IO) {
            FeedAutoRefreshPreference.putAsync(value)
            if (value) {
                FeedFetchWorker.startRepeatWorkerAsync(context)
            } else {
                FeedFetchWorker.cancelRepeatWorker()
            }
        }
    }
    fun setAutoRefreshInterval(context: Context, value: Int) {
        autoRefreshInterval.value = value
        viewModelScope.launch(Dispatchers.IO) {
            FeedAutoRefreshIntervalPreference.putAsync(value)
        }
    }
    fun setAutoRefreshOnlyWifi(context: Context, value: Boolean) {
        autoRefreshOnlyWifi.value = value
        viewModelScope.launch(Dispatchers.IO) {
            FeedAutoRefreshOnlyWifiPreference.putAsync(value)
        }
    }
    suspend fun clearByFeedIdAsync(feedId: String) {
        val ids = FeedEntryHelper.getIdsAsync("feed_id:$feedId")
        TagHelper.deleteTagRelationByKeys(ids, DataType.FEED_ENTRY)
        FeedEntryHelper.deleteAsync(ids)
    }
    fun clearAllAsync() {
        TagHelper.deleteByTypeAsync(DataType.FEED_ENTRY)
        FeedEntryHelper.deleteAllAsync()
    }
    suspend fun clearByTimeAsync(ts: Long) {
        val time = TimeHelper.now().epochSeconds - ts
        val ids = FeedEntryHelper.getIdsAsync("created_at:<${Instant.fromEpochSeconds(time)}")
        TagHelper.deleteTagRelationByKeys(ids, DataType.FEED_ENTRY)
        FeedEntryHelper.deleteAsync(ids)
    }
}
