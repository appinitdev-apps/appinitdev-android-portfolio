class AdapterFolderList(
        private var uris: List<String> = emptyList(),
        private val onAdd: () -> Unit,
        private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<VerticalListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_HEADER -> {
                val binding = AdapterFolderListHeaderBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                )
                Header(binding)
            }
            else -> {
                val binding = AdapterFolderListItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                )
                Holder(binding)
            }
        }
    }
    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        when (holder) {
            is Header -> {
                holder.binding.title.text = holder.getString(R.string.manage_music_folders)
                holder.binding.summary.text = holder.getString(R.string.manage_music_folders_desc)
                holder.binding.add.setOnClickListener { onAdd() }
            }
            is Holder -> {
                val uriString = uris[position - 1]
                val uri = uriString.toUri()
                val docFile = try {
                    DocumentFile.fromTreeUri(holder.itemView.context, uri)
                } catch (_: Exception) {
                    null
                }
                holder.binding.name.text = docFile?.name ?: uriString
                holder.binding.path.text = uriString
                holder.binding.remove.setOnClickListener { onRemove(uriString) }
            }
        }
    }
    override fun getItemCount(): Int = uris.size + 1 
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) RecyclerViewUtils.TYPE_HEADER else RecyclerViewUtils.TYPE_ITEM
    }
    fun submitList(newUris: List<String>) {
        uris = newUris
        notifyDataSetChanged()
    }
    inner class Holder(val binding: AdapterFolderListItemBinding) : VerticalListViewHolder(binding.root)
    inner class Header(val binding: AdapterFolderListHeaderBinding) : VerticalListViewHolder(binding.root)
}
