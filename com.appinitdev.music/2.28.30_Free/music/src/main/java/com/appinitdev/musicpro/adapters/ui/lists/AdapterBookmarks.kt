class AdapterBookmarks(
        private var items: List<AudioWithBookmarks>
) : RecyclerView.Adapter<VerticalListViewHolder>() {
    var callbacks: Callbacks? = null
    override fun getItemCount(): Int = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return SongHolder(AdapterStyleListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        (holder as SongHolder).bind(items[position])
    }
    inner class SongHolder(private val binding: AdapterStyleListBinding) :
            VerticalListViewHolder(binding.root) {
        fun bind(item: AudioWithBookmarks) {
            binding.title.text = item.audio.getProperTitle()
            binding.secondaryDetail.text = item.audio.getProperArtists()
            binding.tertiaryDetail.text = context.buildTertiaryText(item)
            binding.cover.loadArtCoverWithPayload(item.audio)
            binding.container.setOnClickListener {
                callbacks?.onSongClicked(item)
            }
        }
    }
    private fun Context.buildTertiaryText(item: AudioWithBookmarks): String {
        val album = item.audio.getProperAlbum().takeIf { it.isNotEmpty() }
        val stat = resources.getQuantityString(R.plurals.number_of_bookmarks, item.bookmarks.size, item.bookmarks.size)
        return if (album != null) "$stat \u2022 $album" else stat
    }
    fun updateItems(newItems: List<AudioWithBookmarks>) {
        items = newItems
        notifyDataSetChanged()
    }
    interface Callbacks {
        fun onSongClicked(item: AudioWithBookmarks)
    }
}
