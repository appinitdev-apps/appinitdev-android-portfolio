
@Serializable
data class Tag(
    var id: ID,
    var name: String,
    var count: Int,
)

fun DTag.toModel(): Tag {
    return Tag(ID(id), name, count)
}
