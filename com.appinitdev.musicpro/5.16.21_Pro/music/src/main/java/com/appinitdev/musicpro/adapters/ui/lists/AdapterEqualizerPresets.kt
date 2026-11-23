
 
















class AdapterEqualizerPresets(
        private val onPresetClicked: (EqualizerPreset) -> Unit,
        private val onOptionClicked: (EqualizerPreset, View) -> Unit = { _, _ -> }
) : ListAdapter<EqualizerPreset, AdapterEqualizerPresets.ViewHolder>(DIFF_CALLBACK) {

     
    private var selectedPresetId: Long = -1L

     





    fun setSelectedPresetId(presetId: Long) {
        val oldId = selectedPresetId
        selectedPresetId = presetId

        
        val oldIndex = currentList.indexOfFirst { it.id == oldId }
        val newIndex = currentList.indexOfFirst { it.id == presetId }
        if (oldIndex >= 0) notifyItemChanged(oldIndex)
        if (newIndex >= 0) notifyItemChanged(newIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterEqualizerPresetBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
            private val binding: AdapterEqualizerPresetBinding
    ) : VerticalListViewHolder(binding.root) {

        fun bind(preset: EqualizerPreset) {
            binding.presetName.text = preset.name

            
            
            if (preset.isBuiltIn) {
                binding.presetBuiltInLabel.text = binding.root.context.getString(
                        com.appinitdev.musicpro.R.string.built_in)
                binding.presetBuiltInLabel.visible()
            } else {
                binding.presetBuiltInLabel.gone()
            }

            
            if (preset.id == selectedPresetId) {
                binding.presetSelectedIndicator.text = "●"
                binding.presetSelectedIndicator.visible()
            } else {
                binding.presetSelectedIndicator.gone()
            }

            
            binding.equalizerWave.setGains(preset.getBandGains())

            binding.container.setOnClickListener {
                onPresetClicked(preset)
            }

            binding.presetOptionsButton.setOnClickListener {
                onOptionClicked(preset, binding.presetOptionsButton)
                true
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EqualizerPreset>() {
            override fun areItemsTheSame(oldItem: EqualizerPreset, newItem: EqualizerPreset): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: EqualizerPreset, newItem: EqualizerPreset): Boolean {
                return oldItem == newItem
            }
        }
    }
}

