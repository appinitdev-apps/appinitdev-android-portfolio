
interface Migration {
    val key: String

    suspend fun migrate()
}
