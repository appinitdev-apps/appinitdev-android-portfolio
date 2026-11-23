
object GalleryStorageManager {

    // Función auxiliar para calcular el Hash SHA-256 de un archivo
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

    // Guarda una imagen en el almacenamiento interno de la App y la registra en Room
    suspend fun saveImageToInternalStorage(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = GalleryDatabase.getDatabase(context)
            
            // 1. Guardar en un archivo temporal para calcular el hash del contenido
            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}")
            context.contentResolver.openInputStream(uri).use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
            }

            if (!tempFile.exists() || tempFile.length() == 0L) return@withContext false

            // 2. Calcular Hash para el nombre del archivo (evita duplicados reales)
            val hash = getFileHash(tempFile)
            val fileName = "img_$hash.jpg"
            val internalFile = File(context.filesDir, fileName)

            // 3. Si no existe el archivo definitivo, lo movemos desde el temporal
            if (!internalFile.exists()) {
                tempFile.renameTo(internalFile)
            } else {
                tempFile.delete() // Si ya existe, borramos el temporal
            }

            // 4. Registrar en Room (el OnConflictStrategy.REPLACE actualizará la fecha si ya existe la ruta)
            database.galleryDao().insertImage(GalleryImage(localPath = internalFile.absolutePath))
            
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    // Descarga desde URL de red, la guarda internamente en la App y la registra en Room
    suspend fun saveUrlToInternalStorage(context: Context, urlString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = GalleryDatabase.getDatabase(context)
            
            // 1. Descargar a un archivo temporal
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

            // 2. Calcular Hash
            val hash = getFileHash(tempFile)
            val fileName = "img_$hash.jpg"
            val internalFile = File(context.filesDir, fileName)

            // 3. Mover si no existe
            if (!internalFile.exists()) {
                tempFile.renameTo(internalFile)
            } else {
                tempFile.delete()
            }

            // 4. Registrar en Room (actualiza fecha de guardado)
            database.galleryDao().insertImage(GalleryImage(localPath = internalFile.absolutePath))
            
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    // Exporta/Descarga una imagen interna hacia la Galería Pública del Teléfono (Pictures)
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

    // --- NUEVO: Guardar foto capturada (Cámara + Dibujo) ---
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

            // Registrar en Room
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

    // --- NUEVO: Compartir imagen ---
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
