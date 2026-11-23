class AdapterRecentlyPlayed(initial: List<AudioWithStat>) : FastScrollAdapter<VerticalListViewHolder>() {
    private var generalAdapterCallbacks: GeneralAdapterCallbacks? = null
    private val listUpdateCallback = object : ListUpdateCallback {
        @SuppressLint("NotifyDataSetChanged")
        override fun onInserted(position: Int, count: Int) {
            if (count > 100) notifyDataSetChanged() else notifyItemRangeInserted(position, count)
        }
        @SuppressLint("NotifyDataSetChanged")
        override fun onRemoved(position: Int, count: Int) {
            if (count > 100) notifyDataSetChanged() else notifyItemRangeRemoved(position, count)
        }
        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }
        override fun onChanged(position: Int, count: Int, payload: Any?) {
            notifyItemRangeChanged(position, count, payload)
        }
    }
    private val diffCallback = object : DiffUtil.ItemCallback<AudioWithStat>() {
        override fun areItemsTheSame(oldItem: AudioWithStat, newItem: AudioWithStat) =
            oldItem.audio.id == newItem.audio.id
        override fun areContentsTheSame(oldItem: AudioWithStat, newItem: AudioWithStat): Boolean {
            return oldItem.audio.title == newItem.audio.title &&
                    oldItem.audio.artist == newItem.audio.artist &&
                    oldItem.audio.album == newItem.audio.album &&
                    oldItem.audio.duration == newItem.audio.duration &&
                    oldItem.lastPlayed == newItem.lastPlayed
        }
    }
    private val differ = AsyncListDiffer(
            listUpdateCallback,
            AsyncDifferConfig.Builder(diffCallback).build()
    )
    private val songs: List<AudioWithStat> get() = differ.currentList
    private val audioList: List<Audio> get() = songs.map { it.audio }
    var layoutMode: CommonPreferencesConstants.LayoutMode = RecentlyPlayedPreferences.getGridSize()
    init {
        setHasStableIds(true)
        differ.submitList(initial.toList())
    }
    override fun getItemId(position: Int): Long = songs[position].audio.id
    override fun getItemViewType(position: Int): Int = when {
        layoutMode.isLabel -> CommonPreferencesConstants.GRID_TYPE_LABEL
        layoutMode.isGrid -> CommonPreferencesConstants.GRID_TYPE_GRID
        else -> CommonPreferencesConstants.GRID_TYPE_LIST
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
        val item = songs[position]
        when (holder) {
            is ListHolder -> holder.bind(item, isLightBind)
            is GridHolder -> holder.bind(item, isLightBind)
            is LabelHolder -> holder.bind(item, isLightBind)
        }
    }
    override fun getItemCount(): Int = songs.size
    override fun onViewRecycled(holder: VerticalListViewHolder) {
        holder.itemView.clearAnimation()
        super.onViewRecycled(holder)
        when (holder) {
            is ListHolder -> Glide.with(holder.binding.cover).clear(holder.binding.cover)
            is GridHolder -> Glide.with(holder.binding.albumArt).clear(holder.binding.albumArt)
            is LabelHolder -> Unit
        }
    }
    fun setGeneralAdapterCallbacks(callbacks: GeneralAdapterCallbacks) {
        this.generalAdapterCallbacks = callbacks
    }
    fun updateSongs(newSongs: List<AudioWithStat>) {
        differ.submitList(newSongs.toList())
    }
    private fun buildTertiaryText(item: AudioWithStat): CharSequence {
        val album = item.audio.getProperAlbum().takeIf { it.isNotEmpty() }
        val stat = formatRelativeTime(item.lastPlayed)
        return if (album != null) "$stat \u2022 $album" else stat
    }
    inner class ListHolder(val binding: AdapterStyleListBinding) : VerticalListViewHolder(binding.root) {
        fun bind(item: AudioWithStat, isLightBind: Boolean) {
            val audio = item.audio
            binding.title.setTextOrUnknown(audio.getProperTitle())
            binding.secondaryDetail.setTextOrUnknown(audio.getProperArtists())
            binding.tertiaryDetail.text = buildTertiaryText(item)
            binding.title.addAudioQualityIcon(audio)
            binding.container.setAudioID(audio.id)
            if (isLightBind) return
            binding.cover.loadArtCoverWithPayload(audio)
            binding.container.setOnLongClickListener {
                generalAdapterCallbacks?.onSongLongClicked(audioList, bindingAdapterPosition, binding.cover)
                true
            }
            binding.container.setOnClickListener {
                generalAdapterCallbacks?.onSongClicked(audioList, bindingAdapterPosition, it)
            }
        }
    }
    inner class GridHolder(val binding: AdapterStyleGridBinding) : VerticalListViewHolder(binding.root) {
        fun bind(item: AudioWithStat, isLightBind: Boolean) {
            val audio = item.audio
            binding.container.enableGridMode = true
            binding.title.setTextOrUnknown(audio.getProperTitle())
            binding.secondaryDetail.setTextOrUnknown(audio.artist)
            binding.tertiaryDetail.text = buildTertiaryText(item)
            binding.container.setAudioID(audio.id)
            if (isLightBind) return
            binding.albumArt.loadArtCoverWithPayload(audio)
            binding.container.setOnLongClickListener {
                generalAdapterCallbacks?.onSongLongClicked(audioList, bindingAdapterPosition, binding.albumArt)
                true
            }
            binding.container.setOnClickListener {
                generalAdapterCallbacks?.onSongClicked(audioList, bindingAdapterPosition, it)
            }
        }
    }
    inner class LabelHolder(val binding: AdapterStyleLabelsBinding) : VerticalListViewHolder(binding.root) {
        fun bind(item: AudioWithStat, isLightBind: Boolean) {
            val audio = item.audio
            binding.title.setTextOrUnknown(audio.getProperTitle())
            binding.secondaryDetail.setTextOrUnknown(audio.getProperArtists())
            binding.tertiaryDetail.text = buildTertiaryText(item)
            binding.title.addAudioQualityIcon(audio)
            binding.container.setAudioID(audio.id)
            if (isLightBind) return
            binding.container.setOnLongClickListener {
                generalAdapterCallbacks?.onSongLongClicked(audioList, bindingAdapterPosition, null)
                true
            }
            binding.container.setOnClickListener {
                generalAdapterCallbacks?.onSongClicked(audioList, bindingAdapterPosition, it)
            }
        }
    }
    companion object {
        fun formatRelativeTime(timestamp: Long): CharSequence {
            return DateUtils.getRelativeTimeSpanString(
                    timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            )
        }
    }
}
