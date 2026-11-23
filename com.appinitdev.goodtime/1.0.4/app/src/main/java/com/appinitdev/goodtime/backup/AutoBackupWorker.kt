
/**
 * WorkManager worker that performs the auto backup operation.
 * It retrieves the backup path from settings and calls BackupManager to perform the backup.
 */
class AutoBackupWorker(
    private val context: Context,
    private val backupManager: BackupManager,
    private val settingsRepository: SettingsRepository,
    private val logger: Logger,
    private val dbPath: String,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    override suspend fun doWork(): Result {

        try {
            val settings: AppSettings = settingsRepository.settings.first()
            val backupPath = settings.backupSettings.path

            if (!settings.backupSettings.autoBackupEnabled || backupPath.isBlank()) {
                return Result.failure()
            }

            backupManager.checkpointDatabase()
            val fileName = backupManager.generateBackupFileName(DB_AUTO_BACKUP_PREFIX)

            val backupDirUri = backupPath.toUri()
            val backupDir = DocumentFile.fromTreeUri(context, backupDirUri)
            if (backupDir == null || !backupDir.isDirectory) {
                // the directory was probably deleted
                return Result.failure()
            }

            backupDir.findFile(fileName)?.delete()

            val backupFile = backupDir.createFile("application/octet-stream", fileName)
            if (backupFile == null) {
                return Result.failure()
            }

            FileInputStream(dbPath).use { input ->
                context.contentResolver.openOutputStream(backupFile.uri)?.use { output ->
                    input.copyTo(output)
                } ?: run {
                    return Result.failure()
                }
            }

            backupDir
                .listFiles()
                .filter { it.isFile && it.name?.startsWith(DB_AUTO_BACKUP_PREFIX) == true }
                .sortedByDescending { it.lastModified() }
                .drop(7)
                .forEach { it.delete() }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "auto_backup_work"
        private const val DB_AUTO_BACKUP_PREFIX = "GT-AutoBackup-"
    }
}
