@Entity(
    tableName = "my_gallery",
    indices = [Index(value = ["localPath"], unique = true)]
)
data class GalleryImage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val localPath: String, 
    val addedAt: Long = System.currentTimeMillis()
)
@Entity(
    tableName = "captured_works",
    indices = [Index(value = ["workPath"], unique = true)]
)
data class CapturedWork(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workPath: String, 
    val templatePath: String? = null, 
    val addedAt: Long = System.currentTimeMillis(),
    val isTimeLapse: Boolean = false, 
    val sessionId: String? = null     
)
@Dao
interface GalleryDao {
    @Query("SELECT * FROM my_gallery ORDER BY addedAt DESC")
    fun getAllImages(): Flow<List<GalleryImage>>
    @Query("SELECT * FROM my_gallery ORDER BY addedAt DESC LIMIT 1")
    suspend fun getLatestImage(): GalleryImage?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: GalleryImage)
    @Delete
    suspend fun deleteImage(image: GalleryImage)
    @Query("SELECT * FROM captured_works ORDER BY addedAt DESC")
    fun getAllCapturedWorks(): Flow<List<CapturedWork>>
    @Query("SELECT * FROM captured_works WHERE sessionId = :sessionId ORDER BY addedAt ASC")
    fun getWorksBySession(sessionId: String): Flow<List<CapturedWork>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapturedWork(work: CapturedWork)
    @Delete
    suspend fun deleteCapturedWork(work: CapturedWork)
}
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
