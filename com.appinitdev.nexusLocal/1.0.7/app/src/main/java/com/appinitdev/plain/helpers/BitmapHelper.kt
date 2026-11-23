object BitmapHelper {
    suspend fun decodeBitmapFromFileAsync(
        context: Context,
        path: String,
        reqWidth: Int,
        reqHeight: Int,
    ): Bitmap? {
        return ThumbnailGenerator.getBitmapAsync(context, File(path), reqWidth, reqHeight)
    }
}
