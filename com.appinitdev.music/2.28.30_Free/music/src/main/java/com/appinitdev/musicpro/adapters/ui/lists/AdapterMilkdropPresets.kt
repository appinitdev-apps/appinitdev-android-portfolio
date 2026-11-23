class AdapterMilkdropPresets(
        private val onPresetClicked: (MilkdropPreset) -> Unit
) : RecyclerView.Adapter<AdapterMilkdropPresets.ViewHolder>() {
    private var presets: List<MilkdropPreset> = emptyList()
    private var selectedPath: String = ""
    fun submitList(list: List<MilkdropPreset>) {
        presets = list
        notifyDataSetChanged()
    }
    fun setSelectedPath(path: String) {
        val oldIndex = presets.indexOfFirst { it.path == selectedPath }
        selectedPath = path
        val newIndex = presets.indexOfFirst { it.path == path }
        if (oldIndex >= 0) notifyItemChanged(oldIndex)
        if (newIndex >= 0) notifyItemChanged(newIndex)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterMilkdropPresetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(presets[position])
    }
    override fun getItemCount(): Int = presets.size
    inner class ViewHolder(
            private val binding: AdapterMilkdropPresetBinding
    ) : VerticalListViewHolder(binding.root) {
        fun bind(preset: MilkdropPreset) {
            val relativePath = preset.path
                .removePrefix("${PresetManager.PRESETS_ROOT}/")
                .removeSuffix(".milk")
            val lastSlash = relativePath.lastIndexOf('/')
            if (lastSlash >= 0) {
                val folderPart = relativePath.substring(0, lastSlash + 1)
                val spannable = SpannableString(relativePath)
                spannable.setSpan(
                        ForegroundColorSpan(Color.GRAY),
                        0, folderPart.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.presetName.text = spannable
            } else {
                binding.presetName.text = preset.name
            }
            binding.presetName.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0,
                    if (preset.path == selectedPath) R.drawable.ic_ring_12dp else 0,
                    0
            )
            binding.container.setOnClickListener { onPresetClicked(preset) }
        }
    }
}
