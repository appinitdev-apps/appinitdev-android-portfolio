
 












class AdapterMilkdropPager(
        private val onPageClicked: () -> Unit,
) : RecyclerView.Adapter<AdapterMilkdropPager.PageViewHolder>() {

    private var presets: List<MilkdropPreset> = emptyList()

     




    fun submitList(list: List<MilkdropPreset>) {
        presets = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = AdapterMilkdropPagerPageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
        )
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(presets[position])
    }

    override fun getItemCount(): Int = presets.size

     




    inner class PageViewHolder(
            private val binding: AdapterMilkdropPagerPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

         
        fun bind(preset: MilkdropPreset) {
            binding.presetName.text = preset.name
            binding.root.setOnClickListener { onPageClicked() }
        }
    }
}

