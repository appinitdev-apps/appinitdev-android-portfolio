class AdapterLrcSearch(
        private val onItemClick: (LrcLibResponse) -> Unit
) : RecyclerView.Adapter<AdapterLrcSearch.LrcViewHolder>() {
    private val diffCallback = object : DiffUtil.ItemCallback<LrcLibResponse>() {
        override fun areItemsTheSame(oldItem: LrcLibResponse, newItem: LrcLibResponse): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: LrcLibResponse, newItem: LrcLibResponse): Boolean {
            return oldItem == newItem
        }
    }
    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
        }
        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }
        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }
        override fun onChanged(position: Int, count: Int, payload: Any?) {
            notifyItemRangeChanged(position, count, payload)
        }
    }
    private val differ = AsyncListDiffer(
            listUpdateCallback,
            AsyncDifferConfig.Builder(diffCallback).build()
    )
    private val results: List<LrcLibResponse>
        get() = differ.currentList
    init {
        setHasStableIds(true)
    }
    override fun getItemId(position: Int): Long = results[position].id.toLong()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LrcViewHolder {
        val binding = AdapterLrcSearchBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
        )
        return LrcViewHolder(binding)
    }
    override fun onBindViewHolder(holder: LrcViewHolder, position: Int) {
        holder.bind(results[position])
    }
    override fun getItemCount(): Int = results.size
    fun updateResults(newResults: List<LrcLibResponse>) {
        differ.submitList(newResults.toList())
    }
    inner class LrcViewHolder(private val binding: AdapterLrcSearchBinding) :
            VerticalListViewHolder(binding.root) {
        fun bind(response: LrcLibResponse) {
            binding.trackName.text = response.trackName
            binding.artistName.text = response.artistName
            val album = response.albumName?.takeIf { it.isNotBlank() }
            val duration = DateUtils.formatElapsedTime(response.duration.toLong())
            binding.albumDuration.text = when {
                album != null -> "$album · $duration"
                else -> duration
            }
            binding.syncedBadge.visibility =
                if (!response.syncedLyrics.isNullOrBlank()) View.VISIBLE else View.GONE
            binding.container.setOnClickListener {
                onItemClick(response)
            }
        }
    }
}
