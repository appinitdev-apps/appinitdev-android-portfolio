
data class ContactSource(var name: String, var type: String)

fun DContactSource.toModel(): ContactSource {
    return ContactSource(name, type)
}
