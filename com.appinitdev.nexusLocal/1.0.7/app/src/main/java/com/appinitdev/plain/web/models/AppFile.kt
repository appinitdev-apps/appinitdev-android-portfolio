
data class AppFile(
    val id: ID,
    val size: Long,
    val mimeType: String,
    val realPath: String,
    val fileName: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun DAppFile.toModel(fileName: String): AppFile {
    return AppFile(ID(id), size, mimeType, realPath, fileName, createdAt, updatedAt)
}
