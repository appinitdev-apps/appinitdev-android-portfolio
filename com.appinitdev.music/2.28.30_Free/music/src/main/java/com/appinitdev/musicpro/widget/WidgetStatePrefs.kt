object WidgetStatePrefs {
    private const val PREFS_NAME = "felicity_widget_state"
    private const val KEY_TITLE = "widget_title"
    private const val KEY_ARTIST = "widget_artist"
    private const val KEY_IS_PLAYING = "widget_is_playing"
    private const val KEY_SONG_ID = "widget_song_id"
    fun save(context: Context, title: String?, artist: String?, isPlaying: Boolean, songId: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_TITLE, title ?: "")
            putString(KEY_ARTIST, artist ?: "")
            putBoolean(KEY_IS_PLAYING, isPlaying)
            putLong(KEY_SONG_ID, songId)
            apply()
        }
    }
    fun getTitle(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TITLE, "") ?: ""
    fun getArtist(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ARTIST, "") ?: ""
    fun isPlaying(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IS_PLAYING, false)
    fun getSongId(context: Context): Long =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_SONG_ID, -1L)
}
