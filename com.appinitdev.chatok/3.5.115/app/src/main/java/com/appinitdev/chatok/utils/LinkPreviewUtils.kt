data class LinkMetadata(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null
)
object LinkPreviewUtils {
    suspend fun extractMetadata(url: String): LinkMetadata? = withContext(Dispatchers.IO) {
        try {
            val document = Jsoup.connect(url)
                .timeout(5000)
                .get()
            val title = document.select("meta[property=og:title]").attr("content")
                .ifEmpty { document.title() }
            val description = document.select("meta[property=og:description]").attr("content")
                .ifEmpty { document.select("meta[name=description]").attr("content") }
            val image = document.select("meta[property=og:image]").attr("content")
            if (title.isEmpty() && description.isEmpty()) return@withContext null
            LinkMetadata(url, title, description, image)
        } catch (e: Exception) {
            null
        }
    }
    fun containsUrl(text: String): String? {
        val urlRegex = """(https?://[\w.-]+(?:\.[\w.-]+)+[\w_~:/?#\[\]@!$&'()*+,;=.-]+)""".toRegex()
        return urlRegex.find(text)?.value
    }
}
