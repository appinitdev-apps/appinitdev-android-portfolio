object PinnedNotificationManager {
    private const val CHANNEL_ID = "pinned_notes_channel"
    private const val CHANNEL_NAME = "Pinned Notes"
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for notes pinned to status bar"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun pinNoteToNotification(
        context: Context,
        noteId: Long,
        title: String,
        content: String,
        type: Type
    ) {
        createNotificationChannel(context)
        val targetClass = if (type == Type.LIST) EditListActivity::class.java else EditNoteActivity::class.java
        val openIntent = Intent(context, targetClass).apply {
            putExtra(EditActivity.EXTRA_SELECTED_BASE_NOTE, noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val displayTitle = if (title.isBlank()) context.getString(R.string.note) else title
        val displayContent = if (content.isBlank()) context.getString(R.string.note) else content
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.pin)
            .setContentTitle(displayTitle)
            .setContentText(displayContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(displayContent))
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(noteId.toInt(), notification)
    }
    fun unpinNoteNotification(context: Context, noteId: Long) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(noteId.toInt())
    }
}
