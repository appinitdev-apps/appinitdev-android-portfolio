
class DisableGitHubAutoBackupMigration @Inject constructor(
    @ApplicationContext
    private val context: Context,
) : Migration {
    override val key: String
        get() = "disable_github_auto_backups"

    override suspend fun migrate() {
        // Cancel the GitHub worker
        WorkManager.getInstance(context).cancelUniqueWork("GITHUB_AUTO_BACKUP_WORK")

        context.dataStore.edit {
            it.remove(DatastoreKeys.GITHUB_PAT)
            it.remove(DatastoreKeys.GITHUB_REPO)
            it.remove(DatastoreKeys.GITHUB_OWNER)
            it.remove(DatastoreKeys.GITHUB_LAST_BACKUP_EPOCH_SEC)
        }
    }
}
