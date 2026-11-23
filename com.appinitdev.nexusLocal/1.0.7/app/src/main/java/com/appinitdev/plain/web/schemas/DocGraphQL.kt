
fun SchemaBuilder.addDocSchema() {
    query("docs") {
        configure {
            executor = Executor.DataLoaderPrepared
        }
        resolver { offset: Int, limit: Int, query: String, sortBy: FileSortBy ->
            val context = MainApp.instance
            Permission.WRITE_EXTERNAL_STORAGE.checkAsync(context)
            DocMediaStoreHelper.searchAsync(context, query, limit, offset, sortBy).map { it.toDocModel() }
        }
        type<Doc> {
            dataProperty("tags") {
                prepare { item -> item.id.value }
                loader { ids ->
                    TagsLoader.load(ids, DataType.DOC)
                }
            }
        }
    }

    query("docCount") {
        resolver { query: String ->
            if (Permission.WRITE_EXTERNAL_STORAGE.enabledAndCanAsync(MainApp.instance)) {
                DocMediaStoreHelper.countAsync(MainApp.instance, query)
            } else {
                0
            }
        }
    }

    query("docExtGroups") {
        resolver { ->
            if (Permission.WRITE_EXTERNAL_STORAGE.enabledAndCanAsync(MainApp.instance)) {
                DocMediaStoreHelper.getDocExtGroupsAsync(MainApp.instance).map { DocExtGroup(it.first, it.second) }
            } else {
                emptyList()
            }
        }
    }
}

