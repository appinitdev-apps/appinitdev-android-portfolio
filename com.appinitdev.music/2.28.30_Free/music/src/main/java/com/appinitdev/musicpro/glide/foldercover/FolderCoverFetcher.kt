class FolderCoverFetcher internal constructor(
        private val context: Context,
        private val folder: Folder
) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            val bitmap = FolderCover.load(context, folder)
                ?: throw FileNotFoundException("Could not find artwork for folder: ${folder.name}")
            callback.onDataReady(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading folder cover for: ${folder.name}", e)
            callback.onLoadFailed(e)
        }
    }
    override fun cleanup() {}
    override fun cancel() {}
    override fun getDataClass(): Class<Bitmap> = Bitmap::class.java
    override fun getDataSource(): DataSource = DataSource.LOCAL
    companion object {
        private const val TAG = "FolderCoverFetcher"
    }
}
