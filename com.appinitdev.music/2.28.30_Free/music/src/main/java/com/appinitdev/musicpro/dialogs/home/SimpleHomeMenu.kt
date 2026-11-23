class SimpleHomeMenu : ScopedBottomSheetFragment() {
    private lateinit var binding: DialogHomeMenuBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogHomeMenuBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateGridTypeState()
        binding.openAppSettings.setOnClickListener {
            openAppSettings()
        }
        binding.openAppSettings.setOnClickListener {
            openAppSettings()
        }
        binding.rescanLibrary.setOnClickListener {
            com.appinitdev.musicpro.repository.services.AudioDatabaseService.refreshScan(requireContext())
            dismiss()
        }
        binding.wipeAndRescan.setOnClickListener {
            com.appinitdev.musicpro.repository.services.AudioDatabaseService.wipeScan(requireContext())
            dismiss()
        }
    }
    private fun updateGridTypeState() {
        binding.gridTypeGroup.setButtons(
                listOf(
                        Button(iconResId = R.drawable.ic_grid_16dp),
                        Button(iconResId = R.drawable.ic_list_16dp)
                )
        )
        when (HomePreferences.getHomeLayoutType()) {
            CommonPreferencesConstants.GRID_TYPE_GRID -> binding.gridTypeGroup.setSelectedIndex(0)
            CommonPreferencesConstants.GRID_TYPE_LIST -> binding.gridTypeGroup.setSelectedIndex(1)
        }
        binding.gridTypeGroup.setOnButtonSelectedListener {
            when (it) {
                0 -> HomePreferences.setHomeLayoutType(CommonPreferencesConstants.GRID_TYPE_GRID)
                1 -> HomePreferences.setHomeLayoutType(CommonPreferencesConstants.GRID_TYPE_LIST)
            }
        }
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
        }
    }
    companion object {
        fun newInstance(): SimpleHomeMenu {
            val args = Bundle()
            val fragment = SimpleHomeMenu()
            fragment.arguments = args
            return fragment
        }
        private const val TAG = "HomeMenu"
        fun FragmentManager.showHomeMenu(): SimpleHomeMenu {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }
    }
}
