/* #AppInitDev -> Photo Utility Hub */


internal fun ImageToolboxApplication.attachLogWriter() {
    Logger.attachLogWriter(
        context = this@attachLogWriter,
        fileProvider = getString(R.string.file_provider),
        logsFilename = "image_toolbox_logs.txt",
        startupLog = Logger.Log(
            tag = "Device Info",
            message = "--${DeviceInfo.get()}--",
            level = Logger.Level.Info
        ),
        errorHandler = analyticsManager::sendReport,
        isSyncCreate = false
    )
}