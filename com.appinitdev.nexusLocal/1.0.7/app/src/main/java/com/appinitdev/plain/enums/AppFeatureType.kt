
enum class AppFeatureType {
    WEB_PORTAL,
    DOCS,
    NOTES,
    FEEDS,
    APPS,
    FILES,
    IMAGES,
    CHAT,
    AUDIO,
    VIDEOS,

    CONTACTS,

    SOUND_METER,
    POMODORO_TIMER,
    NOTIFICATIONS,

    MIRROR_AUDIO,
    MEDIA_TRASH,

    DLNA_RECEIVER;

    fun has(): Boolean {
        return when (this) {
            APPS, NOTIFICATIONS -> {
                BuildConfig.CHANNEL != AppChannelType.GOOGLE.name
            }

            MIRROR_AUDIO -> {
                isQPlus()
            }
            MEDIA_TRASH -> {
                isRPlus() 
            }





            else -> true
        }
    }

    fun hasPermission(context: Context): Boolean {
        val p = getPermission()
        if (p != null) {
            return Permissions.allCan(context, p.permissions)
        }

        return true
    }

    fun getPermission(): DFeaturePermission? {
        return when (this) {
            FILES -> {
                DFeaturePermission(setOf(Permission.WRITE_EXTERNAL_STORAGE), Permission.WRITE_EXTERNAL_STORAGE)
            }

            CONTACTS -> {
                DFeaturePermission(setOf(Permission.READ_CONTACTS, Permission.WRITE_CONTACTS), Permission.READ_CONTACTS)
            }



            else -> {
                null
            }
        }
    }
}
