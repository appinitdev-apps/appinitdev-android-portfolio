
 





class AlbumCoverLoader(private val context: Context) : ModelLoader<Album, Bitmap> {
    override fun buildLoadData(album: Album, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(album), AlbumCoverFetcher(context, album))
    }

    fun getResourceFetcher(model: Album): DataFetcher<Bitmap> {
        return AlbumCoverFetcher(context, model)
    }

    override fun handles(model: Album): Boolean {
        return true
    }

    internal class Factory(private val context: Context) : ModelLoaderFactory<Album, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Album, Bitmap> {
            return AlbumCoverLoader(context)
        }

        override fun teardown() {}
    }
}
