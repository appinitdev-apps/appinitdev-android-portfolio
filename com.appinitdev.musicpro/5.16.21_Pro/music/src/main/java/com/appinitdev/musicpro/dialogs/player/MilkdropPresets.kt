
 










class MilkdropPresets : ScopedBottomSheetFragment() {

    private lateinit var binding: DialogMilkdropPresetsBinding
    private var adapter: AdapterMilkdropPresets? = null

    private val viewModel: MilkdropPresetsViewModel by viewModels()

     




    private val milkdropViewModel: MilkdropViewModel by viewModels(
            ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DialogMilkdropPresetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentPath = MilkdropPreferences.getLastPreset()

        adapter = AdapterMilkdropPresets { preset ->
            onPresetSelected(preset)
        }.also { a ->
            a.setSelectedPath(currentPath)
            binding.presetsRecyclerView.adapter = a
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.presets.collect { list ->
                    adapter?.submitList(list)
                    
                    val selectedIndex = list.indexOfFirst { it.path == currentPath }
                    if (selectedIndex >= 0) {
                        binding.presetsRecyclerView.scrollToPosition(selectedIndex)
                    }
                }
            }
        }
    }

    private fun onPresetSelected(preset: MilkdropPreset) {
        
        
        milkdropViewModel.selectPreset(preset.path)
        dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    companion object {
        fun newInstance(): MilkdropPresets {
            return MilkdropPresets().apply {
                arguments = Bundle()
            }
        }

        fun FragmentManager.showMilkdropPresets() {
            newInstance().show(this, TAG)
        }

        const val TAG = "MilkdropPresets"
    }
}