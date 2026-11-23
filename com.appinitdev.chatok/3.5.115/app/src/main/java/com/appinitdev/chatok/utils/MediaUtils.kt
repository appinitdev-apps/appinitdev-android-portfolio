
object MediaUtils {
    
    fun compressImage(context: Context, uri: Uri, maxWidth: Int = 1080, maxHeight: Int = 1920, quality: Int = 70): ByteArray {
        // 1. Decodificar solo límites para medir dimensiones sin cargar imagen a RAM
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
        
        // 2. Calular el inSampleSize para submuestreo eficiente
        var inSampleSize = 1
        val (origWidth, origHeight) = options.outWidth to options.outHeight
        if (origHeight > maxHeight || origWidth > maxWidth) {
            val halfHeight = origHeight / 2
            val halfWidth = origWidth / 2
            while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                inSampleSize *= 2
            }
        }
        
        // 3. Decodificar el bitmap optimizado directamente en memoria
        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            this.inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        
        val originalBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return ByteArray(0)
        
        // 4. Escalar si es necesario
        val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        var width = originalBitmap.width
        var height = originalBitmap.height
        
        if (width > maxWidth) {
            width = maxWidth
            height = (width / ratio).toInt()
        }
        
        if (height > maxHeight) {
            height = maxHeight
            width = (height * ratio).toInt()
        }
        
        val resizedBitmap = if (width != originalBitmap.width || height != originalBitmap.height) {
            Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        } else {
            originalBitmap
        }
        
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        
        // Limpieza explícita de Bitmaps para evitar picos de memoria/GC
        if (resizedBitmap != originalBitmap) {
            resizedBitmap.recycle()
        }
        originalBitmap.recycle()
        
        return outputStream.toByteArray()
    }

    fun getFileName(context: Context, uri: Uri): String {
        var name = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
        if (name.isEmpty()) name = uri.path?.substringAfterLast('/') ?: "file"
        return name
    }
}
