
object AppHelper {
    private val fileIcons = mutableSetOf<String>()

    fun relaunch(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    fun foregrounded(): Boolean {
        val appProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(appProcessInfo)
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE)
    }



    fun getCacheSize(context: Context): Long {
        return calculateDirectorySize(context.cacheDir) + calculateDirectorySize(
            context.filesDir.resolve(
                "image_cache"
            )
        )
    }

    private fun calculateDirectorySize(directory: File): Long {
        var totalSize: Long = 0
        val files = directory.listFiles() ?: return 0L
        for (file in files) {
            totalSize += if (file.isDirectory) {
                calculateDirectorySize(file)
            } else {
                file.length()
            }
        }

        return totalSize
    }

    fun clearCacheAsync(context: Context) {
        context.cacheDir.listFiles()?.forEach {
            it.deleteRecursively()
        }
        context.filesDir.resolve("image_cache").listFiles()?.forEach {
            it.deleteRecursively()
        }
        context.filesDir.resolve("upload_tmp").listFiles()?.forEach {
            it.deleteRecursively()
        }
    }

    fun getFileIconPath(extension: String): String {
        if (fileIcons.isEmpty()) {
            cacheIconKeys(MainApp.instance)
        }
        if (!fileIcons.contains(extension)) {
            return "file:///android_asset/ficons/default.svg"
        }

        return "file:///android_asset/ficons/$extension.svg"
    }

    private fun cacheIconKeys(context: Context) {
        context.assets.list("ficons")?.forEach {
            fileIcons.add(it.substringBefore("."))
        }
    }
}
