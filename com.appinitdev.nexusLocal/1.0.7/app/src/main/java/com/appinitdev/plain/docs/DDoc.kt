
data class DDoc(
    override var id: String,
    override var title: String,
    override var path: String,
    override val duration: Long,
    val size: Long,
    val bucketId: String = "",
    val createdAt: Instant,
    val updatedAt: Instant,
) : IMedia, IData {
    val extension: String by lazy { path.getFilenameExtension() }
}
