class GenreCoverLoader(private val context: Context) : ModelLoader<Genre, Bitmap> {
    override fun buildLoadData(model: Genre, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap?>? {
        return ModelLoader.LoadData(ObjectKey(model), GenreCoverFetcher(context, model))
    }
    override fun handles(model: Genre): Boolean {
        return true
    }
    internal class Factory(private val context: Context) : ModelLoaderFactory<Genre, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Genre, Bitmap> {
            return GenreCoverLoader(context)
        }
        override fun teardown() {}
    }
}