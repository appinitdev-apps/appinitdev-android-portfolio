object ThumbnailGenerator {
    suspend fun getBitmapAsync(
        context: Context,
        file: File,
        width: Int,
        height: Int,
        centerCrop: Boolean = true,
        mediaId: String = "",
        fileName: String = "",
    ): Bitmap? {
        val effectiveName = fileName.ifEmpty { file.name }
        if (effectiveName.endsWith(".svg", true)) {
            return try {
                val svg = SVG.getFromString(file.readText())
                val picture = svg.renderToPicture(width, height)
                PictureDrawable(picture).toBitmap(width, height)
            } catch (ex: Exception) {
                LogCat.e(ex.toString())
                null
            }
        }
        var bitmap: Bitmap? = null
        if (isQPlus()) {
            val isMedia = effectiveName.isVideoFast() || effectiveName.isImageFast() || effectiveName.isAudioFast()
            if (isMedia) {
                val contentUri = when {
                    mediaId.isNotEmpty() -> file.path.pathToMediaStoreUri(mediaId)
                    effectiveName.isVideoFast() -> context.contentResolver.getMediaContentUri(file.path)
                    else -> null  
                }
                if (contentUri != null) {
                    try {
                        bitmap = context.contentResolver.loadThumbnail(contentUri, Size(width, height), null)
                    } catch (ex: Exception) {
                        LogCat.e(ex.toString())
                    }
                }
                if (bitmap != null) return bitmap
            }
        }
        if (effectiveName.isPartialSupportVideo()) {
            return try {
                if (isQPlus()) {
                    ThumbnailUtils.createVideoThumbnail(file, Size(width, height), null)
                } else {
                    @Suppress("DEPRECATION")
                    ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Video.Thumbnails.MICRO_KIND)
                }
            } catch (ex: Exception) {
                LogCat.e(ex.toString())
                null
            }
        }
        if (effectiveName.isVideoFast()) {
            return try {
                if (isQPlus()) {
                    ThumbnailUtils.createVideoThumbnail(file, Size(width, height), null)
                } else {
                    @Suppress("DEPRECATION")
                    ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Video.Thumbnails.MICRO_KIND)
                }
            } catch (ex: Exception) {
                LogCat.e(ex.toString())
                null
            }
        }
        try {
            bitmap = decodeSampledBitmapFromFile(file.absolutePath, width, height, centerCrop)
        } catch (ex: Exception) {
            LogCat.e(ex.toString())
        }
        if (bitmap == null) {
            try {
                val imageLoader = SingletonImageLoader.get(context)
                val request = ImageRequest.Builder(context)
                    .data(file)
                    .size(width, height)
                    .scale(if (centerCrop) Scale.FILL else Scale.FIT)
                    .allowHardware(false)
                    .build()
                bitmap = (imageLoader.execute(request).image as? BitmapImage)?.bitmap
            } catch (ex: Exception) {
                LogCat.e(ex.toString())
            }
        }
        return bitmap
    }
    suspend fun toThumbBytesAsync(
        context: Context,
        file: File,
        width: Int,
        height: Int,
        centerCrop: Boolean,
        mediaId: String,
        fileName: String = "",
    ): ByteArray? {
        if (mediaId.isNotEmpty() && width <= 512) {
            val bitmap = getBitmapAsync(context, file, width, height, centerCrop, mediaId, fileName)
            if (bitmap != null) {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                bitmap.recycle()
                return stream.toByteArray()
            }
        }
        ThumbnailCache.get(context, file.absolutePath, width, height, centerCrop)?.let { return it }
        val bitmap = getBitmapAsync(context, file, width, height, centerCrop, fileName = fileName) ?: return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        bitmap.recycle()
        val bytes = stream.toByteArray()
        ThumbnailCache.put(context, file.absolutePath, width, height, centerCrop, bytes)
        return bytes
    }
}
