
@Entity(tableName = "captures")
data class CaptureEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagePath: String,
    val videoName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val captureType: String = "Captura manual"
)

@Dao
interface CaptureDao {
    @Query("SELECT * FROM captures ORDER BY timestamp DESC")
    fun getAllCaptures(): Flow<List<CaptureEntity>>

    @Query("SELECT * FROM captures WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteCaptures(): Flow<List<CaptureEntity>>

    @Insert
    suspend fun insertCapture(capture: CaptureEntity): Long

    @Query("UPDATE captures SET isFavorite = :isFav WHERE id = :captureId")
    suspend fun updateFavorite(captureId: Int, isFav: Boolean)

    @Query("UPDATE captures SET videoName = :newName, captureType = :newTag WHERE id = :captureId")
    suspend fun updateCaptureNameAndTag(captureId: Int, newName: String, newTag: String)

    @Delete
    suspend fun deleteCaptures(captures: List<CaptureEntity>)

    @Query("DELETE FROM captures WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)
}

@Database(entities = [CaptureEntity::class], version = 3)
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
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}