class ImageSliderAdapter(
    private val images: List<String>
) : RecyclerView.Adapter<ImageSliderAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image_slider, parent, false)
        )
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(images[position])
            .into(holder.imageView)
    }
    override fun getItemCount() = images.size
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivSliderImage)
    }
}