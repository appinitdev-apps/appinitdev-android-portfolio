
class HeaderVH(private val binding: RecyclerHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

    init {
        val params = binding.root.layoutParams
        if (params is StaggeredGridLayoutManager.LayoutParams) {
            params.isFullSpan = true
        }
    }

    fun bind(header: Header) {
        binding.root.text = header.label
    }
}
