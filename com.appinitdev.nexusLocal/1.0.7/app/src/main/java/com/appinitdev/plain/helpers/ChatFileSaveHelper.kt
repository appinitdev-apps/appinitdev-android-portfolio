object ChatFileSaveHelper {
    fun importFromUri(
        context: Context,
        uri: Uri,
        mimeType: String = "",
    ): String {
        val tempFile = File(context.cacheDir, "chat_import_${System.currentTimeMillis()}_${Thread.currentThread().id}")
        tempFile.parentFile?.mkdirs()
        try {
            FileHelper.copyFile(context, uri, tempFile.absolutePath)
            val dFile = AppFileStore.importFile(context, tempFile, mimeType, deleteSrc = true)
            return AppFileStore.toFidUri(dFile.id, AppFileStore.extFromMime(dFile.mimeType))
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }
    fun importDownloadedFile(
        context: Context,
        srcFile: File,
        mimeType: String = "",
    ): String {
        val dFile = AppFileStore.importFile(context, srcFile, mimeType, deleteSrc = true)
        return AppFileStore.toFidUri(dFile.id, AppFileStore.extFromMime(dFile.mimeType))
    }
}