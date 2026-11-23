 

internal fun Application.isMain(): Boolean =
    getProcessName().makeLog("Current Process") == packageName.makeLog("Current packageName")


@SuppressLint("PrivateApi")
internal fun Application.getProcessName(): String? {
    if (SDK_INT >= 28) {
        return Application.getProcessName()
    }

    
    try {
        val activityThread = Class.forName(
            "android.app.ActivityThread",
            false,
            this::class.java.classLoader
        )
        val packageName: Any?
        val currentProcessName = activityThread.getDeclaredMethod("currentProcessName")
        currentProcessName.isAccessible = true
        packageName = currentProcessName.invoke(null)
        if (packageName is String) {
            return packageName
        }
    } catch (exception: Throwable) {
        exception.makeLog()
    }

    
    val pid: Int = android.os.Process.myPid()
    val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager

    val processes = am.runningAppProcesses
    if (processes != null && processes.isNotEmpty()) {
        for (process in processes) {
            if (process.pid == pid) {
                return process.processName
            }
        }
    }

    return null
}