
object OfflineImageManager {

    private const val FOLDER_NAME = "tour_images"

    private fun getImagesDir(context: Context): File {
        val dir = File(context.filesDir, FOLDER_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getFileNameForUrl(url: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(url.toByteArray())
            val hexString = StringBuilder()
            for (b in digest) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            val ext = when {
                url.endsWith(".png", ignoreCase = true) -> ".png"
                url.endsWith(".webp", ignoreCase = true) -> ".webp"
                else -> ".jpg"
            }
            "img_${hexString}${ext}"
        } catch (_: Exception) {
            "img_${url.hashCode()}.jpg"
        }
    }

    fun getFileForUrl(context: Context, url: String?): File? {
        if (url.isNullOrEmpty()) return null
        val dir = getImagesDir(context)
        return File(dir, getFileNameForUrl(url))
    }

    fun getFileOrUrl(context: Context, url: String?): Any {
        if (url.isNullOrEmpty()) return ""
        val file = getFileForUrl(context, url)
        return if (file != null && file.exists() && file.length() > 0) {
            file
        } else {
            url
        }
    }

    /**
     * Sincroniza y descarga las imágenes de la lista de tours.
     * 1. Descarga imágenes que no existan aún en el almacenamiento local.
     * 2. Elimina archivos locales de imágenes que YA NO ESTÉN presentes en los tours activos (p.ej. al modificar tours.json).
     */
    suspend fun syncImages(context: Context, tours: List<Tour>) = withContext(Dispatchers.IO) {
        val activeUrls = mutableSetOf<String>()

        for (tour in tours) {
            if (!tour.imagenPrincipal.isNullOrEmpty()) activeUrls.add(tour.imagenPrincipal)
            if (!tour.flayer.isNullOrEmpty()) activeUrls.add(tour.flayer)
            tour.imagenes?.forEach { img ->
                if (!img.isNullOrEmpty()) activeUrls.add(img)
            }
        }

        val dir = getImagesDir(context)
        val activeFileNames = activeUrls.map { getFileNameForUrl(it) }.toSet()

        // 1. Limpieza de imágenes obsoletas que ya no existen en los tours activos
        val existingFiles = dir.listFiles()
        existingFiles?.forEach { file ->
            if (file.isFile && file.name !in activeFileNames) {
                val deleted = file.delete()
                Log.d("OfflineImageManager", "Imagen obsoleta eliminada: ${file.name} (éxito: $deleted)")
            }
        }

        // 2. Descarga de imágenes nuevas (únicamente se descargan una vez)
        for (imageUrl in activeUrls) {
            val file = File(dir, getFileNameForUrl(imageUrl))
            if (!file.exists() || file.length() == 0L) {
                downloadImage(imageUrl, file)
            }
        }
    }

    private fun downloadImage(imageUrl: String, targetFile: File) {
        try {
            val url = URL(imageUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.doInput = true
            conn.connect()

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                conn.inputStream.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("OfflineImageManager", "Imagen descargada exitosamente para uso offline: ${targetFile.name}")
            }
        } catch (e: Exception) {
            Log.e("OfflineImageManager", "Error al descargar imagen para offline [$imageUrl]: ${e.message}")
            if (targetFile.exists()) {
                targetFile.delete()
            }
        }
    }
}
