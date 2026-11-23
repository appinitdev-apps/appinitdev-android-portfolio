val Context.config: Config get() = Config.newInstance(applicationContext)
fun Context.isPathOnRoot(path: String) = !(path.startsWith(config.internalStoragePath) || isPathOnOTG(path) || (isPathOnSD(path)))
fun Context.getAllVolumeNames(): List<String> {
    val volumeNames = mutableListOf(PRIMARY_VOLUME_NAME)
    val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
    getExternalFilesDirs(null)
        .mapNotNull { storageManager.getStorageVolume(it) }
        .filterNot { it.isPrimary }
        .mapNotNull { it.uuid?.lowercase(Locale.US) }
        .forEach {
            volumeNames.add(it)
        }
    return volumeNames
}
