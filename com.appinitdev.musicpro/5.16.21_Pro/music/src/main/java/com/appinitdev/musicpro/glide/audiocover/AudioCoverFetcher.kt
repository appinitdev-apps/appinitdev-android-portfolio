class AudioCoverFetcher internal constructor(
        private val context: Context,
        private val audio: Audio
) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            val bitmap = AudioCover.load(context, audio)
                ?: throw FileNotFoundException("Could not find audio artwork for: ${audio.title}")
            callback.onDataReady(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading audio cover for: ${audio.title}", e)
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
        private const val TAG = "AudioCoverFetcher"
    }
}