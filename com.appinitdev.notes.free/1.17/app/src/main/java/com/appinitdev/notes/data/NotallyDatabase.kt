
@TypeConverters(Converters::class)
@Database(entities = [BaseNote::class, Label::class], version = 9)
abstract class NotallyDatabase : RoomDatabase() {

    abstract fun getLabelDao(): LabelDao

    abstract fun getCommonDao(): CommonDao

    abstract fun getBaseNoteDao(): BaseNoteDao

    fun checkpoint() {
        getBaseNoteDao().query(SimpleSQLiteQuery("pragma wal_checkpoint(FULL)"))
    }

    private var biometricLockObserver: Observer<BiometricLock>? = null
    private var dataInPublicFolderObserver: Observer<Boolean>? = null

    companion object {

        const val DATABASE_NAME = "NotallyDatabase"

        @Volatile private var instance: NotNullLiveData<NotallyDatabase>? = null

        fun getCurrentDatabaseFile(context: ContextWrapper): File {
            return if (NotallyXPreferences.getInstance(context).dataInPublicFolder.value) {
                getExternalDatabaseFile(context)
            } else {
                getInternalDatabaseFile(context)
            }
        }

        fun getExternalDatabaseFile(context: ContextWrapper): File {
            return File(context.getExternalMediaDirectory(), DATABASE_NAME)
        }

        fun getExternalDatabaseFiles(context: ContextWrapper): List<File> {
            return listOf(
                File(context.getExternalMediaDirectory(), DATABASE_NAME),
                File(context.getExternalMediaDirectory(), "$DATABASE_NAME-shm"),
                File(context.getExternalMediaDirectory(), "$DATABASE_NAME-wal"),
            )
        }

        fun getInternalDatabaseFile(context: Context): File {
            return context.getDatabasePath(DATABASE_NAME)
        }

        fun getInternalDatabaseFiles(context: ContextWrapper): List<File> {
            val directory = context.getDatabasePath(DATABASE_NAME).parentFile
            return listOf(
                File(directory, DATABASE_NAME),
                File(directory, "$DATABASE_NAME-shm"),
                File(directory, "$DATABASE_NAME-wal"),
            )
        }

        private fun getCurrentDatabaseName(context: ContextWrapper): String {
            return if (NotallyXPreferences.getInstance(context).dataInPublicFolder.value) {
                getExternalDatabaseFile(context).absolutePath
            } else {
                DATABASE_NAME
            }
        }

        fun getDatabase(
            context: ContextWrapper,
            observePreferences: Boolean = true,
        ): NotNullLiveData<NotallyDatabase> {
            return instance
                ?: synchronized(this) {
                    val preferences = NotallyXPreferences.getInstance(context)
                    this.instance =
                        NotNullLiveData(createInstance(context, preferences, observePreferences))
                    return this.instance!!
                }
        }

        fun getFreshDatabase(context: ContextWrapper): NotallyDatabase {
            return createInstance(context, NotallyXPreferences.getInstance(context), false)
        }

        private fun createInstance(
            context: ContextWrapper,
            preferences: NotallyXPreferences,
            observePreferences: Boolean,
        ): NotallyDatabase {
            val instanceBuilder = Room.databaseBuilder(
                context,
                NotallyDatabase::class.java,
                getCurrentDatabaseName(context)
            ).fallbackToDestructiveMigration()
                /*.addMigrations(
                    Migration2,
                    Migration3,
                    Migration4,
                    Migration5,
                    Migration6,
                    Migration7,
                    Migration8,
                    Migration9,
                    Migration9_10
                )*/

            val instance = instanceBuilder.build()

            // Observadores de preferencias (solo si quieres seguir observando cambios)
            if (observePreferences) {
                instance.biometricLockObserver = Observer {
                    NotallyDatabase.instance?.value?.biometricLockObserver?.let {
                        preferences.biometricLock.removeObserver(it)
                    }
                    val newInstance = createInstance(context, preferences, true)
                    NotallyDatabase.instance?.postValue(newInstance)
                    preferences.biometricLock.observeForeverSkipFirst(
                        newInstance.biometricLockObserver!!
                    )
                }
                preferences.biometricLock.observeForeverSkipFirst(instance.biometricLockObserver!!)

                instance.dataInPublicFolderObserver = Observer {
                    NotallyDatabase.instance?.value?.dataInPublicFolderObserver?.let {
                        preferences.dataInPublicFolder.removeObserver(it)
                    }
                    val newInstance = createInstance(context, preferences, true)
                    NotallyDatabase.instance?.postValue(newInstance)
                    preferences.dataInPublicFolder.observeForeverSkipFirst(
                        newInstance.dataInPublicFolderObserver!!
                    )
                }
                preferences.dataInPublicFolder.observeForeverSkipFirst(instance.dataInPublicFolderObserver!!)
            }

            return instance
        }

        val Migration9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Renombrar tabla antigua
                database.execSQL("ALTER TABLE BaseNote RENAME TO BaseNote_old")

                // 2. Crear tabla nueva con todos los defaults correctos
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS BaseNote (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL DEFAULT 'undefined',
                folder TEXT NOT NULL DEFAULT 'undefined',
                color TEXT NOT NULL DEFAULT 'undefined',
                title TEXT NOT NULL DEFAULT 'undefined',
                pinned INTEGER NOT NULL DEFAULT 0,
                timestamp INTEGER NOT NULL DEFAULT 0,
                modifiedTimestamp INTEGER NOT NULL DEFAULT (strftime('%s','now')),
                labels TEXT NOT NULL DEFAULT '[]',
                body TEXT NOT NULL DEFAULT '',
                spans TEXT NOT NULL DEFAULT '[]',
                items TEXT NOT NULL DEFAULT '[]',
                images TEXT NOT NULL DEFAULT '[]',
                files TEXT NOT NULL DEFAULT '[]',
                audios TEXT NOT NULL DEFAULT '[]',
                reminders TEXT NOT NULL DEFAULT '[]',
                reminder TEXT,
                viewMode TEXT NOT NULL DEFAULT 'EDIT'
            )
        """.trimIndent())

                // 3. Copiar datos de la tabla antigua a la nueva
                database.execSQL("""
            INSERT INTO BaseNote (
                id, type, folder, color, title, pinned, timestamp, modifiedTimestamp,
                labels, body, spans, items, images, files, audios, reminders, reminder, viewMode
            )
            SELECT 
                id, type, folder, color, title, pinned, timestamp, modifiedTimestamp,
                labels, body, spans, items, images, files, audios, reminders, reminder, viewMode
            FROM BaseNote_old
        """.trimIndent())

                // 4. Eliminar la tabla antigua
                database.execSQL("DROP TABLE BaseNote_old")
            }
        }






        object Migration2 : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `BaseNote` ADD COLUMN `color` TEXT NOT NULL DEFAULT 'DEFAULT'"
                )
            }
        }

        object Migration3 : Migration(2, 3) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `BaseNote` ADD COLUMN `images` TEXT NOT NULL DEFAULT `[]`")
            }
        }

        object Migration4 : Migration(3, 4) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `BaseNote` ADD COLUMN `audios` TEXT NOT NULL DEFAULT `[]`")
            }
        }

        object Migration5 : Migration(4, 5) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `BaseNote` ADD COLUMN `files` TEXT NOT NULL DEFAULT `[]`")
            }
        }

        object Migration6 : Migration(5, 6) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `BaseNote` ADD COLUMN `modifiedTimestamp` INTEGER NOT NULL DEFAULT 'timestamp'"
                )
            }
        }

        object Migration7 : Migration(6, 7) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `BaseNote` ADD COLUMN `reminders` TEXT NOT NULL DEFAULT `[]`"
                )
            }
        }

        object Migration8 : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val cursor = db.query("SELECT id, color FROM BaseNote")
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    val colorString = cursor.getString(cursor.getColumnIndexOrThrow("color"))
                    val color = Color.valueOfOrDefault(colorString)
                    val hexColor = color.toColorString()
                    db.execSQL("UPDATE BaseNote SET color = ? WHERE id = ?", arrayOf(hexColor, id))
                }
                cursor.close()
            }
        }

        object Migration9 : Migration(8, 9) {

            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `BaseNote` ADD COLUMN `viewMode` TEXT NOT NULL DEFAULT '${NoteViewMode.EDIT.name}'"
                )
            }
        }
    }
}
