
@Entity(tableName = "captures")
data class CaptureEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagePath: String, // Guardaremos la ruta absoluta del archivo
    val videoName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface CaptureDao {
    @Query("SELECT * FROM captures ORDER BY timestamp DESC")
    fun getAllCaptures(): Flow<List<CaptureEntity>>

    @Insert
    suspend fun insertCapture(capture: CaptureEntity)
}

@Database(entities = [CaptureEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun captureDao(): CaptureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "videotophoto_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}