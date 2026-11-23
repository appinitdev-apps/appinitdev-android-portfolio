
object ResourceUtils {
    @JvmStatic
    fun getString(resource: StringResource): String = runBlocking {
        org.jetbrains.compose.resources.getString(resource)
    }
}
