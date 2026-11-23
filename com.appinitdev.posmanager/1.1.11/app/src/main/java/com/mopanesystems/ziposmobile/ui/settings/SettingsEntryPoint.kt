
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SettingsEntryPoint {
    fun databaseBackupHelper(): DatabaseBackupHelper
}