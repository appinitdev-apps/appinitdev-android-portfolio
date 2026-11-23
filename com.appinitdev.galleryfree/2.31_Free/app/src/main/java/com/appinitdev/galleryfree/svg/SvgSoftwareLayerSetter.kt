
class SvgSoftwareLayerSetter : RequestListener<PictureDrawable> {

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<PictureDrawable>, isFirstResource: Boolean): Boolean {
        val view = (target as ImageViewTarget<*>).view
        view.setLayerType(ImageView.LAYER_TYPE_NONE, null)
        return false
    }

    override fun onResourceReady(
        resource: PictureDrawable,
        model: Any,
        target: Target<PictureDrawable>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        val view = (target as ImageViewTarget<*>).view
        view.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
        return false
    }
}
