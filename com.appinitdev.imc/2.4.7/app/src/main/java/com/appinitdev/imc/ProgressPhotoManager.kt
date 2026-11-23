
object ProgressPhotoManager {

    fun saveProgressPhoto(context: Context, imageUri: Uri, memberId: Long): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val photoDir = File(context.filesDir, "progress_photos")
            if (!photoDir.exists()) photoDir.mkdirs()

            val photoFile = File(photoDir, "photo_${memberId}_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(photoFile)

            
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()

            photoFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
