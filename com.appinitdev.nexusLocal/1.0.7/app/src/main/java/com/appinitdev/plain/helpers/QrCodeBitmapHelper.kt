
object QrCodeBitmapHelper {
    private const val MAX_BITMAP_SIZE = 1024 

    fun getBitmapFromUri(context: Context, imageUri: Uri): Bitmap {
        val source = ImageDecoder.createSource(
            context.contentResolver,
            imageUri
        )
        return ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
    }

    fun getBitmapFromContentUri(
        context: Context,
        imageUri: Uri,
    ): Bitmap {
        val options =
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

        
        BitmapFactory.decodeFileDescriptor(context.contentResolver.openFileDescriptor(imageUri, "r")?.fileDescriptor, null, options)

        
        options.inSampleSize = calcInSampleSize(options.outWidth, options.outHeight, MAX_BITMAP_SIZE)

        
        options.inJustDecodeBounds = false

        
        val bitmap =
            BitmapFactory.decodeFileDescriptor(
                context.contentResolver.openFileDescriptor(imageUri, "r")?.fileDescriptor,
                null,
                options,
            )

        val orientation = getExifOrientationTag(context.contentResolver, imageUri)
        var rotationDegrees = 0
        var flipX = false
        var flipY = false
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipX = true
            ExifInterface.ORIENTATION_ROTATE_90 -> rotationDegrees = 90
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                rotationDegrees = 90
                flipX = true
            }

            ExifInterface.ORIENTATION_ROTATE_180 -> rotationDegrees = 180
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipY = true
            ExifInterface.ORIENTATION_ROTATE_270 -> rotationDegrees = -90
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                rotationDegrees = -90
                flipX = true
            }

            ExifInterface.ORIENTATION_UNDEFINED, ExifInterface.ORIENTATION_NORMAL -> {}
            else -> {}
        }
        return rotateBitmap(bitmap, rotationDegrees, flipX, flipY)
    }

    private fun getExifOrientationTag(
        resolver: ContentResolver,
        imageUri: Uri,
    ): Int {
        
        
        
        if (ContentResolver.SCHEME_CONTENT != imageUri.scheme &&
            ContentResolver.SCHEME_FILE != imageUri.scheme
        ) {
            return 0
        }
        var exif: ExifInterface
        try {
            resolver.openInputStream(imageUri).use { inputStream ->
                if (inputStream == null) {
                    return 0
                }
                exif = ExifInterface(inputStream)
            }
        } catch (e: IOException) {
            LogCat.e("failed to open file to read rotation meta data: $imageUri, $e")
            return 0
        }
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }

    private fun rotateBitmap(
        bitmap: Bitmap,
        rotationDegrees: Int,
        flipX: Boolean,
        flipY: Boolean,
    ): Bitmap {
        val matrix = Matrix()

        
        matrix.postRotate(rotationDegrees.toFloat())

        
        matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        
        if (rotatedBitmap != bitmap) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

     
    private fun calcInSampleSize(srcW: Int, srcH: Int, maxPx: Int): Int {
        var size = 1
        while ((srcW / (size * 2)) >= maxPx && (srcH / (size * 2)) >= maxPx) {
            size *= 2
        }
        return size
    }
}
