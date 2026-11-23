
 


object ModelDownloader {
    @Volatile private var cancelled = false
    @Volatile private var activeConn: HttpURLConnection? = null

    fun cancel() {
        cancelled = true
        activeConn?.disconnect()
    }

     




    suspend fun download(
        files: List<ModelFile>,
        destDir: File,
        onProgress: (Int) -> Unit,
        onError: (Exception) -> Unit,
    ): Boolean {
        cancelled = false
        destDir.mkdirs()
        val totalSize = files.sumOf { it.size }
        var downloaded = 0L
        try {
            for (f in files) {
                if (cancelled) {
                    destDir.deleteRecursively()
                    return false
                }
                downloaded = downloadFile(f.url, File(destDir, f.filename), downloaded, totalSize, onProgress)
            }
            if (cancelled) {
                destDir.deleteRecursively()
                return false
            }
            onProgress(100)
            return true
        } catch (e: Exception) {
            destDir.deleteRecursively()
            if (cancelled) return false
            LogCat.e("Model download failed", e)
            onError(e)
            return false
        }
    }

    private fun downloadFile(
        url: String,
        dest: File,
        startBytes: Long,
        totalSize: Long,
        onProgress: (Int) -> Unit,
    ): Long {
        var downloaded = startBytes
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 30_000
        conn.readTimeout = 30_000
        conn.instanceFollowRedirects = true
        activeConn = conn
        try {
            conn.inputStream.use { input ->
                FileOutputStream(dest).use { output ->
                    val buf = ByteArray(8192)
                    var len: Int
                    while (input.read(buf).also { len = it } != -1) {
                        if (cancelled) {
                            dest.delete()
                            return downloaded
                        }
                        output.write(buf, 0, len)
                        downloaded += len
                        onProgress((downloaded * 100 / totalSize).toInt().coerceIn(0, 99))
                    }
                }
            }
        } finally {
            activeConn = null
            conn.disconnect()
        }
        return downloaded
    }
}

data class ModelFile(val url: String, val filename: String, val size: Long)
