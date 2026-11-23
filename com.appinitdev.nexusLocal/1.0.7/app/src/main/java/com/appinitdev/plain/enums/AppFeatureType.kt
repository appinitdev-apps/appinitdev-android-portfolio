
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
//    CALLS,
    CONTACTS,
//    SMS,
    SOUND_METER,
    POMODORO_TIMER,
    NOTIFICATIONS,
//    CHECK_UPDATES,
    MIRROR_AUDIO,
    MEDIA_TRASH,
//    DONATION,
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
                isRPlus() // Android 11+
            }

//            CHECK_UPDATES -> {
//                BuildConfig.CHANNEL == AppChannelType.GITHUB.name
//            }

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
