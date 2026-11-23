object GalleryStorageManager {
    private fun getFileHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    suspend fun saveImageToInternalStorage(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = GalleryDatabase.getDatabase(context)
            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
            context.contentResolver.openInputStream(uri).use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
            }
            if (!tempFile.exists() || tempFile.length() == 0L) return@withContext false
            val hash = getFileHash(tempFile)
            val fileName = "img_$hash.jpg"
            val internalFile = File(context.filesDir, fileName)
            if (!internalFile.exists()) {
                tempFile.renameTo(internalFile)
            } else {
                tempFile.delete() 
            }
            database.galleryDao().insertImage(GalleryImage(localPath = internalFile.absolutePath))
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
    suspend fun saveUrlToInternalStorage(context: Context, urlString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = GalleryDatabase.getDatabase(context)
            val tempFile = File(context.cacheDir, "temp_url_${System.currentTimeMillis()}")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            connection.inputStream.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            if (!tempFile.exists() || tempFile.length() == 0L) return@withContext false
            val hash = getFileHash(tempFile)
            val fileName = "img_$hash.jpg"
            val internalFile = File(context.filesDir, fileName)
            if (!internalFile.exists()) {
                tempFile.renameTo(internalFile)
            } else {
                tempFile.delete()
            }
            database.galleryDao().insertImage(GalleryImage(localPath = internalFile.absolutePath))
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
    suspend fun downloadToPublicGallery(context: Context, localPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(localPath)
            if (!sourceFile.exists()) return@withContext false
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(publicDir, "ArtyTracePro")
            if (!appDir.exists()) appDir.mkdirs()
            val publicFile = File(appDir, "Saved_${sourceFile.name}")
            sourceFile.inputStream().use { input ->
                publicFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
    suspend fun saveCapturedWork(
        context: Context, 
        bitmap: android.graphics.Bitmap, 
        templateUri: Uri?,
        isTimeLapse: Boolean = false,
        sessionId: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = GalleryDatabase.getDatabase(context)
            val fileName = if (isTimeLapse) "tl_${sessionId}_${System.currentTimeMillis()}.jpg" else "work_${System.currentTimeMillis()}.jpg"
            val internalFile = File(context.filesDir, fileName)
            FileOutputStream(internalFile).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            database.galleryDao().insertCapturedWork(
                CapturedWork(
                    workPath = internalFile.absolutePath,
                    templatePath = templateUri?.toString(),
                    isTimeLapse = isTimeLapse,
                    sessionId = sessionId
                )
            )
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
    fun shareImage(context: Context, localPath: String) {
        try {
            val file = File(localPath)
            if (!file.exists()) {
                android.widget.Toast.makeText(context, context.getString(R.string.archivo_no_encontrado), android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val mimeType = if (localPath.endsWith(".png", true)) "image/png" else "image/jpeg"
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, context.getString(R.string.compartir)))
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, context.getString(R.string.error_al_compartir), android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
