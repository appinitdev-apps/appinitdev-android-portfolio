@OptIn(UnstableApi::class)
object VideoPlayerCacheManager {
    private lateinit var cacheInstance: Cache
    @SuppressLint("UnsafeOptInUsageError")
    fun initialize(context: Context, maxCacheBytes: Long) {
        if (VideoPlayerCacheManager::cacheInstance.isInitialized) {
            return
        }
        cacheInstance = SimpleCache(
            File(context.cacheDir, "video"),
            LeastRecentlyUsedCacheEvictor(maxCacheBytes),
            StandaloneDatabaseProvider(context),
        )
    }
    internal fun getCache(): Cache? =
        if (VideoPlayerCacheManager::cacheInstance.isInitialized) {
            cacheInstance
        } else {
            null
        }
}
