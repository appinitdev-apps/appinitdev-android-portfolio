class AlbumCoverFetcher internal constructor(
        private val context: Context,
        private val album: Album
) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            val bitmap = AlbumCover.load(context, album)
                ?: throw FileNotFoundException("Could not find album artwork for: ${album.name}")
            callback.onDataReady(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading album cover for: ${album.name}", e)
            callback.onLoadFailed(e)
        }
    }
    override fun cleanup() {
    }
    override fun cancel() {
    }
    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }
    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
    companion object {
        private const val TAG = "AlbumCoverFetcher"
    }
}
