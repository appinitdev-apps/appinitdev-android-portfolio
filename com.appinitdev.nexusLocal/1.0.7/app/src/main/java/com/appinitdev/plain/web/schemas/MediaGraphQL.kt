
fun SchemaBuilder.addMediaSchema() {
    query("mediaBuckets") {
        resolver { type: DataType ->
            val context = MainApp.instance
            if (Permission.WRITE_EXTERNAL_STORAGE.enabledAndCanAsync(context)) {
                if (type == DataType.IMAGE) {
                    ImageMediaStoreHelper.getBucketsAsync(context).map { it.toModel() }
                } else if (type == DataType.AUDIO) {
                    if (isQPlus()) {
                        AudioMediaStoreHelper.getBucketsAsync(context).map { it.toModel() }
                    } else {
                        emptyList()
                    }
                } else if (type == DataType.VIDEO) {
                    VideoMediaStoreHelper.getBucketsAsync(context).map { it.toModel() }
                } else if (type == DataType.DOC) {
                    DocMediaStoreHelper.getDocBucketsAsync(context).map { it.toModel() }
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }
    mutation("deleteMediaItems") {
        resolver { type: DataType, query: String ->
            val ids: Set<String>
            val context = MainApp.instance
            val hasTrashFeature = AppFeatureType.MEDIA_TRASH.has()
            when (type) {
                DataType.AUDIO -> {
                    ids = if (hasTrashFeature) AudioMediaStoreHelper.getTrashedIdsAsync(context, query) else AudioMediaStoreHelper.getIdsAsync(context, query)
                    AudioMediaStoreHelper.deleteRecordsAndFilesByIdsAsync(context, ids, true)
                }

                DataType.VIDEO -> {
                    ids = if (hasTrashFeature) VideoMediaStoreHelper.getTrashedIdsAsync(context, query) else VideoMediaStoreHelper.getIdsAsync(context, query)
                    VideoMediaStoreHelper.deleteRecordsAndFilesByIdsAsync(context, ids, true)
                }

                DataType.IMAGE -> {
                    ids = if (hasTrashFeature) ImageMediaStoreHelper.getTrashedIdsAsync(context, query) else ImageMediaStoreHelper.getIdsAsync(context, query)
                    ImageMediaStoreHelper.deleteRecordsAndFilesByIdsAsync(context, ids, true)
                    ImageIndexManager.enqueueRemove(ids)
                }

                DataType.DOC -> {
                    ids = if (hasTrashFeature) DocMediaStoreHelper.getTrashedIdsAsync(context, query) else DocMediaStoreHelper.getIdsAsync(context, query)
                    DocMediaStoreHelper.deleteRecordsAndFilesByIdsAsync(context, ids, true)
                }

                else -> {
                }
            }
            ActionResult(type, query)
        }
    }
    mutation("trashMediaItems") {
        resolver { type: DataType, query: String ->
            if (!isRPlus()) {
                return@resolver ActionResult(type, query)
            }

            var ids = setOf<String>()
            val context = MainApp.instance
            when (type) {
                DataType.AUDIO -> {
                    ids = AudioMediaStoreHelper.getIdsAsync(context, query)
                    val paths = AudioMediaStoreHelper.getPathsByIdsAsync(context, ids)
                    AudioMediaStoreHelper.trashByIdsAsync(context, ids)
                    AudioPlaylistPreference.deleteAsync(paths)
                }

                DataType.VIDEO -> {
                    ids = VideoMediaStoreHelper.getIdsAsync(context, query)
                    val paths = VideoMediaStoreHelper.getPathsByIdsAsync(context, ids)
                    VideoMediaStoreHelper.trashByIdsAsync(context, ids)
                    VideoPlaylistPreference.deleteAsync(paths)
                }

                DataType.IMAGE -> {
                    ids = ImageMediaStoreHelper.getIdsAsync(context, query)
                    ImageMediaStoreHelper.trashByIdsAsync(context, ids)
                    ImageIndexManager.enqueueRemove(ids)
                }

                DataType.DOC -> {
                    ids = DocMediaStoreHelper.getIdsAsync(context, query)
                    DocMediaStoreHelper.trashByIdsAsync(context, ids)
                }

                else -> {
                }
            }
            TagHelper.deleteTagRelationByKeys(ids, type)
            ActionResult(type, query)
        }
    }
    mutation("restoreMediaItems") {
        resolver { type: DataType, query: String ->
            if (!isRPlus()) {
                return@resolver ActionResult(type, query)
            }

            val ids: Set<String>
            val context = MainApp.instance
            when (type) {
                DataType.AUDIO -> {
                    ids = AudioMediaStoreHelper.getTrashedIdsAsync(context, query)
                    AudioMediaStoreHelper.restoreByIdsAsync(context, ids)
                }

                DataType.VIDEO -> {
                    ids = VideoMediaStoreHelper.getTrashedIdsAsync(context, query)
                    VideoMediaStoreHelper.restoreByIdsAsync(context, ids)
                }

                DataType.IMAGE -> {
                    ids = ImageMediaStoreHelper.getTrashedIdsAsync(context, query)
                    ImageMediaStoreHelper.restoreByIdsAsync(context, ids)
                    ImageIndexManager.enqueueAdd(ids)
                }

                DataType.DOC -> {
                    ids = DocMediaStoreHelper.getTrashedIdsAsync(context, query)
                    DocMediaStoreHelper.restoreByIdsAsync(context, ids)
                }

                else -> {
                }
            }
            ActionResult(type, query)
        }
    }
}