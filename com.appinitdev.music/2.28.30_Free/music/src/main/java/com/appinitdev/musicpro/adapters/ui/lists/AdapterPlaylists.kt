class AdapterPlaylists(initial: List<PlaylistWithSongs>) : FastScrollAdapter<VerticalListViewHolder>() {
    private var onPlaylistClicked: ((Playlist) -> Unit)? = null
    private var onPlaylistLongClicked: ((PlaylistWithSongs, ImageView?) -> Unit)? = null
    private val listUpdateCallback = object : ListUpdateCallback {
        @SuppressLint("NotifyDataSetChanged")
        override fun onInserted(position: Int, count: Int) {
            if (count > 50) notifyDataSetChanged() else notifyItemRangeInserted(position, count)
        }
        @SuppressLint("NotifyDataSetChanged")
        override fun onRemoved(position: Int, count: Int) {
            if (count > 50) notifyDataSetChanged() else notifyItemRangeRemoved(position, count)
        }
        override fun onMoved(fromPosition: Int, toPosition: Int) = notifyItemMoved(fromPosition, toPosition)
        override fun onChanged(position: Int, count: Int, payload: Any?) =
            notifyItemRangeChanged(position, count, payload)
    }
    private val diffCallback = object : DiffUtil.ItemCallback<PlaylistWithSongs>() {
        override fun areItemsTheSame(oldItem: PlaylistWithSongs, newItem: PlaylistWithSongs): Boolean =
            oldItem.playlist.id == newItem.playlist.id
        override fun areContentsTheSame(oldItem: PlaylistWithSongs, newItem: PlaylistWithSongs): Boolean =
            oldItem.playlist.name == newItem.playlist.name &&
                    oldItem.playlist.description == newItem.playlist.description &&
                    oldItem.playlist.dateModified == newItem.playlist.dateModified &&
                    oldItem.playlist.isPinned == newItem.playlist.isPinned &&
                    oldItem.songs.size == newItem.songs.size
    }
    private val differ = AsyncListDiffer(
            listUpdateCallback,
            AsyncDifferConfig.Builder(diffCallback).build()
    )
    private val items: List<PlaylistWithSongs> get() = differ.currentList
    var layoutMode: CommonPreferencesConstants.LayoutMode = PlaylistPreferences.getGridSize()
    init {
        setHasStableIds(true)
        differ.submitList(initial)
    }
    override fun getItemId(position: Int): Long = items[position].playlist.id
    override fun getItemViewType(position: Int): Int {
        return when {
            layoutMode.isLabel -> CommonPreferencesConstants.GRID_TYPE_LABEL
            layoutMode.isGrid -> CommonPreferencesConstants.GRID_TYPE_GRID
            else -> CommonPreferencesConstants.GRID_TYPE_LIST
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            CommonPreferencesConstants.GRID_TYPE_GRID ->
                GridHolder(AdapterStyleGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            CommonPreferencesConstants.GRID_TYPE_LABEL ->
                LabelHolder(AdapterStyleLabelsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else ->
                ListHolder(AdapterStyleListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }
    override fun onBind(holder: VerticalListViewHolder, position: Int, isLightBind: Boolean) {
        val item = items[position]
        when (holder) {
            is ListHolder -> holder.bind(item, isLightBind)
            is GridHolder -> holder.bind(item, isLightBind)
            is LabelHolder -> holder.bind(item, isLightBind)
        }
    }
    override fun getItemCount(): Int = items.size
    override fun onViewRecycled(holder: VerticalListViewHolder) {
        holder.itemView.clearAnimation()
        super.onViewRecycled(holder)
        when (holder) {
            is ListHolder -> Glide.with(holder.binding.cover).clear(holder.binding.cover)
            is GridHolder -> Glide.with(holder.binding.albumArt).clear(holder.binding.albumArt)
            is LabelHolder -> Unit
        }
    }
    fun updatePlaylists(list: List<PlaylistWithSongs>) {
        differ.submitList(list)
    }
    fun setOnPlaylistClicked(block: (Playlist) -> Unit) {
        onPlaylistClicked = block
    }
    fun setOnPlaylistLongClicked(block: (PlaylistWithSongs, ImageView?) -> Unit) {
        onPlaylistLongClicked = block
    }
    inner class ListHolder(val binding: AdapterStyleListBinding) : VerticalListViewHolder(binding.root) {
        fun bind(item: PlaylistWithSongs, isLightBind: Boolean) {
            binding.title.setTextOrUnknown(item.playlist.name)
            binding.title.addPlaylistTypeIcon(item.playlist)
            binding.secondaryDetail.text = binding.root.context.getString(R.string.x_songs, item.songs.size)
            binding.tertiaryDetail.text = item.playlist.description.ifNullOrBlank(getString(R.string.not_available))
            if (isLightBind) return
            item.songs.firstOrNull()?.let { binding.cover.loadArtCoverWithPayload(it) }
            binding.container.setOnClickListener {
                onPlaylistClicked?.invoke(item.playlist)
            }
            binding.container.setOnLongClickListener {
                onPlaylistLongClicked?.invoke(item, binding.cover)
                true
            }
        }
    }
    inner class GridHolder(val binding: AdapterStyleGridBinding) : VerticalListViewHolder(binding.root) {
        fun bind(item: PlaylistWithSongs, isLightBind: Boolean) {
            binding.title.setTextOrUnknown(item.playlist.name)
            binding.title.addPlaylistTypeIcon(item.playlist)
            binding.secondaryDetail.text = binding.root.context.getString(R.string.x_songs, item.songs.size)
            binding.tertiaryDetail.text = item.playlist.description.ifNullOrBlank(getString(R.string.not_available))
            if (isLightBind) return
            item.songs.firstOrNull()?.let { binding.albumArt.loadArtCoverWithPayload(it) }
            binding.container.setOnClickListener {
                onPlaylistClicked?.invoke(item.playlist)
            }
            binding.container.setOnLongClickListener {
                onPlaylistLongClicked?.invoke(item, binding.albumArt)
                true
            }
        }
    }
    inner class LabelHolder(val binding: AdapterStyleLabelsBinding) : VerticalListViewHolder(binding.root) {
        fun bind(item: PlaylistWithSongs, isLightBind: Boolean) {
            binding.title.setTextOrUnknown(item.playlist.name)
            binding.title.addPlaylistTypeIcon(item.playlist)
            binding.secondaryDetail.text = binding.root.context.getString(R.string.x_songs, item.songs.size)
            binding.tertiaryDetail.text = item.playlist.description.ifNullOrBlank(getString(R.string.not_available))
            if (isLightBind) return
            binding.container.setOnClickListener {
                onPlaylistClicked?.invoke(item.playlist)
            }
            binding.container.setOnLongClickListener {
                onPlaylistLongClicked?.invoke(item, null)
                true
            }
        }
    }
}
