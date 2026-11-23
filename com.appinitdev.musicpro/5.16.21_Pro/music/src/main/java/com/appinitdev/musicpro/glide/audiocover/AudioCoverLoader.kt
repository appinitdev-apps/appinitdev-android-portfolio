
 





class AudioCoverLoader(private val context: Context) : ModelLoader<Audio, Bitmap> {
    override fun buildLoadData(audio: Audio, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(audio), AudioCoverFetcher(context, audio))
    }

    fun getResourceFetcher(model: Audio): DataFetcher<Bitmap> {
        return AudioCoverFetcher(context, model)
    }

    override fun handles(model: Audio): Boolean {
        return true
    }

    internal class Factory(private val context: Context) : ModelLoaderFactory<Audio, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Audio, Bitmap> {
            return AudioCoverLoader(context)
        }

        override fun teardown() {}
    }
}