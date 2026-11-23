
 

















data class WidgetViewScope(
        val context: Context,
        val views: RemoteViews,
        val title: String,
        val artist: String,
        val isPlaying: Boolean,
        val pendingIntentBuilder: (Context, String) -> PendingIntent
)

