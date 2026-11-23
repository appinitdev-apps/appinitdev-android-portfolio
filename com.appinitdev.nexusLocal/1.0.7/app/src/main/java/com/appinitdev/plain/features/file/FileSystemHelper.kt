object FileSystemHelper {
    private val physicalPaths =
        arrayListOf(
            "/storage/sdcard1", 
            "/storage/extsdcard", 
            "/storage/sdcard0/external_sdcard", 
            "/mnt/extsdcard", "/mnt/sdcard/external_sd", 
            "/mnt/external_sd", "/mnt/media_rw/sdcard1", 
            "/removable/microsd", 
            "/mnt/emmc", "/storage/external_SD", 
            "/storage/ext_sd", 
            "/storage/removable/sdcard1", 
            "/data/sdext", "/data/sdext2", "/data/sdext3", "/data/sdext4", "/sdcard1", 
            "/sdcard2", 
            "/storage/usbdisk0",
            "/storage/usbdisk1",
            "/storage/usbdisk2",
        )
    fun getInternalStorageStats(): DStorageStatsItem {
        val stats = DStorageStatsItem()
        val uuid = StorageManager.UUID_DEFAULT
        stats.totalBytes = storageStatsManager.getTotalBytes(uuid)
        stats.freeBytes = storageStatsManager.getFreeBytes(uuid)
        return stats
    }
    fun getSDCardStorageStats(context: Context): DStorageStatsItem {
        return getStorageStats(getSDCardPath(context))
    }
    fun getUSBStorageStats(): List<DStorageStatsItem> {
        return getUsbDiskPaths().map { getStorageStats(it) }
    }
    private fun getStorageStats(path: String): DStorageStatsItem {
        if (path.isNotEmpty()) {
            val stat = StatFs(path)
            val availableBytes = stat.blockSizeLong * stat.availableBlocksLong
            val totalBytes = stat.blockSizeLong * stat.blockCountLong
            return DStorageStatsItem(totalBytes, availableBytes)
        }
        return DStorageStatsItem(0, 0)
    }
    fun getInternalStoragePath(): String {
        return (
                if (isRPlus()) {
                    storageManager.primaryStorageVolume.directory?.path
                } else {
                    null
                }
                ) ?: Environment.getExternalStorageDirectory()?.absolutePath?.trimEnd('/') ?: ""
    }
    fun getInternalStorageName(): String {
        return LocaleHelper.getString(Res.string.internal_storage)
    }
    fun getSDCardPath(context: Context): String {
        val internalPath = getInternalStoragePath()
        val directories =
            getStorageDirectories(context).filter {
                it != internalPath &&
                        !it.equals(
                            "/storage/emulated/0",
                            true,
                        )
            }
        val fullSDPattern = Pattern.compile("^/storage/[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$")
        var sdCardPath =
            directories.firstOrNull { fullSDPattern.matcher(it).matches() }
                ?: directories.firstOrNull { !physicalPaths.contains(it.lowercase()) } ?: ""
        if (sdCardPath.isEmpty()) {
            val sdPattern = Pattern.compile("^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$")
            try {
                File("/storage").listFiles()?.forEach {
                    if (sdPattern.matcher(it.name).matches()) {
                        sdCardPath = "/storage/${it.name}"
                    }
                }
            } catch (e: Exception) {
            }
        }
        return sdCardPath.trimEnd('/')
    }
    fun getUsbDiskPaths(): List<String> {
        val storageVolumes = storageManager.storageVolumes
        val paths = mutableListOf<String>()
        var rootDirs: Array<File>? = null
        for (storageVolume in storageVolumes) {
            if (storageVolume.isRemovable) {
                val path =
                    if (isRPlus()) {
                        storageVolume.directory.toString()
                    } else {
                        val uuid = storageVolume.uuid ?: continue
                        if (rootDirs == null) {
                            rootDirs = File("/storage").listFiles()
                        }
                        rootDirs?.find { it.name.contains(uuid) }?.absolutePath ?: ""
                    }
                if (path.isNotEmpty() && !path.contains("storage")) {
                    paths.add(path)
                }
            }
        }
        return paths
    }
    private fun getStorageDirectories(context: Context): Array<String> {
        val paths = HashSet<String>()
        val rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE")
        val rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
        if (rawEmulatedStorageTarget.isNullOrEmpty()) {
            context.getExternalFilesDirs(null).filterNotNull().map { it.absolutePath }
                .mapTo(paths) {
                    val index = it.indexOf("/Android/data")
                    if (index < 0) {
                        it
                    } else {
                        it.substring(0, index)
                    }
                }
        } else {
            val path = Environment.getExternalStorageDirectory().absolutePath
            val folders = Pattern.compile("/").split(path)
            val lastFolder = folders[folders.size - 1]
            var isDigit = false
            try {
                Integer.valueOf(lastFolder)
                isDigit = true
            } catch (ignored: NumberFormatException) {
            }
            val rawUserId = if (isDigit) lastFolder else ""
            if (TextUtils.isEmpty(rawUserId)) {
                paths.add(rawEmulatedStorageTarget)
            } else {
                paths.add(rawEmulatedStorageTarget + File.separator + rawUserId)
            }
        }
        if (!rawSecondaryStoragesStr.isNullOrEmpty()) {
            val rawSecondaryStorages =
                rawSecondaryStoragesStr.split(
                    File.pathSeparator.toRegex(),
                ).dropLastWhile(String::isEmpty).toTypedArray()
            Collections.addAll(paths, *rawSecondaryStorages)
        }
        return paths.map { it.trimEnd('/') }.toTypedArray()
    }
    private fun convertFile(
        file: File,
        showHidden: Boolean,
    ): DFile {
        var size: Long = 0
        val isDir = file.isDirectory
        if (!isDir) {
            size = file.length()
        }
        return DFile(
            file.name,
            file.path,
            "",
            null,
            Instant.fromEpochMilliseconds(file.lastModified()),
            size,
            isDir,
            if (isDir) file.getDirectChildrenCount(showHidden) else 0,
        )
    }
    fun getFilesList(
        dir: String,
        showHidden: Boolean,
        sortBy: FileSortBy,
    ): List<DFile> {
        val pathFile = File(dir)
        val files = ArrayList<DFile>()
        if (pathFile.exists() && pathFile.isDirectory) {
            val listFiles = pathFile.listFiles()
            listFiles?.forEach { file ->
                if (!showHidden && file.isHidden) {
                    return@forEach
                }
                files.add(convertFile(file, showHidden))
            }
        }
        return files.sorted(sortBy)
    }
    suspend fun search(
        query: String,
        root: String,
        sortBy: FileSortBy,
    ): List<DFile> {
        val filterFields = QueryHelper.parseAsync(query)
        val showHidden = filterFields.find { it.name == "show_hidden" }?.value?.toBoolean() ?: false
        val text = filterFields.find { it.name == "text" }?.value ?: ""
        val parent = filterFields.find { it.name == "parent" }?.value ?: ""
        val fileSizeFields = filterFields.filter { it.name == "file_size" }
        val dir = parent.ifEmpty { root }
        val items = if (text.isNotEmpty() || fileSizeFields.isNotEmpty()) {
            search(text, dir, showHidden).sorted(sortBy)
        } else {
            getFilesList(dir, showHidden, sortBy)
        }
        if (fileSizeFields.isEmpty()) return items
        return items.filter { !it.isDir && matchFileSizeFilters(it.size, fileSizeFields) }
    }
    private fun matchFileSizeFilters(size: Long, fileSizeFields: List<FilterField>): Boolean {
        for (f in fileSizeFields) {
            val (op, rawValue) = f.normalizeComparison(defaultOp = "=")
            val bytes = rawValue.toLongOrNull() ?: return false
            val ok = when (op) {
                ">" -> size > bytes
                ">=" -> size >= bytes
                "<" -> size < bytes
                "<=" -> size <= bytes
                "!=" -> size != bytes
                "=", "" -> size == bytes
                else -> true
            }
            if (!ok) return false
        }
        return true
    }
    fun search(
        q: String,
        dir: String,
        showHidden: Boolean,
    ): ArrayList<DFile> {
        val files = ArrayList<DFile>()
        File(dir).listFiles()?.sortedBy { it.isDirectory }?.forEach {
            if (!showHidden && it.isHidden) {
                return@forEach
            }
            if (it.name.contains(q, true)) {
                files.add(convertFile(it, showHidden))
            }
            if (it.isDirectory) {
                files.addAll(search(q, it.absolutePath, showHidden))
            }
        }
        return files
    }
    fun createDirectory(path: String): DFile {
        val file = File(path)
        file.mkdirs()
        return convertFile(file, false)
    }
    fun createFile(path: String): DFile {
        val file = File(path)
        file.createNewFile()
        return convertFile(file, false)
    }
    fun getAllVolumeNames(context: Context): List<String> {
        val volumeNames = mutableListOf(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        context.getExternalFilesDirs(null)
            .mapNotNull { storageManager.getStorageVolume(it) }
            .filterNot { it.isPrimary }
            .mapNotNull { it.uuid?.lowercase(Locale.US) }
            .forEach {
                volumeNames.add(it)
            }
        return volumeNames
    }
    fun getRecentFiles(): List<File> {
        val externalStorageDir = Environment.getExternalStorageDirectory()
        val recentFilesQueue = PriorityQueue<File>(100) { f1, f2 ->
            f1.lastModified().compareTo(f2.lastModified())
        }
        gatherRecentFiles(externalStorageDir, recentFilesQueue)
        return recentFilesQueue.sortedByDescending { it.lastModified() }
    }
    private fun gatherRecentFiles(directory: File, recentFilesQueue: PriorityQueue<File>) {
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    gatherRecentFiles(file, recentFilesQueue)
                } else {
                    if (recentFilesQueue.size < 100) {
                        recentFilesQueue.add(file)
                    } else {
                        if (file.lastModified() > (recentFilesQueue.peek()?.lastModified() ?: Long.MIN_VALUE)) {
                            recentFilesQueue.poll()
                            recentFilesQueue.add(file)
                        }
                    }
                }
            }
        }
    }
}
