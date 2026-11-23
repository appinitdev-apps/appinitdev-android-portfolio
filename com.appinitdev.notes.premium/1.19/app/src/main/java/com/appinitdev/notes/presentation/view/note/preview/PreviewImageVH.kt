
class PreviewImageVH(
    private val binding: RecyclerPreviewImageBinding,
    onClick: ((position: Int) -> Unit),
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.root.setOnClickListener { onClick(absoluteAdapterPosition) }
    }

    fun bind(file: File?) {
        binding.Message.visibility = View.GONE

        Glide.with(binding.ImageView)
            .load(file)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .listener(
                object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        binding.Message.visibility = View.VISIBLE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        return false
                    }
                }
            )
            .into(binding.ImageView)
    }
}
