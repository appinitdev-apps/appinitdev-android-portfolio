
 













class SaveEqualizerPreset : ScopedBottomSheetFragment() {

    private var binding: DialogSaveEqualizerPresetBinding? = null

     
    var onSave: ((String) -> Unit)? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return DialogSaveEqualizerPresetBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
        
        binding?.presetWavePreview?.setGains(EqualizerManager.getAllGains())

        
        
        binding?.presetNameInput?.requestFocus()

        binding?.cancel?.setOnClickListener {
            dismissAllowingStateLoss()
        }

        binding?.save?.setOnClickListener {
            val name = binding?.presetNameInput?.text?.toString()?.trim() ?: ""
            if (name.isNotEmpty()) {
                onSave?.invoke(name)
                dismissAllowingStateLoss()
            } else {
                
                binding?.presetNameInput?.animate()
                    ?.translationX(10f)?.setDuration(50)
                    ?.withEndAction {
                        binding?.presetNameInput?.animate()
                            ?.translationX(-10f)?.setDuration(50)
                            ?.withEndAction {
                                binding?.presetNameInput?.animate()
                                    ?.translationX(0f)?.setDuration(50)?.start()
                            }?.start()
                    }?.start()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {

        const val TAG = "SaveEqualizerPreset"

        fun newInstance(): SaveEqualizerPreset = SaveEqualizerPreset().apply {
            arguments = Bundle()
        }

         




        fun FragmentManager.showSaveEqualizerPreset(onSave: (String) -> Unit) {
            val fragment = newInstance()
            fragment.onSave = onSave
            fragment.show(this, TAG)
        }
    }
}

