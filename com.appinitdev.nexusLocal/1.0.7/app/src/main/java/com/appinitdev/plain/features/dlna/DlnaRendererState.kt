enum class DlnaPlaybackState { NO_MEDIA_PRESENT, STOPPED, PLAYING, PAUSED, TRANSITIONING }
object DlnaRendererState {
    val isRunning = MutableStateFlow(false)
    val mediaUri = MutableStateFlow("")
    val mediaTitle = MutableStateFlow("")
    val mediaAlbumArtUri = MutableStateFlow("")
    val mediaType = MutableStateFlow(DlnaMediaType.UNKNOWN)
    val playbackState = MutableStateFlow(DlnaPlaybackState.NO_MEDIA_PRESENT)
    val port = MutableStateFlow(7878)
    val currentPositionMs = MutableStateFlow(0L)
    val durationMs = MutableStateFlow(0L)
    val seekTargetMs = MutableStateFlow<Long?>(null)
    val commandChannel = Channel<DlnaCommand>(Channel.UNLIMITED)
    val rawPendingCastRequest = MutableStateFlow<PendingCastRequest?>(null)
    val pendingCastRequest = MutableStateFlow<PendingCastRequest?>(null)
    val pendingPlayQueued = MutableStateFlow(false)
    val startError = MutableStateFlow("")
    fun reset() {
        mediaUri.value = ""
        mediaTitle.value = ""
        mediaAlbumArtUri.value = ""
        mediaType.value = DlnaMediaType.UNKNOWN
        playbackState.value = DlnaPlaybackState.NO_MEDIA_PRESENT
        currentPositionMs.value = 0L
        durationMs.value = 0L
        seekTargetMs.value = null
        rawPendingCastRequest.value = null
        pendingCastRequest.value = null
        pendingPlayQueued.value = false
        startError.value = ""
    }
    fun formatPositionInfo(): Pair<String, String> {
        val pos = currentPositionMs.value
        val dur = durationMs.value
        return Pair(formatTime(pos), if (dur > 0) formatTime(dur) else "00:00:00")
    }
    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }
}
