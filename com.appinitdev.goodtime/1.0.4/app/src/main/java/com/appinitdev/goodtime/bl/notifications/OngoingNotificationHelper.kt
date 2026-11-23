
object OngoingNotificationHelper {

    private const val CHANNEL_ID = "goodtime_live_updates_channel"
    private const val CHANNEL_NAME = "Live Focus Updates"

    fun buildLiveProgressNotification(
        context: Context,
        labelName: String,
        remainingSec: Long,
        totalDurationSec: Long,
        isRunning: Boolean
    ): Notification {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingContent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mins = remainingSec / 60
        val secs = remainingSec % 60
        val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

        val maxProgress = totalDurationSec.toInt().coerceAtLeast(1)
        val currentProgress = (totalDurationSec - remainingSec).toInt().coerceIn(0, maxProgress)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_status_goodtime)
            .setContentTitle("Focus: $labelName")
            .setContentText(if (isRunning) "Remaining: $timeFormatted" else "Paused: $timeFormatted")
            .setProgress(maxProgress, currentProgress, false)
            .setOngoing(isRunning)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingContent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)

        return builder.build()
    }
}
