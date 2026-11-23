data class CloudBackupInfo(
    val fileName: String,
    val fileSizeMs: Long,
    val timestamp: Long,
    val formattedDate: String,
    val isSuccess: Boolean
)
class GoogleDriveBackupManager(private val context: Context) {
    private val dbName = "goodtime_db"
    fun createCloudBackupPayload(): CloudBackupInfo? {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) {
            val fallbackDb = File(context.filesDir.parentFile, "databases/$dbName")
            if (!fallbackDb.exists()) return null
        }
        val targetFile = if (dbFile.exists()) dbFile else File(context.filesDir.parentFile, "databases/$dbName")
        val backupDir = File(context.cacheDir, "cloud_backups")
        if (!backupDir.exists()) backupDir.mkdirs()
        val timeStampStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFileName = "goodtime_backup_$timeStampStr.db"
        val backupFile = File(backupDir, backupFileName)
        return try {
            FileInputStream(targetFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }
            CloudBackupInfo(
                fileName = backupFileName,
                fileSizeMs = backupFile.length(),
                timestamp = System.currentTimeMillis(),
                formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                isSuccess = true
            )
        } catch (_: Exception) {
            null
        }
    }
    fun restoreCloudBackupPayload(backupFile: File): Boolean {
        if (!backupFile.exists()) return false
        val targetDbFile = context.getDatabasePath(dbName)
        return try {
            targetDbFile.parentFile?.mkdirs()
            FileInputStream(backupFile).use { input ->
                FileOutputStream(targetDbFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
