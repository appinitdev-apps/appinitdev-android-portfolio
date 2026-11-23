
data class DownloadTask(
    val id: String,
    val messageFile: DMessageFile,
    val peer: DPeer,
    val messageId: String,
    var status: DownloadStatus = DownloadStatus.PENDING,
    var error: String = "",
    var downloadedSize: Long = 0,
    var downloadSpeed: Long = 0,
    var lastDownloadedSize: Long = 0,
    var lastUpdateTime: Long? = null,
    var httpClient: OkHttpClient? = null,
    var aborted: Boolean = false
)  {
    fun isDownloading(): Boolean {
        return status == DownloadStatus.PENDING || status == DownloadStatus.DOWNLOADING
    }

     
    fun isActive(): Boolean {
        return setOf(DownloadStatus.PENDING, DownloadStatus.DOWNLOADING, DownloadStatus.PAUSED, DownloadStatus.FAILED).contains(status)
    }
}