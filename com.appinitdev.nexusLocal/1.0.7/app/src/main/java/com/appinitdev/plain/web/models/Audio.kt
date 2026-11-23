data class Audio(
    val id: ID,
    val title: String,
    val artist: String,
    val path: String,
    val duration: Long,
    val size: Long,
    val bucketId: String,
    val albumFileId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isFavorite: Boolean,
)
data class PlaylistAudio(
    val title: String,
    val artist: String,
    val path: String,
    val duration: Long,
)
fun DAudio.toModel(): Audio {
    return Audio(ID(id), title, artist, path, duration, size, bucketId, FileHelper.getFileId(getAlbumUri().toString()), createdAt, updatedAt, isFavorite)
}
fun DPlaylistAudio.toModel(): PlaylistAudio {
    return PlaylistAudio(title, artist, path, duration)
}
