data class ScreensaverState(
    val formattedTime: String,
    val labelName: String,
    val isRunning: Boolean,
    val isNightMode: Boolean = true
)
object FlipClockScreensaver {
    fun formatSecondsToFlipTime(remainingSeconds: Long): Pair<String, String> {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val minutesStr = String.format(java.util.Locale.getDefault(), "%02d", minutes)
        val secondsStr = String.format(java.util.Locale.getDefault(), "%02d", seconds)
        return Pair(minutesStr, secondsStr)
    }
    fun buildScreensaverState(
        remainingSeconds: Long,
        labelName: String,
        isRunning: Boolean
    ): ScreensaverState {
        val (mins, secs) = formatSecondsToFlipTime(remainingSeconds)
        return ScreensaverState(
            formattedTime = "$mins:$secs",
            labelName = labelName,
            isRunning = isRunning,
            isNightMode = true
        )
    }
}
