
@Entity(tableName = "favorites", indices = [Index(value = ["full_path"], unique = true)])
data class Favorite(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    @ColumnInfo(name = "full_path") var fullPath: String,
    @ColumnInfo(name = "filename") var filename: String,
    @ColumnInfo(name = "parent_path") var parentPath: String
)
