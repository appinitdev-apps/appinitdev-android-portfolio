
@UnstableApi
class BaseApplication : Application(), ImageLoaderFactory {
    companion object {
        lateinit var instance: BaseApplication
            private set
        lateinit var simpleCache: SimpleCache
        const val exoPlayerCacheSize: Long = 90 * 1024 * 1024
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var databaseProvider: DatabaseProvider
        lateinit var database: AppDatabase
    }
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        
        val settings = firestoreSettings {
            setLocalCacheSettings(
                persistentCacheSettings {
                    setSizeBytes(100L * 1024 * 1024) 
                }
            )
        }
        FirebaseFirestore.getInstance().firestoreSettings = settings

        database = AppDatabase.getDatabase(this)
        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        databaseProvider = StandaloneDatabaseProvider(this)
        simpleCache = SimpleCache(cacheDir.resolve("video_cache"), leastRecentlyUsedCacheEvictor, databaseProvider)}

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250 * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}