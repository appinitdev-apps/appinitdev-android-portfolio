
object ZipBrowserHelper {
    const val ZIP_SEPARATOR = "!zip!/"

    fun isZipPath(path: String): Boolean = path.contains(ZIP_SEPARATOR)

    fun getZipFilePath(path: String): String = path.substringBefore(ZIP_SEPARATOR)

    fun getInternalPath(path: String): String = path.substringAfter(ZIP_SEPARATOR, "")

    fun joinPath(zipFilePath: String, internalPath: String): String =
        "$zipFilePath$ZIP_SEPARATOR$internalPath"

     
    fun getDisplayName(zipVirtualPath: String): String {
        val internalPath = getInternalPath(zipVirtualPath)
        val trimmed = internalPath.trimEnd('/')
        return if (trimmed.isEmpty()) {
            getZipFilePath(zipVirtualPath).substringAfterLast("/")
        } else {
            trimmed.substringAfterLast("/")
        }
    }

     




    fun listEntries(zipVirtualPath: String, sortBy: FileSortBy): List<DFile> {
        val zipFilePath = getZipFilePath(zipVirtualPath)
        val internalDir = getInternalPath(zipVirtualPath)
        
        val prefix = when {
            internalDir.isEmpty() -> ""
            internalDir.endsWith("/") -> internalDir
            else -> "$internalDir/"
        }
        
        val entries = linkedMapOf<String, DFile>()
        try {
            ZipInputStream(File(zipFilePath).inputStream().buffered()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    if (entryName.startsWith(prefix)) {
                        val relative = entryName.removePrefix(prefix)
                        if (relative.isNotEmpty()) {
                            val slashIndex = relative.indexOf('/')
                            when {
                                slashIndex == -1 -> {
                                    
                                    if (!entries.containsKey(relative)) {
                                        entries[relative] = DFile(
                                            name = relative,
                                            path = joinPath(zipFilePath, "$prefix$relative"),
                                            permission = "",
                                            createdAt = null,
                                            updatedAt = if (entry.time > 0) Instant.fromEpochMilliseconds(entry.time) else Instant.fromEpochMilliseconds(0),
                                            size = entry.size.coerceAtLeast(0),
                                            isDir = false,
                                            children = 0,
                                        )
                                    }
                                }
                                slashIndex == relative.length - 1 -> {
                                    
                                    val dirName = relative.dropLast(1)
                                    if (dirName.isNotEmpty() && !entries.containsKey(dirName)) {
                                        entries[dirName] = DFile(
                                            name = dirName,
                                            path = joinPath(zipFilePath, "$prefix$dirName/"),
                                            permission = "",
                                            createdAt = null,
                                            updatedAt = if (entry.time > 0) Instant.fromEpochMilliseconds(entry.time) else Instant.fromEpochMilliseconds(0),
                                            size = 0,
                                            isDir = true,
                                            children = 0,
                                        )
                                    }
                                }
                                else -> {
                                    
                                    val dirName = relative.substring(0, slashIndex)
                                    if (!entries.containsKey(dirName)) {
                                        entries[dirName] = DFile(
                                            name = dirName,
                                            path = joinPath(zipFilePath, "$prefix$dirName/"),
                                            permission = "",
                                            createdAt = null,
                                            updatedAt = Instant.fromEpochMilliseconds(0),
                                            size = 0,
                                            isDir = true,
                                            children = 0,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            LogCat.e(e.toString())
        }
        return entries.values.toList().sorted(sortBy)
    }

     





    fun extractEntryToCache(context: android.content.Context, zipVirtualPath: String): File? {
        val zipFilePath = getZipFilePath(zipVirtualPath)
        val internalPath = getInternalPath(zipVirtualPath).trimEnd('/')
        if (internalPath.isEmpty()) return null
        val rawName = internalPath.substringAfterLast('/')
        val safeName = rawName.replace("[/\\\\:*?\"<>|]".toRegex(), "_").take(80)
        val cacheKey = ""
        val tempFile = File(context.cacheDir, "zip_preview_$cacheKey")
        if (tempFile.exists()) return tempFile
        try {
            ZipInputStream(File(zipFilePath).inputStream().buffered()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name.trimEnd('/') == internalPath) {
                        tempFile.outputStream().use { out -> zis.copyTo(out) }
                        return tempFile
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (e: Exception) {
            LogCat.e(e.toString())
        }
        return null
    }
}
