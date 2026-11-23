class AdapterPlaylistCheckbox(
        private var playlists: List<Playlist>,
        preCheckedIds: Set<Long> = emptySet(),
        private var songCounts: Map<Long, Int> = emptyMap()
) : RecyclerView.Adapter<AdapterPlaylistCheckbox.ViewHolder>() {
    private val checkedIds: MutableSet<Long> = preCheckedIds.toMutableSet()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                AdapterPlaylistCheckboxBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                )
        )
    }
    override fun getItemCount(): Int = playlists.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(playlists[position])
    }
    fun getCheckedIds(): Set<Long> = checkedIds.toSet()
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newPlaylists: List<Playlist>, newSongCounts: Map<Long, Int>) {
        this.playlists = newPlaylists
        this.songCounts = newSongCounts
        val validIds = newPlaylists.map { it.id }.toSet()
        checkedIds.retainAll(validIds)
        notifyDataSetChanged()
    }
    @Suppress("unused")
    @SuppressLint("NotifyDataSetChanged")
    fun updateSongCounts(counts: Map<Long, Int>) {
        this.songCounts = counts
        notifyDataSetChanged()
    }
    inner class ViewHolder(private val binding: AdapterPlaylistCheckboxBinding) :
            VerticalListViewHolder(binding.root) {
        fun bind(playlist: Playlist) {
            binding.name.text = playlist.name
            binding.count.text = binding.root.context.getString(
                    R.string.x_songs, songCounts[playlist.id] ?: 0
            )
            binding.checkbox.setOnCheckedChangeListener(null)
            binding.checkbox.setChecked(checkedIds.contains(playlist.id))
            binding.checkbox.setOnCheckedChangeListener { checked ->
                if (checked) checkedIds.add(playlist.id) else checkedIds.remove(playlist.id)
            }
            binding.container.setOnClickListener {
                binding.checkbox.toggle()
            }
        }
    }
}
