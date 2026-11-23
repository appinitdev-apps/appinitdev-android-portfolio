
@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagePath: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val isCustomImage: Boolean = true,
    val category: String = "Mis Fotos"
)

@Entity(
    tableName = "puzzle_stats",
    foreignKeys = [
        ForeignKey(
            entity = PuzzleEntity::class,
            parentColumns = ["id"],
            childColumns = ["puzzleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("puzzleId")]
)
data class PuzzleStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val puzzleId: Int,
    val n: Int,
    val moves: Int,
    val time: Int, // Segundos
    val date: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "puzzle_progress",
    primaryKeys = ["puzzleId", "n"]
)
data class PuzzleProgressEntity(
    val puzzleId: Int,
    val n: Int,
    val moves: Int,
    val time: Int,
    val state: String
)

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzles ORDER BY dateAdded DESC")
    fun getAllPuzzles(): Flow<List<PuzzleEntity>>

    @Insert
    suspend fun insert(puzzle: PuzzleEntity): Long

    @Delete
    suspend fun delete(puzzle: PuzzleEntity)

    @Query("DELETE FROM puzzles WHERE id IN (:ids)")
    suspend fun deleteMultiple(ids: List<Int>)

    // Estadísticas
    @Insert
    suspend fun insertStat(stat: PuzzleStatEntity)

    @Query("SELECT * FROM puzzle_stats WHERE puzzleId = :puzzleId AND n = :n ORDER BY time ASC, moves ASC LIMIT 1")
    suspend fun getBestStat(puzzleId: Int, n: Int): PuzzleStatEntity?

    @Query("SELECT * FROM puzzle_stats WHERE puzzleId = :puzzleId ORDER BY date DESC")
    fun getStatsForPuzzle(puzzleId: Int): Flow<List<PuzzleStatEntity>>

    // Progreso
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: PuzzleProgressEntity)

    @Query("SELECT * FROM puzzle_progress WHERE puzzleId = :puzzleId AND n = :n")
    suspend fun getProgress(puzzleId: Int, n: Int): PuzzleProgressEntity?

    @Query("DELETE FROM puzzle_progress WHERE puzzleId = :puzzleId AND n = :n")
    suspend fun deleteProgress(puzzleId: Int, n: Int)
}

val PREPOPULATE_CALLBACK = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        NumberPuzzleMode.entries.forEach { mode ->
            db.execSQL(
                """
                INSERT INTO puzzles (id, imagePath, dateAdded, isCustomImage, category) 
                VALUES (${mode.puzzleId}, '${mode.displayName}', ${System.currentTimeMillis()}, 0, 'Modos Numéricos')
                """.trimIndent()
            )
        }
    }
}

@Database(
    entities = [PuzzleEntity::class, PuzzleStatEntity::class, PuzzleProgressEntity::class],
    version = 6
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao
}
