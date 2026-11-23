
 








class AdapterDashboardArtists(
        private var artists: List<Artist>
) : RecyclerView.Adapter<AdapterDashboardArtists.Holder>() {

    private var callbacks: AdapterDashboardArtistsCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = AdapterCarouselBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int = artists.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val artist = artists[position]

        
        holder.binding.art.loadArtCoverWithPayload(artist)
        holder.binding.title.text = artist.name ?: holder.itemView.context.getString(
                com.appinitdev.musicpro.R.string.unknown)

        
        holder.binding.artist.text = holder.itemView.context.getString(
                com.appinitdev.musicpro.R.string.x_albums,
                artist.albumCount
        )

        holder.binding.container.setOnClickListener {
            callbacks?.onArtistClicked(artists[holder.bindingAdapterPosition])
        }

        holder.binding.container.setOnLongClickListener {
            callbacks?.onArtistLongClicked(
                    artists[holder.bindingAdapterPosition],
                    holder.binding.art as ImageView)
            true
        }
    }

     




    fun updateData(newArtists: List<Artist>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = artists.size
            override fun getNewListSize() = newArtists.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                artists[oldItemPosition].id == newArtists[newItemPosition].id

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                artists[oldItemPosition] == newArtists[newItemPosition]
        })
        artists = newArtists
        diff.dispatchUpdatesTo(this)
    }

     




    fun setCallbacks(callbacks: AdapterDashboardArtistsCallbacks) {
        this.callbacks = callbacks
    }

    inner class Holder(val binding: AdapterCarouselBinding) :
            HorizontalListViewHolder(binding.root)

    companion object {
         


        interface AdapterDashboardArtistsCallbacks {
             




            fun onArtistClicked(artist: Artist)

             





            fun onArtistLongClicked(artist: Artist, imageView: ImageView)
        }
    }
}

