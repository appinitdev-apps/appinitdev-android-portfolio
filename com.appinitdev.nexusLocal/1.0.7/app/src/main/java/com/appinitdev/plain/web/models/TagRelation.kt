
@Serializable
data class TagRelation(
    var tagId: String = "",
    var key: String = "",
)

fun DTagRelation.toModel(): TagRelation {
    return TagRelation(tagId, key)
}
