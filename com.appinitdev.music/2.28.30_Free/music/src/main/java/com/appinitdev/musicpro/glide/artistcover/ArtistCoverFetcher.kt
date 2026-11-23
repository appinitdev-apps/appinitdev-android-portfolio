class ArtistCoverFetcher internal constructor(
        private val context: Context,
        private val artist: Artist
) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            val bitmap = ArtistCover.load(context, artist)
            callback.onDataReady(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading artist cover for: ${artist.name}", e)
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
        private const val TAG = "ArtistCoverFetcher"
    }
}
