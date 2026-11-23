
 









class AdapterBookmarkTimestamps(
        private val items: MutableList<AudioBookmark>,
        private val showDeleteButton: Boolean,
        private val onTimestampClicked: (AudioBookmark) -> Unit,
        private val onDeleteClicked: ((AudioBookmark, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<AdapterBookmarkTimestamps.BookmarkHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkHolder {
        val binding = ItemBookmarkRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookmarkHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarkHolder, position: Int) {
        holder.bind(items[position])
    }

     



    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun isEmpty(): Boolean = items.isEmpty()

    inner class BookmarkHolder(private val binding: ItemBookmarkRowBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(bookmark: AudioBookmark) {
            val label = DateUtils.formatElapsedTime(bookmark.timestampMs / 1000L)
            binding.bookmarkTimestamp.text = label

            binding.bookmarkTimestamp.setOnClickListener {
                onTimestampClicked(bookmark)
            }

            if (showDeleteButton) {
                binding.bookmarkDelete.visibility = View.VISIBLE
                binding.bookmarkDelete.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_ID.toInt()) {
                        removeAt(pos)
                        onDeleteClicked?.invoke(bookmark, isEmpty())
                    }
                }
            } else {
                binding.bookmarkDelete.visibility = View.GONE
            }
        }
    }
}

