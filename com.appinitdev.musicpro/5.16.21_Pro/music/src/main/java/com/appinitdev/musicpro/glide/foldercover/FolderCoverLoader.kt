
 





class FolderCoverLoader(private val context: Context) : ModelLoader<Folder, Bitmap> {
    override fun buildLoadData(model: Folder, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap?>? {
        return ModelLoader.LoadData(ObjectKey(model), FolderCoverFetcher(context, model))
    }

    override fun handles(model: Folder): Boolean = true

    internal class Factory(private val context: Context) : ModelLoaderFactory<Folder, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Folder, Bitmap> {
            return FolderCoverLoader(context)
        }

        override fun teardown() {}
    }
}
