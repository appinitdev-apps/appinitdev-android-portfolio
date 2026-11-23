
// 1. Entidad de la Base de Datos
@Entity(
    tableName = "my_gallery",
    indices = [Index(value = ["localPath"], unique = true)]
)
data class GalleryImage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val localPath: String, // Ruta del archivo guardado internamente en la app
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "captured_works",
    indices = [Index(value = ["workPath"], unique = true)]
)
data class CapturedWork(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workPath: String, // Ruta de la foto final tomada (Cámara + Dibujo)
    val templatePath: String? = null, // Opcional: El dibujo que se usó
    val addedAt: Long = System.currentTimeMillis(),
    val isTimeLapse: Boolean = false, // NUEVO v4
    val sessionId: String? = null     // NUEVO v4 (UUID para agrupar fotos de una misma sesión)
)

// 2. DAO (Data Access Object)
@Dao
interface GalleryDao {
    // --- Lógica para Plantillas ---
    @Query("SELECT * FROM my_gallery ORDER BY addedAt DESC")
    fun getAllImages(): Flow<List<GalleryImage>>

    @Query("SELECT * FROM my_gallery ORDER BY addedAt DESC LIMIT 1")
    suspend fun getLatestImage(): GalleryImage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: GalleryImage)

    @Delete
    suspend fun deleteImage(image: GalleryImage)

    // --- Lógica para Fotos Capturadas (NUEVO) ---
    @Query("SELECT * FROM captured_works ORDER BY addedAt DESC")
    fun getAllCapturedWorks(): Flow<List<CapturedWork>>
    
    @Query("SELECT * FROM captured_works WHERE sessionId = :sessionId ORDER BY addedAt ASC")
    fun getWorksBySession(sessionId: String): Flow<List<CapturedWork>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapturedWork(work: CapturedWork)

    @Delete
    suspend fun deleteCapturedWork(work: CapturedWork)
}

// 3. Clase Base de Datos
@Database(entities = [GalleryImage::class, CapturedWork::class], version = 4, exportSchema = false)
abstract class GalleryDatabase : RoomDatabase() {
    abstract fun galleryDao(): GalleryDao

    companion object {
        @Volatile
        private var INSTANCE: GalleryDatabase? = null

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE captured_works ADD COLUMN isTimeLapse INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE captured_works ADD COLUMN sessionId TEXT")
            }
        }

        fun getDatabase(context: Context): GalleryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GalleryDatabase::class.java,
                    "artytrace_gallery_db"
                )
                .addMigrations(MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
