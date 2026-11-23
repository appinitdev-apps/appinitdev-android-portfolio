
 









class AdapterDashboardSongs(
        private var songs: List<Audio>
) : RecyclerView.Adapter<AdapterDashboardSongs.Holder>() {

    private var callbacks: AdapterDashboardSongsCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = AdapterCarouselBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int = songs.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val song = songs[position]
        holder.binding.art.loadArtCoverWithPayload(song)
        holder.binding.title.text = song.getProperTitle()
        holder.binding.artist.text = song.getProperArtists()
        holder.binding.container.setOnClickListener {
            callbacks?.onSongClicked(songs.toMutableList(), holder.bindingAdapterPosition)
        }
        holder.binding.container.setOnLongClickListener {
            callbacks?.onSongLongClicked(
                    songs.toMutableList(),
                    holder.bindingAdapterPosition,
                    holder.binding.art as ImageView)
            true
        }
    }

     






    fun updateData(newSongs: List<Audio>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = songs.size
            override fun getNewListSize() = newSongs.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                songs[oldItemPosition].id == newSongs[newItemPosition].id
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                songs[oldItemPosition] == newSongs[newItemPosition]
        })
        songs = newSongs
        diff.dispatchUpdatesTo(this)
    }

     






    fun randomize(holder: Holder) {
        if (songs.isEmpty()) return
        val randomSong = songs.random()
        holder.binding.art.loadArtCoverWithPayload(randomSong)
        holder.binding.title.text = randomSong.getProperTitle()
        holder.binding.artist.text = randomSong.getProperArtists()
    }

     




    fun setCallbacks(callbacks: AdapterDashboardSongsCallbacks) {
        this.callbacks = callbacks
    }

    inner class Holder(val binding: AdapterCarouselBinding) :
            HorizontalListViewHolder(binding.root)

    companion object {
         


        interface AdapterDashboardSongsCallbacks {
             





            fun onSongClicked(songs: MutableList<Audio>, position: Int)

             






            fun onSongLongClicked(songs: MutableList<Audio>, position: Int, imageView: ImageView)
        }
    }
}

