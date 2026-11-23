
@Module
@InstallIn(SingletonComponent::class)
object BackupModule {

    @Provides
    @Singleton
    fun provideDatabaseBackupHelper(
        posDatabase: POSDatabase // Hilt inyecta el Singleton de Room
    ): DatabaseBackupHelper {
        return DatabaseBackupHelper(posDatabase)
    }
}