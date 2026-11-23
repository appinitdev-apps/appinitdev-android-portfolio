


@Entity(tableName = "date_takens", indices = [Index(value = ["full_path"], unique = true)])
data class DateTaken(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "full_path") var fullPath: String,
    @ColumnInfo(name = "filename") var filename: String,
    @ColumnInfo(name = "parent_path") var parentPath: String,
    @ColumnInfo(name = "date_taken") var taken: Long,
    @ColumnInfo(name = "last_fixed") var lastFixed: Int,
    @ColumnInfo(name = "last_modified") var lastModified: Long
)
