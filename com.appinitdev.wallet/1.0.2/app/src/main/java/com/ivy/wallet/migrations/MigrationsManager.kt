class MigrationsManager @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val disableGitHubAutoBackupMigration: Lazy<DisableGitHubAutoBackupMigration>,
) {
    private val migrations by lazy {
        listOf(
            disableGitHubAutoBackupMigration.get(),
        )
    }
    @Suppress("TooGenericExceptionCaught", "PrintStackTrace", "MagicNumber")
    suspend fun executeMigrations() {
        delay(2_000L) 
        val data = context.dataStore.data.firstOrNull()
        for (migration in migrations) {
            val key = booleanPreferencesKey("migration_${migration.key}")
            val isExecuted = data?.get(key) ?: false
            if (!isExecuted) {
                Timber.i("[MIGRATION] Executing '${migration.key}' migration...")
                try {
                    withContext(Dispatchers.IO) {
                        migration.migrate()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                context.dataStore.edit {
                    it[key] = true
                }
                Timber.i("[MIGRATION] Finished '${migration.key}' migration.")
            }
        }
    }
}
