class DatabaseBackupHelper @Inject constructor(
    private val posDatabase: POSDatabase // Inyectamos la instancia Singleton de Room
) {

    private val DB_NAME = "pos_database"
    private val AUX_FILES = listOf("-wal", "-shm", "-journal")

    // --- EXPORTAR ---
    fun exportDatabase(context: Context, uri: Uri) {
        val dbFile = context.getDatabasePath(DB_NAME)

        // 1. Cerrar la base de datos usando la instancia inyectada para forzar el WAL commit
        posDatabase.close() // <-- ¡Usamos el método .close() de la instancia de Room!

        try {
            // ... (el resto del try-catch de exportación es el mismo)
            context.contentResolver.openOutputStream(uri)?.use { output ->
                dbFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            // 2. Reactivar la base de datos llamando a getDatabase
            // Nota: Aquí se asume que POSDatabase.getDatabase() reabre el Singleton.
            // Esto puede necesitar un cambio si el Singleton se gestiona solo por Hilt.
            // Una solución más simple es solo dejar que la aplicación acceda a ella
            // de nuevo a través de los repositorios.
            // Por ahora, lo dejaremos como estaba, pero el CERRADO es lo más importante.
            POSDatabase.getDatabase(context)
        }
    }

    // --- IMPORTAR ---
    // --- IMPORTAR (Método corregido) ---
    fun importDatabase(context: Context, uri: Uri) {
        val dbFile: File = context.getDatabasePath(DB_NAME)

        try {
            // 1. CERRAR TODAS LAS CONEXIONES activas a la base de datos (el Singleton de Hilt)
            //    Esto es OBLIGATORIO para reemplazar los archivos.
            posDatabase.close()

            // 2. Eliminar los archivos auxiliares de WAL
            AUX_FILES.forEach { suffix ->
                val file = File(dbFile.path + suffix)
                if (file.exists()) {
                    file.delete()
                }
            }

            // 3. Sobrescribir el archivo .db principal con el archivo importado
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.i("BackupHelper", "Database import successful.")

        } catch (e: Exception) {
            e.printStackTrace()
            // Si la importación falla, intentamos eliminar el archivo corrupto
            dbFile.delete()
            throw e // Lanzar para que la UI sepa del error
        }
        // 4. ¡NO HAY BLOQUE "finally"!
        // En este punto, la app está en un estado inestable.
        // El Singleton de Hilt está CERRADO.
        // La app DEBE ser reiniciada.
    }
}