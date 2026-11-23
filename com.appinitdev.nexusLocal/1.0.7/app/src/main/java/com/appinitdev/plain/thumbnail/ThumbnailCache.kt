
 
















object ThumbnailCache {

     






    private const val CACHE_VERSION = "v2"

    private fun cacheDir(context: Context): File =
        File(context.cacheDir, "thumbs").also { it.mkdirs() }

    private fun cacheKey(path: String, lastModifiedMs: Long, w: Int, h: Int, centerCrop: Boolean): String {
        val raw = "$path:$lastModifiedMs:${w}x$h:$centerCrop:$CACHE_VERSION"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

     



    fun get(context: Context, path: String, w: Int, h: Int, centerCrop: Boolean): ByteArray? {
        val key = cacheKey(path, File(path).lastModified(), w, h, centerCrop)
        val cached = File(cacheDir(context), "$key.jpg")
        return if (cached.exists()) cached.readBytes() else null
    }

     
    fun put(context: Context, path: String, w: Int, h: Int, centerCrop: Boolean, bytes: ByteArray) {
        val key = cacheKey(path, File(path).lastModified(), w, h, centerCrop)
        File(cacheDir(context), "$key.jpg").writeBytes(bytes)
    }
}
