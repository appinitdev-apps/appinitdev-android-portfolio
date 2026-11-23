
 











object AppFileStore {
     
    fun toFidUri(fileId: String, ext: String = ""): String =
        if (ext.isNotEmpty()) "fid:$fileId.$ext" else "fid:$fileId"

     
    fun extFromMime(mimeType: String): String {
        if (mimeType.isEmpty()) return ""
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.lowercase() ?: ""
    }

     




    fun realPathFromId(context: Context, fidSuffix: String): String {
        val hash = fidSuffix.substringBefore(".")
        val base = context.appDir()
        return "$base/${hash.substring(0, 2)}/${hash.substring(2, 4)}/$fidSuffix"
    }

     






    fun resolveUri(context: Context, uri: String): String {
        if (uri.startsWith("fid:", ignoreCase = true)) {
            return realPathFromId(context, uri.removePrefix("fid:"))
        }
        return uri
    }

    

     

















    fun importFile(
        context: Context,
        srcFile: File,
        mimeType: String = "",
        deleteSrc: Boolean = false,
    ): DAppFile {
        val dao = AppDatabase.instance.appFileDao()
        val size = srcFile.length()
        val strongHash by lazy { FileHashHelper.strongHash(srcFile) }

        
        val weakHash = FileHashHelper.weakHash(srcFile)
        val candidates = dao.findByWeakKey(size, weakHash)

        if (candidates.isNotEmpty()) {
            
            tryReuseExisting(context, dao, srcFile, strongHash, deleteSrc)?.let { return it }
            
            return insertNew(context, dao, srcFile, size, weakHash, strongHash, mimeType, deleteSrc)
        }

        
        tryReuseExisting(context, dao, srcFile, strongHash, deleteSrc)?.let { return it }

        return insertNew(context, dao, srcFile, size, weakHash, strongHash, mimeType, deleteSrc)
    }

     


    fun importBytes(
        context: Context,
        data: ByteArray,
        mimeType: String = "",
    ): DAppFile {
        val dao = AppDatabase.instance.appFileDao()
        val size = data.size.toLong()
        val strongHash = FileHashHelper.strongHash(data)

        val existing = dao.getById(strongHash)
        if (existing != null) {
            dao.incrementRefCount(strongHash)
            return existing
        }

        
        val weakHash = FileHashHelper.weakHash(data)

        val effectiveMime = mimeType.ifEmpty { "application/octet-stream" }
        val ext = extFromMime(effectiveMime)
        val destFile = destFile(context, strongHash, ext)
        destFile.parentFile?.mkdirs()
        destFile.writeBytes(data)

        val record = DAppFile(strongHash).apply {
            this.size = size
            this.mimeType = effectiveMime
            this.realPath = destFile.absolutePath
            this.refCount = 1
            this.weakHash = weakHash
        }
        dao.insert(record)
        return record
    }

    

     




    fun release(context: Context, fidSuffix: String) {
        val hash = fidSuffix.substringBefore(".")
        val dao = AppDatabase.instance.appFileDao()
        dao.decrementRefCount(hash)
        val updated = dao.getById(hash) ?: return
        if (updated.refCount <= 0) {
            dao.delete(hash)
            File(updated.realPath).delete()
            LogCat.d("ChatFileStore: deleted orphan file $hash")
        }
    }

    

    private fun destFile(context: Context, hash: String, ext: String = ""): File {
        val base = context.appDir()
        val name = if (ext.isNotEmpty()) "$hash.$ext" else hash
        return File("$base/${hash.substring(0, 2)}/${hash.substring(2, 4)}/$name")
    }

    private fun tryReuseExisting(
        context: Context,
        dao: AppFileDao,
        srcFile: File,
        strongHash: String,
        deleteSrc: Boolean,
    ): DAppFile? {
        val existing = dao.getById(strongHash) ?: return null
        val ext = extFromMime(existing.mimeType)
        val targetFile = destFile(context, strongHash, ext)

        
        if (!targetFile.exists()) {
            
            val legacyFile = destFile(context, strongHash)
            if (legacyFile.exists()) {
                legacyFile.renameTo(targetFile)
                LogCat.d("ChatFileStore: renamed legacy file $strongHash to include extension")
            } else {
                storeSourceFile(srcFile, targetFile, deleteSrc)
                LogCat.d("ChatFileStore: restored missing file $strongHash")
            }
        } else if (deleteSrc) {
            srcFile.delete()
        }

        if (existing.realPath != targetFile.absolutePath) {
            existing.realPath = targetFile.absolutePath
            dao.update(existing)
        }

        dao.incrementRefCount(strongHash)
        existing.refCount += 1
        LogCat.d("ChatFileStore: reusing file $strongHash (refCount ${existing.refCount})")
        return existing
    }

    private fun storeSourceFile(
        srcFile: File,
        destFile: File,
        deleteSrc: Boolean,
    ) {
        destFile.parentFile?.mkdirs()
        if (deleteSrc) {
            
            
            val renamed = srcFile.renameTo(destFile)
            if (!renamed) {
                srcFile.copyTo(destFile, overwrite = true)
                srcFile.delete()
            }
        } else {
            srcFile.copyTo(destFile, overwrite = true)
        }
    }

    private fun insertNew(
        context: Context,
        dao: AppFileDao,
        srcFile: File,
        size: Long,
        weakHash: String,
        strongHash: String,
        mimeType: String,
        deleteSrc: Boolean,
    ): DAppFile {
        val effectiveMime = mimeType.ifEmpty {
            val srcExt = srcFile.name.getFilenameExtension()
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(srcExt) ?: "application/octet-stream"
        }
        val ext = extFromMime(effectiveMime)
        val destFile = destFile(context, strongHash, ext)
        storeSourceFile(srcFile, destFile, deleteSrc)

        val record = DAppFile(strongHash).apply {
            this.size = size
            this.mimeType = effectiveMime
            this.realPath = destFile.absolutePath
            this.refCount = 1
            this.weakHash = weakHash
        }
        dao.insert(record)
        LogCat.d("ChatFileStore: stored new file $strongHash (${size} bytes)")
        return record
    }
}
