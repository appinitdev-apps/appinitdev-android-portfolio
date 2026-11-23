
/**
 * Manager for scheduling and canceling auto backup operations.
 * It observes the BackupSettings from SettingsRepository and schedules or cancels
 * the backup work accordingly.
 */
class AutoBackupManager(
    context: Context,
    private val settingsRepository: SettingsRepository,
    private val logger: Logger,
) {
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        logger.i { "AutoBackupManager initialized" }
        observeBackupSettings()
    }

    private fun observeBackupSettings() {
        scope.launch {
            settingsRepository.settings
                .map { it.backupSettings }
                .distinctUntilChanged()
                .collect { backupSettings ->
                    handleBackupSettingsChange(backupSettings)
                }
        }
    }

    private fun handleBackupSettingsChange(backupSettings: BackupSettings) {
        logger.i { "Backup settings changed: autoBackupEnabled=${backupSettings.autoBackupEnabled}, path=${backupSettings.path}" }

        if (backupSettings.autoBackupEnabled && backupSettings.path.isNotBlank()) {
            scheduleBackup()
            logger.i { "Auto backup scheduled with path: ${backupSettings.path}" }
        } else {
            cancelBackup()
            logger.i { "Auto backup canceled" }
        }
    }

    private fun scheduleBackup() {
        val constraints =
            Constraints
                .Builder()
                .setRequiresCharging(true)
                .build()

        val backupWorkRequest =
            PeriodicWorkRequestBuilder<AutoBackupWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS,
            ).setInitialDelay(5, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                .build()

        workManager.enqueueUniquePeriodicWork(
            AutoBackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            backupWorkRequest,
        )
    }

    private fun cancelBackup() {
        logger.i { "Auto backup canceled" }
        workManager.cancelUniqueWork(AutoBackupWorker.WORK_NAME)
    }
}
