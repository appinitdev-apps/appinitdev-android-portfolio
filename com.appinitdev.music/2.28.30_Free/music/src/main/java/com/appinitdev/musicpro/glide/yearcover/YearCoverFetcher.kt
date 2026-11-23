
 






class YearCoverFetcher internal constructor(
        private val context: Context,
        private val yearGroup: YearGroup
) : DataFetcher<Bitmap> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            val bitmap = YearCover.load(context, yearGroup)
                ?: throw FileNotFoundException("Could not find artwork for year: ${yearGroup.year}")
            callback.onDataReady(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cover for year: ${yearGroup.year}", e)
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() {}

    override fun cancel() {}

    override fun getDataClass(): Class<Bitmap> = Bitmap::class.java

    override fun getDataSource(): DataSource = DataSource.LOCAL

    companion object {
        private const val TAG = "YearCoverFetcher"
    }
}
