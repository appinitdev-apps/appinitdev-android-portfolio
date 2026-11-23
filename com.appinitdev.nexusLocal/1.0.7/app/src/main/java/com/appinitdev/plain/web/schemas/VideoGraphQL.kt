fun SchemaBuilder.addVideoSchema() {
    query("videos") {
        configure {
            executor = Executor.DataLoaderPrepared
        }
        resolver { offset: Int, limit: Int, query: String, sortBy: FileSortBy ->
            val context = MainApp.instance
            Permission.WRITE_EXTERNAL_STORAGE.checkAsync(context)
            VideoMediaStoreHelper.searchAsync(context, query, limit, offset, sortBy).map { it.toModel() }
        }
        type<Video> {
            dataProperty("tags") {
                prepare { item -> item.id.value }
                loader { ids ->
                    TagsLoader.load(ids, DataType.VIDEO)
                }
            }
        }
    }
    query("videoCount") {
        resolver { query: String ->
            if (Permission.WRITE_EXTERNAL_STORAGE.enabledAndCanAsync(MainApp.instance)) {
                VideoMediaStoreHelper.countAsync(MainApp.instance, query)
            } else {
                0
            }
        }
    }
}
