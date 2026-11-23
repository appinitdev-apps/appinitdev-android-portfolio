
data class ContactGroup(
    var id: ID,
    var name: String,
    var contactCount: Int = 0,
)

fun DGroup.toModel(): ContactGroup {
    return ContactGroup(ID(id.toString()), name)
}
