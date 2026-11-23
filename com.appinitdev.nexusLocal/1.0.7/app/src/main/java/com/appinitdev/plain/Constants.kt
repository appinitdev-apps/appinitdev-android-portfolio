object Constants {
    const val SSL_NAME = "nexuslocal"
    const val DATABASE_NAME = "nexuslocal.db"
    const val NOTIFICATION_CHANNEL_ID = "default"
    const val CHAT_NOTIFICATION_CHANNEL_ID = "peer_chat"
    const val MAX_READABLE_TEXT_FILE_SIZE = 10 * 1024 * 1024 
    const val SUPPORT_EMAIL = "appinitdev@gmail.com"
    const val ONE_DAY = 24 * 60 * 60L
    const val ONE_DAY_MS = ONE_DAY * 1000L
    const val KEY_STORE_FILE_NAME = "keystore.bks"
    const val MAX_MESSAGE_LENGTH = 2048 
    const val TEXT_FILE_SUMMARY_LENGTH = 250
    const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"
    const val ACTION_START_HTTP_SERVER = "${BuildConfig.APPLICATION_ID}.action.START_HTTP_SERVER"
    const val ACTION_STOP_HTTP_SERVER = "${BuildConfig.APPLICATION_ID}.action.STOP_HTTP_SERVER"
    const val ACTION_STOP_SCREEN_MIRROR = "${BuildConfig.APPLICATION_ID}.action.STOP_SCREEN_MIRROR"
    const val ACTION_PEER_CHAT_REPLY = "${BuildConfig.APPLICATION_ID}.action.PEER_CHAT_REPLY"
    const val ACTION_REPOST_HTTP_NOTIFICATION = "${BuildConfig.APPLICATION_ID}.action.REPOST_HTTP_NOTIFICATION"
    const val ACTION_PLAY_MEDIA = "${BuildConfig.APPLICATION_ID}.action.PLAY_MEDIA"
    const val EXTRA_MEDIA_PATH = "media_path"
}
