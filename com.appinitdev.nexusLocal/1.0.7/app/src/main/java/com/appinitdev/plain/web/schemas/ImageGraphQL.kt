fun SchemaBuilder.addImageSchema() {
    query("images") {
        configure {
            executor = Executor.DataLoaderPrepared
        }
        resolver { offset: Int, limit: Int, query: String, sortBy: FileSortBy ->
            val context = MainApp.instance
            Permission.WRITE_EXTERNAL_STORAGE.checkAsync(context)
            val fields = SearchHelper.parse(query)
            val textField = fields.find { it.name == "text" }
            val queryText = textField?.value ?: ""
            ImageSearchHelper.searchCombinedAsync(
                context = context,
                queryText = queryText,
                extraQuery = query,
                limit = limit,
                offset = offset,
                sortBy = sortBy
            ).map { it.toModel() }
        }
        type<Image> {
            dataProperty("tags") {
                prepare { item -> item.id.value }
                loader { ids ->
                    TagsLoader.load(ids, DataType.IMAGE)
                }
            }
        }
    }
    query("imageCount") {
        resolver { query: String ->
            val context = MainApp.instance
            if (Permission.WRITE_EXTERNAL_STORAGE.enabledAndCanAsync(context)) {
                val fields = SearchHelper.parse(query)
                val textField = fields.find { it.name == "text" }
                val queryText = textField?.value ?: ""
                ImageSearchHelper.countCombinedAsync(
                    context = context,
                    queryText = queryText,
                    extraQuery = query
                )
            } else {
                0
            }
        }
    }
    query("imageSearchStatus") {
        resolver { -> buildImageSearchStatus() }
    }
    type<ImageSearchStatus> {}
    mutation("enableImageSearch") {
        resolver { ->
            sendEvent(HEnableImageSearchEvent())
            true
        }
    }
    mutation("disableImageSearch") {
        resolver { ->
            sendEvent(HDisableImageSearchEvent())
            true
        }
    }
    mutation("cancelImageModelDownload") {
        resolver { ->
            sendEvent(HCancelImageModelDownloadEvent())
            true
        }
    }
    mutation("startImageIndex") {
        resolver { force: Boolean? ->
            ImageIndexManager.fullScan(force == true)
            true
        }
    }
    mutation("cancelImageIndex") {
        resolver { ->
            ImageSearchIndexer.cancel()
            true
        }
    }
}
