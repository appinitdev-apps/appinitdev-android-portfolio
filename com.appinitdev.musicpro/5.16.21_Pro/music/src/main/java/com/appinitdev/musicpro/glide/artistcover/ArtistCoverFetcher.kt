
/**
 * Glide DataFetcher for loading artist cover artwork.
 * Delegates to ArtistCover.load() for centralized artist cover loading logic.
 */
class ArtistCoverFetcher internal constructor(
        private val context: Context,
        private val artist: Artist
) : DataFetcher<Bitmap> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            // Delegate to ArtistCover for centralized artist cover loading
            val bitmap = ArtistCover.load(context, artist)

            callback.onDataReady(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading artist cover for: ${artist.name}", e)
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() {
        // No cleanup needed - ArtistUtils handles resource management
    }

    override fun cancel() {
        // No cancellation needed
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
