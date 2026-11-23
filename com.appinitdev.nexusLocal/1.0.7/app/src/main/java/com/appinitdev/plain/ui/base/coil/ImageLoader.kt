fun newImageLoader(context: PlatformContext): ImageLoader {
    val appContext = context.applicationContext
    val memoryPercent = if (activityManager.isLowRamDevice) 0.25 else 0.75
    val unsafeOkHttpClient = HttpClientManager.createUnsafeOkHttpClient()
    return ImageLoader.Builder(appContext)
        .components {
            add(SvgDecoder.Factory(true))
            add(AnimatedImageDecoder.Factory())
            add(ThumbnailDecoder.Factory())
            add(VideoFrameDecoder.Factory()) 
            add(OkHttpNetworkFetcherFactory(unsafeOkHttpClient))
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(appContext, percent = memoryPercent)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(appContext.cacheDir.resolve("image_cache").absoluteFile)
                .maxSizePercent(1.0)
                .build()
        }
        .crossfade(100)
        .build()
}
