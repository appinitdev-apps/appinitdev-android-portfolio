class DatabaseBackupHelper @Inject constructor(
    private val posDatabase: POSDatabase 
) {

    private val DB_NAME = "pos_database"
    private val AUX_FILES = listOf("-wal", "-shm", "-journal")

    
    fun exportDatabase(context: Context, uri: Uri) {
        val dbFile = context.getDatabasePath(DB_NAME)

        
        posDatabase.close() 

        try {
            
            context.contentResolver.openOutputStream(uri)?.use { output ->
                dbFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            
            
            
            
            
            
            POSDatabase.getDatabase(context)
        }
    }

    
    
    fun importDatabase(context: Context, uri: Uri) {
        val dbFile: File = context.getDatabasePath(DB_NAME)

        try {
            
            
            posDatabase.close()

            
            AUX_FILES.forEach { suffix ->
                val file = File(dbFile.path + suffix)
                if (file.exists()) {
                    file.delete()
                }
            }

            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.i("BackupHelper", "Database import successful.")

        } catch (e: Exception) {
            e.printStackTrace()
            
            dbFile.delete()
            throw e 
        }
        
        
        
        
    }
}