
object FeedsLoader {
    fun load(
        ids: List<String>,
    ): List<ExecutionResult<Feed?>> {
        val map = FeedHelper.getAll().associateBy { it.id }
        return ids.map { id ->
            ExecutionResult.Success(map[id]?.toModel())
        }
    }
}
