
abstract class BaseContentHelper {
    protected abstract val uriExternal: Uri
    protected abstract suspend fun buildWhereAsync(query: String): ContentWhere
    protected abstract fun getProjection(): Array<String>

    suspend fun countAsync(context: Context, query: String): Int {
        return context.contentResolver.count(uriExternal, buildWhereAsync(query))
    }

    suspend fun getIdsAsync(context: Context, query: String): Set<String> {
        val where = buildWhereAsync(query)
        return context.contentResolver.getSearchCursor(uriExternal, getProjection(), where)?.map { cursor, cache ->
            cursor.getStringValue(BaseColumns._ID, cache)
        }?.toSet() ?: emptySet()
    }

    fun deleteByIdsAsync(context: Context, ids: Set<String>) {
        ids.chunked(500).forEach { chunk ->
            val selection = "${BaseColumns._ID} IN (${StringHelper.getQuestionMarks(chunk.size)})"
            val selectionArgs = chunk.map { it }.toTypedArray()
            context.contentResolver.delete(uriExternal, selection, selectionArgs)
        }
    }
}
