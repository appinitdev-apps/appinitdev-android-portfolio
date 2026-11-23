@GlideModule
class AudioCoverModule : AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
    @SuppressLint("CheckResult")
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultTransitionOptions(Bitmap::class.java, BitmapTransitionOptions.withCrossFade())
        val requestOptions = RequestOptions()
        requestOptions.format(DecodeFormat.PREFER_ARGB_8888)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
        requestOptions.transform(
                Padding(BlurShadow.MAX_BLUR_RADIUS.toInt()),
                BlurShadow(context)
                    .setElevation(25F)
                    .setBlurRadius(BlurShadow.MAX_BLUR_RADIUS))
        builder.setDefaultRequestOptions(requestOptions)
    }
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(DescriptorCoverModel::class.java, InputStream::class.java, DescriptorCoverLoader.Factory())
        registry.append(Album::class.java, Bitmap::class.java, AlbumCoverLoader.Factory(context))
        registry.append(Audio::class.java, Bitmap::class.java, AudioCoverLoader.Factory(context))
        registry.append(Artist::class.java, Bitmap::class.java, ArtistCoverLoader.Factory(context))
        registry.append(Genre::class.java, Bitmap::class.java, GenreCoverLoader.Factory(context))
        registry.append(Folder::class.java, Bitmap::class.java, FolderCoverLoader.Factory(context))
        registry.append(YearGroup::class.java, Bitmap::class.java, YearCoverLoader.Factory(context))
    }
}
