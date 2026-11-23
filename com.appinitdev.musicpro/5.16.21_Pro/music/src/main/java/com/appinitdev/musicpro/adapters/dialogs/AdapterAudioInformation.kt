class AdapterAudioInformation(private val data: List<Data>) : Adapter<Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(AdapterAudioInfoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind()
    }
    override fun getItemCount(): Int {
        return data.size
    }
    fun getSpanSize(position: Int, spanCount: Int): Int {
        return if (data[position].isFullSpan) spanCount else 1
    }
    inner class Holder(val binding: AdapterAudioInfoBinding) : VerticalListViewHolder(binding.root) {
        fun bind() {
            val item = data[bindingAdapterPosition]
            binding.type.setText(item.type)
            binding.value.text = item.value
        }
    }
    data class Data(
            @StringRes val type: Int,
            val value: String,
            val isFullSpan: Boolean = false
    )
}