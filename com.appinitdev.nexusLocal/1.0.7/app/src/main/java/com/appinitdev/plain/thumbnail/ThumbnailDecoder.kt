
 

























internal const val SHARPEN_THRESHOLD = 300

fun decodeSampledBitmapFromFile(
    path: String,
    reqWidth: Int,
    reqHeight: Int,
    centerCrop: Boolean,
): Bitmap? {
    
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, opts)
    val srcW = opts.outWidth
    val srcH = opts.outHeight
    if (srcW <= 0 || srcH <= 0) return null

    
    val exifOrient = readExifOrientation(path)

    
    
    
    val swapDims = exifOrient == ExifInterface.ORIENTATION_ROTATE_90 ||
            exifOrient == ExifInterface.ORIENTATION_ROTATE_270 ||
            exifOrient == ExifInterface.ORIENTATION_TRANSPOSE ||
            exifOrient == ExifInterface.ORIENTATION_TRANSVERSE
    val logicalReqW = if (swapDims) reqHeight else reqWidth
    val logicalReqH = if (swapDims) reqWidth else reqHeight

    
    val sampleSize = calcInSampleSize(srcW, srcH, logicalReqW, logicalReqH, centerCrop)

    
    val sampledW = (srcW + sampleSize - 1) / sampleSize
    val sampledH = (srcH + sampleSize - 1) / sampleSize

    
    
    val scaleFactor = if (centerCrop) {
        maxOf(logicalReqW.toFloat() / sampledW, logicalReqH.toFloat() / sampledH)
    } else {
        minOf(logicalReqW.toFloat() / sampledW, logicalReqH.toFloat() / sampledH)
    }.coerceAtMost(1f) 

    
    opts.inJustDecodeBounds = false
    opts.inSampleSize = sampleSize
    opts.inPreferredConfig = Bitmap.Config.ARGB_8888
    if (scaleFactor < 1f) {
        val densityBase = 10_000
        opts.inDensity = densityBase
        opts.inTargetDensity = (densityBase * scaleFactor + 0.5f).toInt()
        opts.inScaled = true
    }

    val decoded = BitmapFactory.decodeFile(path, opts) ?: return null

    
    
    val oriented = applyExifOrientation(decoded, exifOrient)

    
    val result = if (centerCrop) cropCenter(oriented, reqWidth, reqHeight) else oriented

    
    
    return if (result.width <= SHARPEN_THRESHOLD && result.height <= SHARPEN_THRESHOLD) {
        result.applySharpen()
    } else {
        result
    }
}

 



internal fun readExifOrientation(path: String): Int = try {
    ExifInterface(path).getAttributeInt(
        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
    )
} catch (_: Exception) { ExifInterface.ORIENTATION_NORMAL }

 


















internal fun applyExifOrientation(bitmap: Bitmap, orient: Int): Bitmap {
    val m = Matrix()
    when (orient) {
        ExifInterface.ORIENTATION_NORMAL,
        ExifInterface.ORIENTATION_UNDEFINED -> return bitmap

        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> { m.postRotate(90f); m.preScale(-1f, 1f) }
        ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> { m.postRotate(270f); m.preScale(-1f, 1f) }
        ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
        else -> return bitmap
    }
    val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    if (result !== bitmap) bitmap.recycle()
    return result
}

 





private fun calcInSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int, fill: Boolean): Int {
    var size = 1
    while (true) {
        val next = size * 2
        val nw = srcW / next
        val nh = srcH / next
        val fits = if (fill) nw >= reqW && nh >= reqH else nw >= reqW || nh >= reqH
        if (!fits || nw <= 0 || nh <= 0) break
        size = next
    }
    return size
}

 
private fun cropCenter(bitmap: Bitmap, reqW: Int, reqH: Int): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    if (w == reqW && h == reqH) return bitmap
    val x = ((w - reqW) / 2).coerceAtLeast(0)
    val y = ((h - reqH) / 2).coerceAtLeast(0)
    val cropped = Bitmap.createBitmap(bitmap, x, y, minOf(reqW, w), minOf(reqH, h))
    if (cropped !== bitmap) bitmap.recycle()
    return cropped
}

 







fun Bitmap.applySharpen(factor: Float = 0.25f): Bitmap {
    val w = width
    val h = height
    if (w < 3 || h < 3) return this

    val src = IntArray(w * h)
    getPixels(src, 0, w, 0, 0, w, h)
    val dst = src.copyOf()

    val iCenter = ((1f + 4f * factor) * 256f + 0.5f).toInt()
    val iSide = -((iCenter - 256 + 2) / 4)

    for (y in 1 until h - 1) {
        val row = y * w
        for (x in 1 until w - 1) {
            val i = row + x
            val c = src[i]; val n = src[i - w]; val s = src[i + w]
            val e = src[i + 1]; val ww = src[i - 1]

            val rSum = (n shr 16 and 0xFF) + (s shr 16 and 0xFF) + (e shr 16 and 0xFF) + (ww shr 16 and 0xFF)
            val gSum = (n shr 8 and 0xFF) + (s shr 8 and 0xFF) + (e shr 8 and 0xFF) + (ww shr 8 and 0xFF)
            val bSum = (n and 0xFF) + (s and 0xFF) + (e and 0xFF) + (ww and 0xFF)

            val r = ((c shr 16 and 0xFF) * iCenter + rSum * iSide shr 8).coerceIn(0, 255)
            val g = ((c shr 8 and 0xFF) * iCenter + gSum * iSide shr 8).coerceIn(0, 255)
            val b = ((c and 0xFF) * iCenter + bSum * iSide shr 8).coerceIn(0, 255)

            dst[i] = (c ushr 24 shl 24) or (r shl 16) or (g shl 8) or b
        }
    }

    val result = Bitmap.createBitmap(w, h, config ?: Bitmap.Config.ARGB_8888)
    result.setPixels(dst, 0, w, 0, 0, w, h)
    recycle()
    return result
}
