data class FavoriteFolder(
    val rootPath: String,
    val fullPath: String,
    val alias: String? = null,
)
fun DFavoriteFolder.toModel(): FavoriteFolder {
    return FavoriteFolder(rootPath, fullPath, alias)
}