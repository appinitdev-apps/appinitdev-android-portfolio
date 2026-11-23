class PlaylistsSort : ScopedBottomSheetFragment() {
    private lateinit var binding: DialogSortPlaylistsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSortPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (PlaylistPreferences.getSongSort()) {
            CommonPreferencesConstants.BY_NAME -> binding.name.isChecked = true
            CommonPreferencesConstants.BY_DATE_ADDED -> binding.dateCreated.isChecked = true
            CommonPreferencesConstants.BY_DATE_MODIFIED -> binding.dateModified.isChecked = true
            CommonPreferencesConstants.BY_NUMBER_OF_SONGS -> binding.songCount.isChecked = true
            else -> binding.name.isChecked = true
        }
        binding.normal.isChecked = PlaylistPreferences.getSortingStyle() == CommonPreferencesConstants.ASCENDING
        binding.reversed.isChecked = PlaylistPreferences.getSortingStyle() == CommonPreferencesConstants.DESCENDING
        binding.sortByChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                binding.name.id -> PlaylistPreferences.setSongSort(CommonPreferencesConstants.BY_NAME)
                binding.dateCreated.id -> PlaylistPreferences.setSongSort(CommonPreferencesConstants.BY_DATE_ADDED)
                binding.dateModified.id -> PlaylistPreferences.setSongSort(CommonPreferencesConstants.BY_DATE_MODIFIED)
                binding.songCount.id -> PlaylistPreferences.setSongSort(CommonPreferencesConstants.BY_NUMBER_OF_SONGS)
            }
        }
        binding.sortingStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                binding.normal.id -> PlaylistPreferences.setSortingStyle(CommonPreferencesConstants.ASCENDING)
                binding.reversed.id -> PlaylistPreferences.setSortingStyle(CommonPreferencesConstants.DESCENDING)
            }
        }
    }
    companion object {
        private const val TAG = "PlaylistsSort"
        fun newInstance(): PlaylistsSort {
            return PlaylistsSort().apply {
                arguments = Bundle()
            }
        }
        fun FragmentManager.showPlaylistsSort(): PlaylistsSort {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }
    }
}
