class YearCoverLoader(private val context: Context) : ModelLoader<YearGroup, Bitmap> {
    override fun buildLoadData(model: YearGroup, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap?>? {
        return ModelLoader.LoadData(ObjectKey(model), YearCoverFetcher(context, model))
    }
    override fun handles(model: YearGroup): Boolean = true
    internal class Factory(private val context: Context) : ModelLoaderFactory<YearGroup, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<YearGroup, Bitmap> {
            return YearCoverLoader(context)
        }
        override fun teardown() {}
    }
}
