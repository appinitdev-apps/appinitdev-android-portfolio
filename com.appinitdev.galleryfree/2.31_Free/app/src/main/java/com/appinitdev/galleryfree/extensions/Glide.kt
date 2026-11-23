
/**
 * Cross fade transition option that disabled fading when loading from cache.
 */
fun getOptionalCrossFadeTransition(duration: Int): DrawableTransitionOptions {
    return DrawableTransitionOptions.with(
        TransitionFactory { dataSource, isFirstResource ->
            if (dataSource == DataSource.RESOURCE_DISK_CACHE) return@TransitionFactory null
            DrawableCrossFadeFactory.Builder(duration).build().build(dataSource, isFirstResource)
        }
    )
}

