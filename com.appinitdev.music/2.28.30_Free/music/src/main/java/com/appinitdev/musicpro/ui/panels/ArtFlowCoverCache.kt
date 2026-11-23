@Suppress("unused")
class ArtFlowCoverCache(
        private val context: Context,
        maxMemoryCacheSizeMB: Int = 25
) {
    private val TAG = "ArtFlowCoverCache"
    private val maxMemoryCacheSize = maxMemoryCacheSizeMB * 1024 * 1024
    private val memoryCache = object : LruCache<Int, Bitmap>(maxMemoryCacheSize) {
        override fun sizeOf(key: Int, bitmap: Bitmap): Int = bitmap.byteCount
        override fun entryRemoved(evicted: Boolean, key: Int, oldValue: Bitmap, newValue: Bitmap?) {
            if (evicted && oldValue != newValue) {
                Log.d(TAG, "Evicted and recycling bitmap for index $key from cache")
                oldValue.recycle()
            }
        }
    }
    private val pendingLoads = ConcurrentHashMap<Int, kotlinx.coroutines.Job>()
    @Volatile
    private var currentCenterIndex = 0
    @Volatile
    private var currentRadius = 8
    private val ioExecutor = Executors.newFixedThreadPool(3).asCoroutineDispatcher()
    private val cacheScope = CoroutineScope(SupervisorJob() + ioExecutor)
    private var audioList: List<Audio> = emptyList()
    fun setAudioList(list: List<Audio>) {
        audioList = list
        clearCache()
    }
    fun getOrNull(index: Int): Bitmap? {
        if (index !in audioList.indices) return null
        return memoryCache.get(index)
    }
    fun loadSync(index: Int, maxDimension: Int): Bitmap? {
        if (index !in audioList.indices) return null
        memoryCache.get(index)?.let { return it }
        val bitmap = loadBitmapFromDisk(index, maxDimension)
        if (bitmap != null) memoryCache.put(index, bitmap)
        return bitmap
    }
    fun preloadAround(centerIndex: Int, radius: Int = 8, maxDimension: Int = 512, debounceMs: Long = LOAD_DEBOUNCE_MS) {
        currentCenterIndex = centerIndex
        currentRadius = radius
        val validStart = (centerIndex - radius).coerceAtLeast(0)
        val validEnd = (centerIndex + radius).coerceAtMost(audioList.size - 1)
        pendingLoads.entries.removeIf { (index, job) ->
            (index < validStart || index > validEnd).also { outOfRange ->
                if (outOfRange) job.cancel()
            }
        }
        for (offset in 0..radius) {
            for (index in buildOffsetPair(centerIndex, offset)) {
                if (index !in validStart..validEnd) continue
                if (pendingLoads.containsKey(index)) continue
                if (memoryCache.get(index) != null) continue
                pendingLoads[index] = cacheScope.launch {
                    if (debounceMs > 0L) delay(debounceMs)
                    if (!isActive || abs(index - currentCenterIndex) > currentRadius) {
                        pendingLoads.remove(index)
                        return@launch
                    }
                    if (memoryCache.get(index) != null) {
                        Log.d(TAG, "Index $index was loaded by fallback during debounce; skipping disk load")
                        pendingLoads.remove(index)
                        return@launch
                    }
                    try {
                        val bitmap = withContext(Dispatchers.IO) {
                            loadBitmapFromDisk(index, maxDimension)
                        }
                        when {
                            bitmap == null -> {  
                            }
                            !isActive || abs(index - currentCenterIndex) > currentRadius -> {
                                bitmap.recycle()
                            }
                            else -> {
                                memoryCache.put(index, bitmap)
                                Log.d(TAG, "Preloaded bitmap for index $index")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error preloading index $index", e)
                    } finally {
                        pendingLoads.remove(index)
                    }
                }
            }
        }
        cleanupCache(centerIndex, radius + 2)
    }
    fun clearCache() {
        pendingLoads.values.forEach { it.cancel() }
        pendingLoads.clear()
        memoryCache.evictAll()
    }
    fun release() {
        clearCache()
        cacheScope.cancel()
        ioExecutor.close()
    }
    private fun buildOffsetPair(centerIndex: Int, offset: Int): List<Int> {
        return if (offset == 0) listOf(centerIndex)
        else listOf(centerIndex + offset, centerIndex - offset)
    }
    private fun loadBitmapFromDisk(index: Int, maxDimension: Int): Bitmap? {
        if (index !in audioList.indices) return null
        return try {
            val audio = audioList[index]
            val bitmap = AudioCover.load(context, audio) ?: return null
            if (maxDimension > 0) {
                val w = bitmap.width
                val h = bitmap.height
                val maxDim = kotlin.math.max(w, h)
                if (maxDim > maxDimension) {
                    val scaleFactor = maxDimension.toFloat() / maxDim
                    val scaled = bitmap.scale((w * scaleFactor).toInt(), (h * scaleFactor).toInt(), true)
                    if (bitmap != scaled) bitmap.recycle()
                    return scaled
                }
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap for index $index", e)
            null
        }
    }
    private fun cleanupCache(centerIndex: Int, keepRadius: Int) {
        val snapshot = memoryCache.snapshot()
        for ((index, bitmap) in snapshot) {
            if (abs(index - centerIndex) > keepRadius) {
                memoryCache.remove(index)
                bitmap.recycle()
            }
        }
    }
    companion object {
        private const val LOAD_DEBOUNCE_MS = 1000L
    }
}
