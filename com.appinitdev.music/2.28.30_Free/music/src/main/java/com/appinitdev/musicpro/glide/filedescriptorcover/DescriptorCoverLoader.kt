class DescriptorCoverLoader : ModelLoader<DescriptorCoverModel, InputStream> {
    override fun buildLoadData(descriptorCoverModel: DescriptorCoverModel, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ObjectKey(descriptorCoverModel), DescriptorCoverFetcher(descriptorCoverModel))
    }
    override fun handles(model: DescriptorCoverModel): Boolean {
        return true
    }
    internal class Factory : ModelLoaderFactory<DescriptorCoverModel, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<DescriptorCoverModel, InputStream> {
            return DescriptorCoverLoader()
        }
        override fun teardown() {}
    }
}