class AdapterRecommended(private var list: List<Audio>) :
        RecyclerView.Adapter<AdapterRecommended.Holder>() {
    private lateinit var callbacks: AdapterRecommendedCallbacks
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(AdapterGridImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (list.isNotEmpty()) {
            val item = list[position]
            holder.binding.art.loadArtCover(
                    item = item,
                    shadow = false,
                    roundedCorners = false,
                    darken = false)
            holder.binding.container.setOnClickListener {
                if (list.isNotEmpty()) {
                    callbacks.onItemClicked(list, holder.bindingAdapterPosition)
                }
            }
            holder.binding.container.setOnLongClickListener {
                if (list.isNotEmpty()) {
                    callbacks.onItemLongClicked(list, holder.bindingAdapterPosition, holder.binding.art)
                }
                true
            }
            holder.binding.title.text = item.getProperTitle()
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }
    override fun getItemId(position: Int): Long {
        return list[position].id
    }
    inner class Holder(val binding: AdapterGridImageBinding) : RecyclerView.ViewHolder(binding.root)
    fun randomize() {
        for (i in 0 until itemCount) {
            notifyItemChanged(i)
        }
    }
    fun updateItem(position: Int) {
        notifyItemChanged(position)
    }
    fun updateData(newList: List<Audio>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = list.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                list[oldItemPosition].id == newList[newItemPosition].id
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                list[oldItemPosition] == newList[newItemPosition]
        })
        list = newList
        diff.dispatchUpdatesTo(this)
    }
    fun setCallbacks(callbacks: AdapterRecommendedCallbacks) {
        this.callbacks = callbacks
    }
    companion object {
        interface AdapterRecommendedCallbacks {
            fun onItemClicked(items: List<Audio>, position: Int)
            fun onItemLongClicked(items: List<Audio>, position: Int, imageView: ImageView)
        }
    }
}
