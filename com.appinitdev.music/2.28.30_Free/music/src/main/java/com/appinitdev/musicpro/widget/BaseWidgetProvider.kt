abstract class BaseWidgetProvider : AppWidgetProvider() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    abstract fun getLayoutId(): Int
    abstract fun applyStaticViews(scope: WidgetViewScope)
    abstract fun applyAlbumArt(views: RemoteViews, bitmap: Bitmap?)
    abstract fun applyBackgroundArt(views: RemoteViews, bitmap: Bitmap?)
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                handleReceive(context, intent)
            } finally {
                pendingResult.finish()
            }
        }
    }
    private suspend fun handleReceive(context: Context, intent: Intent) {
        val manager = AppWidgetManager.getInstance(context)
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                    ?: manager.getAppWidgetIds(ComponentName(context, this::class.java))
                ids.forEach { id -> pushUpdate(context, manager, id) }
            }
            ACTION_WIDGET_UPDATE -> {
                WidgetStatePrefs.save(
                        context,
                        title = intent.getStringExtra(EXTRA_TITLE) ?: "",
                        artist = intent.getStringExtra(EXTRA_ARTIST) ?: "",
                        isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false),
                        songId = intent.getLongExtra(EXTRA_SONG_ID, -1L)
                )
                val ids = manager.getAppWidgetIds(ComponentName(context, this::class.java))
                ids.forEach { id -> pushUpdate(context, manager, id) }
            }
            else -> {
                withContext(Dispatchers.Main) { super.onReceive(context, intent) }
            }
        }
    }
    protected open suspend fun pushUpdate(context: Context, manager: AppWidgetManager, widgetId: Int) {
        SharedPreferences.init(context)
        val title = WidgetStatePrefs.getTitle(context).ifEmpty { "Unknown" }
        val artist = WidgetStatePrefs.getArtist(context).ifEmpty { "Unknown" }
        val isPlaying = WidgetStatePrefs.isPlaying(context)
        val songId = WidgetStatePrefs.getSongId(context)
        val views = RemoteViews(context.packageName, getLayoutId())
        val viewScope = WidgetViewScope(
                context = context,
                views = views,
                title = title,
                artist = artist,
                isPlaying = isPlaying,
                pendingIntentBuilder = ::buildPendingIntent
        )
        applyStaticViews(viewScope)
        manager.updateAppWidget(widgetId, views)
        val bgBitmap = loadBackgroundArt(context, manager, widgetId, songId)
        applyBackgroundArt(views, bgBitmap)
        manager.updateAppWidget(widgetId, views)
        val artBitmap = loadAlbumArt(context, songId)
        applyAlbumArt(views, artBitmap)
        manager.updateAppWidget(widgetId, views)
    }
    protected open suspend fun loadAlbumArt(context: Context, songId: Long): Bitmap? =
        withContext(Dispatchers.IO) {
            if (songId == -1L) return@withContext null
            runCatching {
                val audio = AudioDatabase.getInstance(context.applicationContext)
                    .audioDao()
                    ?.getAudioById(songId)
                    ?: return@withContext null
                context.getArtCoverForWidget(
                        audio,
                        height = getAlbumArtSize(context),
                        width = getAlbumArtSize(context),
                        shadow = false,
                        blur = false,
                        greyscale = AlbumArtPreferences.isGreyscaleEnabled(),
                        darken = false,
                        crop = true,
                        roundedCorners = AlbumArtPreferences.isRoundedCornersEnabled()
                )
            }.getOrNull()
        }
    protected open suspend fun loadBackgroundArt(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            songId: Long
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (songId == -1L) return@withContext null
        runCatching {
            val audio = AudioDatabase.getInstance(context.applicationContext)
                .audioDao()
                ?.getAudioById(songId)
                ?: return@withContext null
            val density = context.resources.displayMetrics.density
            val options = manager.getAppWidgetOptions(widgetId)
            val widthPx = (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 294) * density)
                .toInt().coerceAtLeast(8)
            val heightPx = (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 50) * density)
                .toInt().coerceAtLeast(8)
            context.getArtCoverForWidget(
                    audio,
                    height = heightPx,
                    width = widthPx,
                    shadow = false,
                    blur = true,
                    greyscale = AlbumArtPreferences.isGreyscaleEnabled(),
                    darken = true,
                    crop = true,
                    roundedCorners = AlbumArtPreferences.isRoundedCornersEnabled()
            )
        }.getOrNull()
    }
    protected open fun getAlbumArtSize(context: Context): Int =
        context.resources.getDimensionPixelSize(com.appinitdev.musicpro.R.dimen.album_art_dimen)
    protected fun buildPendingIntent(context: Context, action: String): PendingIntent =
        PendingIntent.getBroadcast(
                context,
                action.hashCode(),
                Intent(action, null, context, WidgetActionReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    companion object {
        const val ACTION_WIDGET_UPDATE = "app.simple.felicity.ACTION_WIDGET_UPDATE"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_SONG_ID = "extra_song_id"
    }
}
