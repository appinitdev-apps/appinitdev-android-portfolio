class FeedAutoRefreshInterval(val value: Int) : ISelectOption {
    override suspend fun isSelected(context: Context): Boolean {
        return value == FeedAutoRefreshIntervalPreference.getAsync()
    }
}
