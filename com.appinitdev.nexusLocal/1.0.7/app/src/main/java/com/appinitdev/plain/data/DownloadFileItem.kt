
@Serializable
data class DownloadFileItem(val path: String, val name: String = "")
data class DownloadFileItemWrap(val file: File, val name: String)
