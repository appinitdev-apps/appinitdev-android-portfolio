class ArtistCoverLoader(private val context: Context) : ModelLoader<Artist, Bitmap> {
    override fun buildLoadData(model: Artist, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        val lastModified = artistImageFile(model.name)?.lastModified() ?: 0L
        val key = ObjectKey("${model.id}_$lastModified")
        return ModelLoader.LoadData(key, ArtistCoverFetcher(context, model))
    }
    private fun artistImageFile(artistName: String?): File? {
        if (artistName.isNullOrBlank()) return null
        val sanitized = artistName.trim()
            .replace(' ', '_')
            .filter { it.isLetterOrDigit() || it == '_' || it == '-' }
            .ifEmpty { "unknown" }
        return File(File(context.filesDir, "artist_images"), "$sanitized.png")
    }
    fun getResourceFetcher(model: Artist): DataFetcher<Bitmap> {
        return ArtistCoverFetcher(context, model)
    }
    override fun handles(model: Artist): Boolean {
        return true
    }
    internal class Factory(private val context: Context) : ModelLoaderFactory<Artist, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Artist, Bitmap> {
            return ArtistCoverLoader(context)
        }
        override fun teardown() {}
    }
}
