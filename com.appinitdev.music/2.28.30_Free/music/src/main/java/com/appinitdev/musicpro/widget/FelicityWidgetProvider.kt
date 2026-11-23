class FelicityWidgetProvider : BaseWidgetProvider() {
    override fun getLayoutId(): Int = R.layout.widget_blurred
    override fun applyStaticViews(scope: WidgetViewScope) {
        val views = scope.views
        val context = scope.context
        views.setTextViewText(R.id.widget_title, scope.title)
        views.setTextViewText(R.id.widget_artist, scope.artist)
        views.setTextColor(R.id.widget_title, Color.WHITE)
        views.setTextColor(R.id.widget_artist, Color.LTGRAY)
        val playPauseRes = if (scope.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        views.setImageViewResource(R.id.widget_play_pause, playPauseRes)
        views.setInt(R.id.widget_prev, "setColorFilter", Color.WHITE)
        views.setInt(R.id.widget_play_pause, "setColorFilter", Color.WHITE)
        views.setInt(R.id.widget_next, "setColorFilter", Color.WHITE)
        views.setInt(R.id.widget_prev, "setBackgroundColor", Color.TRANSPARENT)
        views.setInt(R.id.widget_play_pause, "setBackgroundColor", Color.TRANSPARENT)
        views.setInt(R.id.widget_next, "setBackgroundColor", Color.TRANSPARENT)
        views.setOnClickPendingIntent(
                R.id.widget_play_pause,
                scope.pendingIntentBuilder(context, WidgetActionReceiver.ACTION_PLAY_PAUSE)
        )
        views.setOnClickPendingIntent(
                R.id.widget_prev,
                scope.pendingIntentBuilder(context, WidgetActionReceiver.ACTION_PREV)
        )
        views.setOnClickPendingIntent(
                R.id.widget_next,
                scope.pendingIntentBuilder(context, WidgetActionReceiver.ACTION_NEXT)
        )
    }
    override fun applyAlbumArt(views: RemoteViews, bitmap: Bitmap?) {
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.widget_album_art, bitmap)
        } else {
            views.setImageViewResource(R.id.widget_album_art, R.mipmap.ic_launcher)
        }
    }
    override fun applyBackgroundArt(views: RemoteViews, bitmap: Bitmap?) {
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.widget_background_view, bitmap)
        }
    }
}
