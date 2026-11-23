@Module
@InstallIn(SingletonComponent::class)
object BackupModule {
    @Provides
    @Singleton
    fun provideDatabaseBackupHelper(
        posDatabase: POSDatabase 
    ): DatabaseBackupHelper {
        return DatabaseBackupHelper(posDatabase)
    }
}