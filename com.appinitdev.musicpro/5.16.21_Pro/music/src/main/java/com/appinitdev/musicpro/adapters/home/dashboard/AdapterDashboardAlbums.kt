class AdapterDashboardAlbums(
        private var albums: List<Album>
) : RecyclerView.Adapter<AdapterDashboardAlbums.Holder>() {
    private var callbacks: AdapterDashboardAlbumsCallbacks? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = AdapterCarouselBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }
    override fun getItemCount(): Int = albums.size
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val album = albums[position]
        holder.binding.art.loadArtCoverWithPayload(album)
        holder.binding.title.text = album.name ?: holder.itemView.context.getString(
                com.appinitdev.musicpro.R.string.unknown)
        holder.binding.artist.text = album.artist ?: holder.itemView.context.getString(
                com.appinitdev.musicpro.R.string.unknown)
        holder.binding.container.setOnClickListener {
            callbacks?.onAlbumClicked(albums[holder.bindingAdapterPosition])
        }
        holder.binding.container.setOnLongClickListener {
            callbacks?.onAlbumLongClicked(
                    albums[holder.bindingAdapterPosition],
                    holder.binding.art as ImageView)
            true
        }
    }
    fun updateData(newAlbums: List<Album>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = albums.size
            override fun getNewListSize() = newAlbums.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                albums[oldItemPosition].id == newAlbums[newItemPosition].id
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                albums[oldItemPosition] == newAlbums[newItemPosition]
        })
        albums = newAlbums
        diff.dispatchUpdatesTo(this)
    }
    fun setCallbacks(callbacks: AdapterDashboardAlbumsCallbacks) {
        this.callbacks = callbacks
    }
    inner class Holder(val binding: AdapterCarouselBinding) :
            HorizontalListViewHolder(binding.root)
    companion object {
        interface AdapterDashboardAlbumsCallbacks {
            fun onAlbumClicked(album: Album)
            fun onAlbumLongClicked(album: Album, imageView: ImageView)
        }
    }
}
