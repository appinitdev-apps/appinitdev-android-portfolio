
fun SchemaBuilder.addCallSchema() {
    query("calls") {
        configure {
            executor = Executor.DataLoaderPrepared
        }
        resolver { offset: Int, limit: Int, query: String ->
            Permissions.checkAsync(MainApp.instance, setOf(Permission.READ_CALL_LOG))
            CallMediaStoreHelper.searchAsync(MainApp.instance, query, limit, offset).map { it.toModel() }
        }
        type<Call> {
            dataProperty("tags") {
                prepare { item -> item.id.value }
                loader { ids ->
                    TagsLoader.load(ids, DataType.CALL)
                }
            }
        }
    }
    query("callCount") {
        resolver { query: String ->
            val context = MainApp.instance
            if (Permission.READ_CALL_LOG.enabledAndCanAsync(context)) {
                CallMediaStoreHelper.countAsync(context, query)
            } else {
                0
            }
        }
    }
    query("sims") {
        resolver { ->
            SimHelper.getAll().map { it.toModel() }
        }
    }
    mutation("call") {
        resolver { number: String, showDialer: Boolean ->
            Permission.CALL_PHONE.checkAsync(MainApp.instance)
            CallMediaStoreHelper.call(MainActivity.instance.get()!!, number, showDialer)
            true
        }
    }
    mutation("deleteCalls") {
        resolver { query: String ->
            val context = MainApp.instance
            Permission.WRITE_CALL_LOG.checkAsync(context)
            val newIds = CallMediaStoreHelper.getIdsAsync(context, query)
            TagHelper.deleteTagRelationByKeys(newIds, DataType.CALL)
            CallMediaStoreHelper.deleteByIdsAsync(context, newIds)
            true
        }
    }
}
