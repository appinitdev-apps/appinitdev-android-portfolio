internal object BitmapCalculator {
  internal fun scaleBitmap(bitmap: Bitmap, targetSize: IntSize): Bitmap {
    return Bitmap.createScaledBitmap(
      bitmap,
      targetSize.width,
      targetSize.height,
      false,
    )
  }
  internal fun cropBitmap(bitmap: Bitmap, targetSize: IntSize): Bitmap {
    return ThumbnailUtils.extractThumbnail(bitmap, targetSize.width, targetSize.height)
  }
  internal fun inside(bitmap: Bitmap, targetSize: IntSize): Bitmap {
    return if (bitmap.width < targetSize.width && bitmap.height < targetSize.height) {
      bitmap
    } else {
      scaleBitmap(bitmap, targetSize)
    }
  }
}
