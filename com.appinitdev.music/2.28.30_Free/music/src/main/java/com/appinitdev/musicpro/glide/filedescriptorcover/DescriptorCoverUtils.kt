
object DescriptorCoverUtils {

    /**
     * Load original artwork directly from file using file descriptor.
     */
    fun ImageView.loadFromDescriptor(
            uri: Uri?,
            roundedCorners: Boolean = true,
            blur: Boolean = true,
            crop: Boolean = false,
            skipCache: Boolean = false
    ) {
        val transformations = mutableListOf<Transformation<android.graphics.Bitmap>>()

        if (crop) {
            transformations.add(CenterCrop())
        }

        if (roundedCorners) {
            transformations.add(RoundedCorners(AppearancePreferences.getCornerRadius().toInt()))
        }

        if (blur) {
            transformations.add(Padding(BlurShadow.DEFAULT_SHADOW_SIZE.toInt()))

            transformations.add(
                    BlurShadow(this.context)
                        .setElevation(25F)
                        .setBlurRadius(BlurShadow.DEFAULT_SHADOW_SIZE)
            )
        }

        var glideRequest = Glide.with(this)
            .asBitmap()
            .dontTransform() // This way we can apply our own transformations and skip the module specific ones
            .transform(*transformations.toTypedArray())
            .load(DescriptorCoverModel(this.context, uri ?: Uri.EMPTY))
            .error(R.drawable.ic_felicity)

        if (skipCache) {
            glideRequest = glideRequest
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        }

        glideRequest.into(this)
    }
}