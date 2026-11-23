
class CoilImagesPlugin(val context: Context) : AbstractMarkwonPlugin() {
    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(Image::class.java, ImageSpanFactory())
    }

    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
        builder.asyncDrawableLoader(object : AsyncDrawableLoader() {
            private val cache: MutableMap<AsyncDrawable, Disposable> = HashMap(2)

            override fun load(drawable: AsyncDrawable) {
                val loaded = AtomicBoolean(false)
                val target = AsyncDrawableTarget(drawable, loaded)
                val cachedPath = context.imageLoader
                    .diskCache?.openSnapshot(drawable.destination)?.data
                val request = ImageRequest.Builder(context)
                    .data(cachedPath ?: drawable.destination)
                    .build().newBuilder()
                    .target(target)
                    .build()
                
                
                
                val disposable = context.imageLoader.enqueue(request)
                
                if (!loaded.get()) {
                    
                    loaded.set(true)
                    cache[drawable] = disposable
                }
            }

            override fun cancel(drawable: AsyncDrawable) {
                cache.remove(drawable)?.dispose()
            }

            override fun placeholder(drawable: AsyncDrawable): Drawable? {
                return null
            }

            private inner class AsyncDrawableTarget(val drawable: AsyncDrawable, val loaded: AtomicBoolean) : Target {
                fun onSuccess(loadedDrawable: Drawable) {
                    
                    if (cache.remove(drawable) != null
                        || !loaded.get()
                    ) {
                        
                        loaded.set(true)
                        if (drawable.isAttached) {
                            DrawableUtils.applyIntrinsicBoundsIfEmpty(loadedDrawable)
                            drawable.setResult(loadedDrawable)
                        }
                    }
                }

                fun onStart(placeholder: Drawable?) {
                    if (placeholder != null && drawable.isAttached) {
                        DrawableUtils.applyIntrinsicBoundsIfEmpty(placeholder)
                        drawable.setResult(placeholder)
                    }
                }

                fun onError(errorDrawable: Drawable?) {
                    if (cache.remove(drawable) != null) {
                        if (errorDrawable != null && drawable.isAttached) {
                            DrawableUtils.applyIntrinsicBoundsIfEmpty(errorDrawable)
                            drawable.setResult(errorDrawable)
                        }
                    }
                }
            }
        })
    }

    override fun beforeSetText(textView: TextView, markdown: Spanned) {
        AsyncDrawableScheduler.unschedule(textView)
    }

    override fun afterSetText(textView: TextView) {
        AsyncDrawableScheduler.schedule(textView)
    }
}