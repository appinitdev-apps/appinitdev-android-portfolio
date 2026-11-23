fun SchemaBuilder.addFileQuerySchema() {
    query("mounts") {
        resolver { ->
            MountsLoader.load(MainApp.instance)
        }
    }
    query("recentFiles") {
        resolver { ->
            val context = MainApp.instance
            Permission.WRITE_EXTERNAL_STORAGE.checkAsync(context)
            if (isQPlus()) {
                FileMediaStoreHelper.getRecentFilesAsync(context).map { it.toModel() }
            } else {
                FileSystemHelper.getRecentFiles().map { it.toModel() }
            }
        }
    }
    query("files") {
        resolver { root: String, offset: Int, limit: Int, query: String, sortBy: FileSortBy ->
            val context = MainApp.instance
            Permission.WRITE_EXTERNAL_STORAGE.checkAsync(context)
            FileSystemHelper.search(query, root, sortBy).drop(offset).take(limit).map { it.toModel() }
        }
    }
    query("fileInfo") {
        resolver { id: ID, path: String, fileName: String ->
            val context = MainApp.instance
            Permission.WRITE_EXTERNAL_STORAGE.checkAsync(context)
            val finalPath = path.getFinalPath(context)
            val file = File(finalPath)
            val updatedAt = Instant.fromEpochMilliseconds(file.lastModified())
            var tags = emptyList<Tag>()
            var data: MediaFileInfo? = null
            if (fileName.isImageFast()) {
                if (id.value.isNotEmpty()) {
                    tags = TagsLoader.load(id.value, DataType.IMAGE)
                }
                data = FileInfoLoader.loadImage(finalPath)
            } else if (fileName.isVideoFast()) {
                if (id.value.isNotEmpty()) {
                    tags = TagsLoader.load(id.value, DataType.VIDEO)
                }
                data = FileInfoLoader.loadVideo(context, finalPath)
            } else if (fileName.isAudioFast()) {
                if (id.value.isNotEmpty()) {
                    tags = TagsLoader.load(id.value, DataType.AUDIO)
                }
                data = FileInfoLoader.loadAudio(context, finalPath)
            }
            FileInfo(path, updatedAt, size = file.length(), tags, data)
        }
    }
    query("fileIds") {
        resolver { paths: List<String> ->
            paths.map { FileHelper.getFileId(it) }
        }
    }
}
