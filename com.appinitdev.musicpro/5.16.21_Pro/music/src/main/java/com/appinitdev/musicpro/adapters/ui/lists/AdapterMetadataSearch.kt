
 











class AdapterMetadataSearch(
        private val onItemClick: (MusicBrainzRecordingResult) -> Unit
) : RecyclerView.Adapter<AdapterMetadataSearch.MetadataViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<MusicBrainzRecordingResult>() {
        override fun areItemsTheSame(oldItem: MusicBrainzRecordingResult, newItem: MusicBrainzRecordingResult) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MusicBrainzRecordingResult, newItem: MusicBrainzRecordingResult) =
            oldItem == newItem
    }

    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) = notifyItemRangeInserted(position, count)
        override fun onRemoved(position: Int, count: Int) = notifyItemRangeRemoved(position, count)
        override fun onMoved(from: Int, to: Int) = notifyItemMoved(from, to)
        override fun onChanged(position: Int, count: Int, payload: Any?) =
            notifyItemRangeChanged(position, count, payload)
    }

    private val differ = AsyncListDiffer(
            listUpdateCallback,
            AsyncDifferConfig.Builder(diffCallback).build()
    )

    private val results: List<MusicBrainzRecordingResult>
        get() = differ.currentList

    init {
        setHasStableIds(true)
    }

     



    override fun getItemId(position: Int): Long = results[position].id.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetadataViewHolder {
        val binding = AdapterMetadataSearchBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
        )
        return MetadataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MetadataViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size

     


    fun updateResults(newResults: List<MusicBrainzRecordingResult>) {
        differ.submitList(newResults.toList())
    }

    inner class MetadataViewHolder(private val binding: AdapterMetadataSearchBinding) :
            VerticalListViewHolder(binding.root) {

        fun bind(recording: MusicBrainzRecordingResult) {
            binding.trackName.text = recording.title.orEmpty()

            
            binding.artistName.text = recording.artistCredit
                ?.mapNotNull { it.name?.takeIf { n -> n.isNotBlank() } }
                ?.joinToString(", ")
                .orEmpty()

            
            val album = recording.releases?.firstOrNull()?.title?.takeIf { it.isNotBlank() }

            
            val duration = recording.length?.let { ms ->
                DateUtils.formatElapsedTime(ms / 1000)
            }

            binding.albumDuration.text = listOfNotNull(album, duration).joinToString(" · ")

            binding.container.setOnClickListener {
                onItemClick(recording)
            }
        }
    }
}
