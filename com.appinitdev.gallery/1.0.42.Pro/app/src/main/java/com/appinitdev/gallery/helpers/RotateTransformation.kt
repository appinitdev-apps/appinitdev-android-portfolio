class RotateTransformation(var degrees: Int) : BitmapTransformation() {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {}
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.width, toTransform.height, matrix, true)
    }
}
