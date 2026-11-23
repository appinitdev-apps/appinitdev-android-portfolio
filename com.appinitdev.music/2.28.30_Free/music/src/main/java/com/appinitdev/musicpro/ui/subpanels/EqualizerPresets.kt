
 













class EqualizerPresets : MediaFragment() {

    private lateinit var binding: FragmentEqualizerPresetsBinding
    private lateinit var headerBinding: HeaderEqualizerPresetsBinding

    private val viewModel: EqualizerPresetsViewModel by viewModels()
    private var adapter: AdapterEqualizerPresets? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentEqualizerPresetsBinding.inflate(inflater, container, false)
        headerBinding = HeaderEqualizerPresetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireHiddenMiniPlayer()

        
        
        binding.appHeader.setContentView(headerBinding.root)
        binding.appHeader.attachTo(binding.recyclerView, AppHeader.ScrollMode.HIDE_ON_SCROLL)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupAdapter()
        observePresets()
    }

    override fun onDestroyView() {
        adapter = null
        super.onDestroyView()
    }

     



    private fun setupAdapter() {
        adapter = AdapterEqualizerPresets(
                onPresetClicked = ::applyPreset,
                onOptionClicked = ::showPresetOptions
        )
        binding.recyclerView.adapter = adapter
    }

     



    private fun observePresets() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.presets.collect { presets ->
                    adapter?.submitList(presets)
                }
            }
        }
    }

     






    private fun applyPreset(preset: EqualizerPreset) {
        val gains = preset.getBandGains()

        
        
        EqualizerPreferences.setAllBandGains(gains)
        for (i in gains.indices) {
            EqualizerManager.setBandGain(i, gains[i], persist = false)
        }
        EqualizerManager.setPreamp(preset.preampDb)

        goBack()
    }

     







    private fun showPresetOptions(preset: EqualizerPreset, anchorView: View) {
        SharedScrollViewPopup(
                container = requireContainerView(),
                anchorView = anchorView,
                menuItems = listOf(
                        PopupMenuItem(title = R.string.apply_preset),
                        PopupMenuItem(title = R.string.delete),
                ),
                onMenuItemClick = {
                    val effect = when (it) {
                        R.string.apply_preset -> applyPreset(preset)
                        R.string.delete -> confirmAndDelete(preset)
                        else -> return@SharedScrollViewPopup
                    }
                },
                onDismiss = {}
        ).show()
    }

     






    private fun confirmAndDelete(preset: EqualizerPreset) {
        withSureDialog { confirmed ->
            if (confirmed) {
                viewModel.deletePreset(preset)
            }
        }
    }

    companion object {

        const val TAG = "EqualizerPresets"

         



        fun newInstance(): EqualizerPresets {
            return EqualizerPresets().apply {
                arguments = Bundle()
            }
        }
    }
}