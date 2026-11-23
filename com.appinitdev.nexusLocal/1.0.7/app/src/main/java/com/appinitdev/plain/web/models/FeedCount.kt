
data class FeedCount(val id: String, val count: Int)

fun DFeedCount.toModel(): FeedCount {
    return FeedCount(id, count)
}